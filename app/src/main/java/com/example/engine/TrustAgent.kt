package com.example.engine

import com.example.data.model.Buyer
import com.example.data.model.TransactionMode

enum class ModeSelectorChoice {
    AUTO,
    OFFLINE,
    AUTH
}

data class TrustDecision(
    val isApproved: Boolean,
    val selectedMode: TransactionMode,
    val ruleTriggered: String,
    val reason: String,
    val subReason: String,
    val availableExposure: Long,
    val requestedAmount: Long,
    val exceededBy: Long = 0L,
    val mandateRequired: Boolean = false,
    val mandateValid: Boolean = true
)

object TrustAgent {
    /**
     * Deterministic Trust Agent decision engine.
     * Enforces bounded offline exposure and hybrid mode routing.
     */
    fun evaluate(
        amount: Long,
        modeChoice: ModeSelectorChoice,
        buyer: Buyer,
        isNetworkOnline: Boolean
    ): TrustDecision {
        val availableExposure = buyer.availableExposure

        if (buyer.isRestricted) {
            return TrustDecision(
                isApproved = false,
                selectedMode = TransactionMode.OFFLINE_VALUE,
                ruleTriggered = "ACCOUNT_RESTRICTED",
                reason = "Account Offline Access Restricted",
                subReason = "An administrator has temporarily restricted offline transactions on this device due to risk flags.",
                availableExposure = availableExposure,
                requestedAmount = amount,
                exceededBy = amount
            )
        }

        if (amount <= 0) {
            return TrustDecision(
                isApproved = false,
                selectedMode = TransactionMode.OFFLINE_VALUE,
                ruleTriggered = "INVALID_AMOUNT",
                reason = "Invalid transaction amount",
                subReason = "Please enter an amount greater than ₹0.",
                availableExposure = availableExposure,
                requestedAmount = amount
            )
        }

        val hasValidMandate = buyer.mandateReference.isNotBlank()

        when (modeChoice) {
            ModeSelectorChoice.AUTO -> {
                return if (amount <= availableExposure) {
                    TrustDecision(
                        isApproved = true,
                        selectedMode = TransactionMode.OFFLINE_VALUE,
                        ruleTriggered = "BOUNDED_ALLOWANCE_SATISFIED",
                        reason = "Offline Value Mode selected",
                        subReason = "₹$amount is within your available offline allowance of ₹$availableExposure.",
                        availableExposure = availableExposure,
                        requestedAmount = amount
                    )
                } else if (hasValidMandate) {
                    TrustDecision(
                        isApproved = true,
                        selectedMode = TransactionMode.AUTHORIZATION,
                        ruleTriggered = "MANDATE_BACKED_AUTHORIZATION",
                        reason = "Authorization Mode selected",
                        subReason = "₹$amount exceeds offline allowance (₹$availableExposure). Payment will use Authorization Mode backed by Razorpay mandate ${buyer.mandateReference}.",
                        availableExposure = availableExposure,
                        requestedAmount = amount,
                        mandateRequired = true,
                        mandateValid = true
                    )
                } else {
                    TrustDecision(
                        isApproved = false,
                        selectedMode = TransactionMode.OFFLINE_VALUE,
                        ruleTriggered = "OFFLINE_LIMIT_EXCEEDED",
                        reason = "Offline spending allowance exceeded",
                        subReason = "Requested ₹$amount exceeds available allowance of ₹$availableExposure, and no active UPI mandate is registered.",
                        availableExposure = availableExposure,
                        requestedAmount = amount,
                        exceededBy = amount - availableExposure,
                        mandateRequired = true,
                        mandateValid = false
                    )
                }
            }

            ModeSelectorChoice.OFFLINE -> {
                return if (amount <= availableExposure) {
                    TrustDecision(
                        isApproved = true,
                        selectedMode = TransactionMode.OFFLINE_VALUE,
                        ruleTriggered = "OFFLINE_VALUE_APPROVED",
                        reason = "Offline Value Mode confirmed",
                        subReason = "Direct offline value transaction bounded by available allowance (₹$availableExposure).",
                        availableExposure = availableExposure,
                        requestedAmount = amount
                    )
                } else {
                    TrustDecision(
                        isApproved = false,
                        selectedMode = TransactionMode.OFFLINE_VALUE,
                        ruleTriggered = "OFFLINE_LIMIT_EXCEEDED",
                        reason = "Offline spending allowance exceeded",
                        subReason = "Requested ₹$amount exceeds your available offline allowance of ₹$availableExposure.",
                        availableExposure = availableExposure,
                        requestedAmount = amount,
                        exceededBy = amount - availableExposure
                    )
                }
            }

            ModeSelectorChoice.AUTH -> {
                return if (hasValidMandate) {
                    TrustDecision(
                        isApproved = true,
                        selectedMode = TransactionMode.AUTHORIZATION,
                        ruleTriggered = "EXPLICIT_AUTHORIZATION_MODE",
                        reason = "Authorization Mode confirmed",
                        subReason = "Bounded transaction backed by pre-authorized Razorpay mandate ${buyer.mandateReference}.",
                        availableExposure = availableExposure,
                        requestedAmount = amount,
                        mandateRequired = true,
                        mandateValid = true
                    )
                } else {
                    TrustDecision(
                        isApproved = false,
                        selectedMode = TransactionMode.AUTHORIZATION,
                        ruleTriggered = "MISSING_MANDATE",
                        reason = "Mandate authorization missing",
                        subReason = "Authorization mode requires an active Razorpay UPI Autopay mandate.",
                        availableExposure = availableExposure,
                        requestedAmount = amount,
                        mandateRequired = true,
                        mandateValid = false
                    )
                }
            }
        }
    }
}
