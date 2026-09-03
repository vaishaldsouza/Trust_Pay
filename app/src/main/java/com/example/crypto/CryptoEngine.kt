package com.example.crypto

import android.util.Base64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Signature
import java.util.UUID

object CryptoEngine {
    private const val ALGORITHM = "Ed25519"
    private const val FALLBACK_ALGORITHM = "SHA256withECDSA"
    private const val FALLBACK_KEY_GEN = "EC"

    private var cachedKeyPair: KeyPair? = null
    private var isEd25519Supported: Boolean = true

    init {
        try {
            val kpg = KeyPairGenerator.getInstance(ALGORITHM)
            cachedKeyPair = kpg.generateKeyPair()
            isEd25519Supported = true
        } catch (e: Exception) {
            try {
                val kpg = KeyPairGenerator.getInstance(FALLBACK_KEY_GEN)
                kpg.initialize(256)
                cachedKeyPair = kpg.generateKeyPair()
                isEd25519Supported = false
            } catch (ex: Exception) {
                isEd25519Supported = false
            }
        }
    }

    fun getAlgorithmName(): String = if (isEd25519Supported) "Ed25519" else "ECDSA (P-256)"

    fun getOrCreateBuyerKeyPair(): KeyPair {
        cachedKeyPair?.let { return it }
        val pair = generateKeyPair()
        cachedKeyPair = pair
        return pair
    }

    fun generateKeyPair(): KeyPair {
        return try {
            val kpg = KeyPairGenerator.getInstance(ALGORITHM)
            kpg.generateKeyPair()
        } catch (e: Exception) {
            val kpg = KeyPairGenerator.getInstance(FALLBACK_KEY_GEN)
            kpg.initialize(256)
            kpg.generateKeyPair()
        }
    }

    fun generateNonce(): String {
        return "NC-${UUID.randomUUID().toString().replace("-", "").take(10).uppercase()}"
    }

    fun buildCanonicalPayload(
        buyerId: String,
        merchantId: String,
        amount: Long,
        transactionId: String,
        nonce: String,
        timestamp: Long,
        mode: String,
        mandateReference: String
    ): String {
        return "$buyerId|$merchantId|$amount|$transactionId|$nonce|$timestamp|$mode|$mandateReference"
    }

    fun signPayload(payload: String, privateKey: PrivateKey): String {
        val algorithm = if (isEd25519Supported) ALGORITHM else FALLBACK_ALGORITHM
        val sig = Signature.getInstance(algorithm)
        sig.initSign(privateKey)
        sig.update(payload.toByteArray(Charsets.UTF_8))
        val signatureBytes = sig.sign()
        return Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
    }

    fun verifySignature(payload: String, signatureBase64: String, publicKey: PublicKey): Boolean {
        return try {
            val algorithm = if (isEd25519Supported) ALGORITHM else FALLBACK_ALGORITHM
            val sig = Signature.getInstance(algorithm)
            sig.initVerify(publicKey)
            sig.update(payload.toByteArray(Charsets.UTF_8))
            val sigBytes = Base64.decode(signatureBase64, Base64.NO_WRAP)
            sig.verify(sigBytes)
        } catch (e: Exception) {
            false
        }
    }

    fun exportPublicKey(publicKey: PublicKey): String {
        return "ED25519-PUB-" + Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP).take(24)
    }

    fun getPublicKeyBase64(publicKey: PublicKey): String {
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }
}
