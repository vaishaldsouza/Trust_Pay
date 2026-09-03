package com.example.engine

import com.example.crypto.CryptoEngine
import com.example.data.local.AppDatabase
import com.example.data.local.TransactionEntity
import com.example.data.local.UsedNonceEntity
import com.example.data.model.RiskSeverity
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.remote.RemoteFraudAlert
import com.example.data.remote.SupabaseClient
import com.example.data.remote.SupabaseDeviceRepository
import com.example.data.remote.SupabaseTransactionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.KeyPair
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class SyncProgressState(
    val isSyncing: Boolean = false,
    val progressPercent: Int = 0,
    val currentStepText: String = "Idle",
    val discoveredCount: Int = 0,
    val verifiedSignaturesCount: Int = 0,
    val replayAttackCount: Int = 0,
    val duplicateCount: Int = 0,
    val highRiskCount: Int = 0,
    val lowRiskCount: Int = 0,
    val settledCount: Int = 0,
    val reviewRequiredCount: Int = 0,
    val flaggedTransactionId: String? = null,
    val isComplete: Boolean = false,
    val supabaseSyncSuccess: Boolean = false
)

class SyncEngine(
    private val database: AppDatabase,
    private val buyerKeyPair: KeyPair = CryptoEngine.getOrCreateBuyerKeyPair(),
    private val supabaseTxRepo: SupabaseTransactionRepository = SupabaseTransactionRepository(),
    private val supabaseDeviceRepo: SupabaseDeviceRepository = SupabaseDeviceRepository()
) {
    private val _syncState = MutableStateFlow(SyncProgressState())
    val syncState: StateFlow<SyncProgressState> = _syncState.asStateFlow()

    suspend fun runSyncPipeline(
        onBuyerExposureUpdated: (Long) -> Unit = {}
    ): SyncProgressState {
        _syncState.value = SyncProgressState(
            isSyncing = true,
            progressPercent = 5,
            currentStepText = "1. Scanning offline Room queue..."
        )

        delay(250)
        val pendingEntities = database.transactionDao().getPendingOfflineTransactions()
        val totalDiscovered = pendingEntities.size

        if (totalDiscovered == 0) {
            val emptyState = SyncProgressState(
                isSyncing = false,
                progressPercent = 100,
                currentStepText = "Queue empty. All local and cloud transactions up to date.",
                isComplete = true,
                supabaseSyncSuccess = SupabaseClient.isConfigured()
            )
            _syncState.value = emptyState
            return emptyState
        }

        var verifiedSigCount = 0
        var replayCount = 0
        var dupCount = 0
        var highRisk = 0
        var lowRisk = 0
        var settled = 0
        var reviewReq = 0
        var flaggedId: String? = null

        _syncState.value = _syncState.value.copy(
            progressPercent = 15,
            currentStepText = "$totalDiscovered transactions discovered in local queue",
            discoveredCount = totalDiscovered
        )
        delay(300)

        // Register buyer device public key to Supabase if configured
        if (SupabaseClient.isConfigured()) {
            val buyerPubKeyBase64 = CryptoEngine.getPublicKeyBase64(buyerKeyPair.public)
            supabaseDeviceRepo.registerDevicePublicKey(
                userId = "buyer_device_01",
                publicKeyBase64 = buyerPubKeyBase64,
                keyAlgorithm = "Ed25519",
                trustTier = "STANDARD"
            )
        }

        // Process each transaction through the 15-step cloud reconciliation pipeline
        for ((index, entity) in pendingEntities.withIndex()) {
            val domainTx = entity.toDomain()
            var currentStatus = TransactionStatus.PENDING_SYNC

            // Step 1: Reconstruct Canonical Payload & Verify Signature
            val canonicalPayload = CryptoEngine.buildCanonicalPayload(
                buyerId = domainTx.buyerId,
                merchantId = domainTx.merchantId,
                amount = domainTx.amount,
                transactionId = domainTx.transactionId,
                nonce = domainTx.nonce,
                timestamp = domainTx.timestamp,
                mode = domainTx.mode.name,
                mandateReference = "MND-9823-XYZ"
            )

            val isSignatureValid = if (domainTx.isTampered) {
                false
            } else {
                CryptoEngine.verifySignature(
                    payload = canonicalPayload,
                    signatureBase64 = domainTx.signature,
                    publicKey = buyerKeyPair.public
                )
            }

            if (!isSignatureValid) {
                currentStatus = TransactionStatus.INVALID_SIGNATURE
                database.transactionDao().update(
                    TransactionEntity.fromDomain(domainTx.copy(status = currentStatus))
                )
                if (SupabaseClient.isConfigured()) {
                    supabaseTxRepo.uploadTransaction(domainTx.copy(status = currentStatus))
                }
                continue
            }
            verifiedSigCount++

            // Step 2: Nonce Replay Check (Local Room DB + Remote Supabase)
            val localNonceUsed = database.usedNonceDao().countNonce(domainTx.nonce) > 0
            val remoteNonceUsed = if (SupabaseClient.isConfigured()) {
                supabaseTxRepo.isNonceUsed(domainTx.nonce).getOrDefault(false)
            } else false

            if (localNonceUsed || remoteNonceUsed) {
                replayCount++
                currentStatus = TransactionStatus.REPLAY_DETECTED
                database.transactionDao().update(
                    TransactionEntity.fromDomain(domainTx.copy(status = currentStatus))
                )
                if (SupabaseClient.isConfigured()) {
                    supabaseTxRepo.uploadTransaction(domainTx.copy(status = currentStatus))
                }
                continue
            }
            database.usedNonceDao().insert(UsedNonceEntity(nonce = domainTx.nonce))
            if (SupabaseClient.isConfigured()) {
                supabaseTxRepo.recordUsedNonce(domainTx.nonce, domainTx.transactionId)
            }

            // Step 3: Duplicate Transaction ID Check
            if (domainTx.status == TransactionStatus.SETTLED) {
                dupCount++
                currentStatus = TransactionStatus.DUPLICATE
                database.transactionDao().update(
                    TransactionEntity.fromDomain(domainTx.copy(status = currentStatus))
                )
                continue
            }

            // Step 4: Upload Initial Record to Supabase (State: PENDING_SYNC)
            if (SupabaseClient.isConfigured()) {
                supabaseTxRepo.uploadTransaction(domainTx.copy(status = TransactionStatus.PENDING_SYNC))
            }

            // Step 5: Fraud & Anomaly ML Evaluation
            val eval = FraudDetector.evaluate(domainTx)
            val updatedWithFraud = domainTx.copy(
                fraudProbability = eval.fraudProbability,
                anomalyScore = eval.anomalyScore,
                fraudReasons = eval.reasons,
                status = TransactionStatus.FRAUD_CHECKED
            )

            // Step 6: Store Fraud Result & Alert in Supabase
            if (SupabaseClient.isConfigured()) {
                supabaseTxRepo.uploadFraudResult(
                    transactionId = domainTx.transactionId,
                    score = eval.fraudProbability.toDouble(),
                    anomalyScore = eval.anomalyScore.toDouble(),
                    reasons = eval.reasons
                )
                if (eval.severity == RiskSeverity.HIGH || eval.fraudProbability >= 0.70f) {
                    supabaseTxRepo.uploadFraudAlert(
                        RemoteFraudAlert(
                            id = UUID.randomUUID().toString(),
                            transactionId = domainTx.transactionId,
                            severity = eval.severity.name,
                            score = eval.fraudProbability.toDouble(),
                            reasons = eval.reasons,
                            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date()),
                            resolved = false
                        )
                    )
                }
            }

            // Step 7 & 8: High Risk Routing vs Authorized Settlement
            if (eval.severity == RiskSeverity.HIGH) {
                highRisk++
                reviewReq++
                flaggedId = domainTx.transactionId
                currentStatus = TransactionStatus.FRAUD_REVIEW

                val finalFraudTx = updatedWithFraud.copy(
                    status = currentStatus,
                    syncedAt = System.currentTimeMillis()
                )
                database.transactionDao().update(
                    TransactionEntity.fromDomain(finalFraudTx, isOfflineQueued = false)
                )
                if (SupabaseClient.isConfigured()) {
                    supabaseTxRepo.updateTransactionStatus(
                        transactionId = domainTx.transactionId,
                        status = currentStatus
                    )
                }
            } else {
                lowRisk++

                // Step 9 & 10: Settle via Razorpay Backend / Test-Mode
                val settlementResult = RazorpayService.executeSettlement(domainTx)
                settled++
                currentStatus = TransactionStatus.SETTLED

                // Step 11: Reduce/reconcile local offline exposure
                onBuyerExposureUpdated(domainTx.amount)

                val finalSettledTx = updatedWithFraud.copy(
                    status = currentStatus,
                    syncedAt = System.currentTimeMillis(),
                    settledAt = settlementResult.timestamp,
                    settlementRef = settlementResult.settlementRef
                )

                // Step 12: Update Room DB & Mark Synchronized
                database.transactionDao().update(
                    TransactionEntity.fromDomain(finalSettledTx, isOfflineQueued = false)
                )

                // Step 13: Update Supabase Status with Settlement Ref
                if (SupabaseClient.isConfigured()) {
                    supabaseTxRepo.updateTransactionStatus(
                        transactionId = domainTx.transactionId,
                        status = currentStatus,
                        settlementRef = settlementResult.settlementRef,
                        settledAt = settlementResult.timestamp
                    )
                }
            }

            val stepPercent = 20 + ((index + 1) * 75 / totalDiscovered)
            _syncState.value = _syncState.value.copy(
                progressPercent = stepPercent,
                currentStepText = "Verified & Synced to Cloud (${index + 1}/$totalDiscovered)",
                verifiedSignaturesCount = verifiedSigCount,
                replayAttackCount = replayCount,
                duplicateCount = dupCount,
                highRiskCount = highRisk,
                lowRiskCount = lowRisk,
                settledCount = settled,
                reviewRequiredCount = reviewReq,
                flaggedTransactionId = flaggedId
            )
            delay(200)
        }

        val completedState = SyncProgressState(
            isSyncing = false,
            progressPercent = 100,
            currentStepText = if (SupabaseClient.isConfigured()) "Reconciliation & Supabase Sync complete" else "Local Reconciliation & Settlement complete",
            discoveredCount = totalDiscovered,
            verifiedSignaturesCount = verifiedSigCount,
            replayAttackCount = replayCount,
            duplicateCount = dupCount,
            highRiskCount = highRisk,
            lowRiskCount = lowRisk,
            settledCount = settled,
            reviewRequiredCount = reviewReq,
            flaggedTransactionId = flaggedId,
            isComplete = true,
            supabaseSyncSuccess = SupabaseClient.isConfigured()
        )
        _syncState.value = completedState
        return completedState
    }

    fun resetState() {
        _syncState.value = SyncProgressState()
    }
}

