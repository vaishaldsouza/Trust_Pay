package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.crypto.CryptoEngine
import com.example.data.model.Buyer
import com.example.data.model.RiskSeverity
import com.example.data.model.Transaction
import com.example.data.model.TransactionMode
import com.example.data.model.TransactionStatus
import com.example.engine.FraudDetector
import com.example.engine.ModeSelectorChoice
import com.example.engine.TrustAgent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TrustPay", appName)
  }

  @Test
  fun `cryptographic signing and verification succeeds and fails on tampering`() {
    val keyPair = CryptoEngine.generateKeyPair()
    val payload = CryptoEngine.buildCanonicalPayload(
      buyerId = "dev_buyer_01",
      merchantId = "merch_42",
      amount = 150L,
      transactionId = "TXN-TEST-01",
      nonce = "NONCE-12345",
      timestamp = 1700000000L,
      mode = "OFFLINE_VALUE",
      mandateReference = "MND-9823-XYZ"
    )

    val signature = CryptoEngine.signPayload(payload, keyPair.private)
    val isValid = CryptoEngine.verifySignature(payload, signature, keyPair.public)
    assertTrue("Signature must be cryptographically valid", isValid)

    val tamperedPayload = payload.replace("150", "999")
    val isTamperedValid = CryptoEngine.verifySignature(tamperedPayload, signature, keyPair.public)
    assertFalse("Tampered payload must fail cryptographic verification", isTamperedValid)
  }

  @Test
  fun `trust agent enforces deterministic offline allowance and modes`() {
    val buyer = Buyer(
      userId = "dev_buyer_01",
      offlineLimit = 500L,
      offlineExposure = 180L, // Available = 320
      mandateReference = "MND-9823-XYZ",
      maxMandateMonthly = 2000L
    )

    // Case 1: Under allowance offline -> Approved with OFFLINE_VALUE
    val decision1 = TrustAgent.evaluate(
      amount = 150L,
      modeChoice = ModeSelectorChoice.AUTO,
      buyer = buyer,
      isNetworkOnline = false
    )
    assertTrue(decision1.isApproved)
    assertEquals(TransactionMode.OFFLINE_VALUE, decision1.selectedMode)

    // Case 2: Exceeds allowance in OFFLINE mode -> Rejected deterministically (Image 21)
    val decision2 = TrustAgent.evaluate(
      amount = 400L,
      modeChoice = ModeSelectorChoice.OFFLINE,
      buyer = buyer,
      isNetworkOnline = false
    )
    assertFalse("Exceeding offline allowance in OFFLINE mode must be rejected", decision2.isApproved)
    assertEquals("OFFLINE_LIMIT_EXCEEDED", decision2.ruleTriggered)

    // Case 3: Larger amount in AUTO mode with active mandate -> AUTHORIZATION mode
    val decision3 = TrustAgent.evaluate(
      amount = 800L,
      modeChoice = ModeSelectorChoice.AUTO,
      buyer = buyer,
      isNetworkOnline = true
    )
    assertTrue(decision3.isApproved)
    assertEquals(TransactionMode.AUTHORIZATION, decision3.selectedMode)
  }

  @Test
  fun `fraud detector identifies velocity and high exposure spikes`() {
    val suspiciousTx = Transaction(
      transactionId = "TXN-ANOMALY-01",
      buyerId = "dev_buyer_01",
      buyerName = "Ganesh",
      merchantId = "merch_gold_99",
      merchantName = "Royal Jewelry & Luxury",
      amount = 89000L,
      currency = "INR",
      mode = TransactionMode.AUTHORIZATION,
      timestamp = System.currentTimeMillis(),
      nonce = "NC-89000",
      signature = "mock_sig",
      status = TransactionStatus.PENDING_SYNC
    )

    val evaluation = FraudDetector.evaluate(suspiciousTx)
    assertEquals(RiskSeverity.HIGH, evaluation.severity)
    assertTrue(evaluation.fraudProbability > 0.5f)
    assertTrue(evaluation.reasons.isNotEmpty())
  }
}
