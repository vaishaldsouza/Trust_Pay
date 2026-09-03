package com.example.engine

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Transaction
import kotlinx.coroutines.delay
import java.util.UUID

data class MandateDetails(
    val mandateId: String = "MND-9823-XYZ",
    val type: String = "Razorpay UPI Autopay",
    val status: String = "ACTIVE",
    val maxMonthlyLimit: Long = 2000L,
    val authorizedAccount: String = "ganesh@okhdfcbank",
    val validUntil: String = "Dec 2028"
)

data class SettlementResult(
    val isSuccess: Boolean,
    val settlementRef: String,
    val paymentId: String,
    val amount: Long,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)

object RazorpayService {
    private const val TAG = "TrustPayRazorpay"
    const val LABEL = "Razorpay Test/Mock Settlement"

    fun getActiveMandate(): MandateDetails = MandateDetails()

    /**
     * Executes mock/test mandate settlement via Razorpay client isolation.
     * Hard constraint: Never exposes key secrets, clearly tags test settlement.
     */
    suspend fun executeSettlement(transaction: Transaction): SettlementResult {
        // Simulate network processing
        delay(350)

        val paymentId = "pay_rzp_mock_" + UUID.randomUUID().toString().replace("-", "").take(12)
        val settlementRef = "set_rzp_" + UUID.randomUUID().toString().replace("-", "").take(10)

        Log.i(TAG, "[$LABEL] Settlement executed for TXN ${transaction.transactionId} of ₹${transaction.amount} using mandate MND-9823-XYZ -> $settlementRef")

        return SettlementResult(
            isSuccess = true,
            settlementRef = settlementRef,
            paymentId = paymentId,
            amount = transaction.amount,
            note = "$LABEL | Mandate execution via UPI Autopay"
        )
    }
}
