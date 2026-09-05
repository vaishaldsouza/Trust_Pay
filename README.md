# 🔐 TrustPay

![Kotlin](https://img.shields.io/badge/Kotlin-1.9-purple)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue)
![Razorpay](https://img.shields.io/badge/Razorpay-Test%20Mode%20API-0C2451)
![Supabase](https://img.shields.io/badge/Supabase-Postgres%20%2B%20Auth-3ECF8E)
![XGBoost](https://img.shields.io/badge/XGBoost-Fraud%20Model-EC5F41)
![License](https://img.shields.io/badge/License-MIT-yellow)

**An offline-first payment resilience layer that keeps transactions moving when the internet doesn't — and never pretends a payment settled before it actually did.**

> "When the internet goes down, TrustPay keeps commerce moving — securely, intelligently, and without replacing the payment network."

---

## 📌 Overview

Millions of people in rural and low-connectivity areas lose access to digital payments the moment signal drops. TrustPay is a native Android prototype that lets a buyer and a merchant complete a bounded, cryptographically signed transaction with **zero internet connectivity**, then reconciles it through Razorpay's real settlement APIs the moment connectivity returns.

TrustPay does not create a new currency, does not custody funds, and does not claim a transaction is settled before Razorpay has actually confirmed it. It is a trust layer bounded by exposure limits and pre-authorized mandates — not a parallel payment system.

---

## 🚨 The Problem

- UPI and card payments require a live connection at the moment of transaction.
- Coastal markets, rural haats, transit corridors, and event venues routinely lose signal for minutes to hours.
- Merchants in dead zones either fall back to cash (losing the audit trail and inclusion benefits of digital payments) or lose the sale entirely.
- Existing offline-payment concepts either move real value offline (introducing double-spend risk) or require infrastructure most low-connectivity regions don't have.

---

## 💡 The Solution

TrustPay separates **authorization** (can happen offline, against a pre-existing mandate) from **settlement** (always happens online, always through Razorpay):

- **Real Razorpay UPI Autopay mandate**, created while online, capping how much a buyer can ever draw down.
- **Four real offline transport channels** — Bluetooth LE, Wi-Fi Direct, QR code, and ultrasonic audio — for exchanging a signed transaction locally, with no internet, no SMS, no cellular data.
- **Bounded, deterministic exposure limits**, enforced on-device, independent of AI — the same buyer can't overcommit across multiple offline transactions before syncing.
- **A Trust Agent** that decides — with a visible, explainable reason — whether to accept a transaction as Offline Value, route it to Authorization mode, or decline it.
- **Real settlement, honestly reported.** A transaction only ever shows `SETTLED` once Razorpay has actually confirmed the charge (via webhook or a captured payment response) — never on order creation alone.

---

## 🧠 Why This Isn't "Just a Wallet"

```
Wallet-Based Offline Payments          TrustPay's Approach

Buyer loads value offline              Buyer pre-authorizes a bounded
      │                                mandate while ONLINE
      ▼                                      │
Offline debit from local balance             ▼
      │                                Offline exchange is a SIGNED
      ▼                                AUTHORIZATION TOKEN, not stored value
"Double spending cannot be fully             │
prevented" (stated limitation)               ▼
                                        Worst case: a declined/partial
                                        settlement against a REAL mandate —
                                        never lost float, never a new
                                        custodial liability
```

No money or token of monetary value ever moves offline. Only a signed *intent to draw down* against an already-authorized Razorpay mandate does.

---

## ⚙️ Detailed System Flow

### 1. Online Setup (real Razorpay API call)
- Buyer creates a real Razorpay Customer + Order/token-backed mandate via the Recurring Payments API, capped at a maximum authorized amount.
- Merchant registers a standard receiving account.

### 2. Offline Transaction (no network required)
- Buyer and merchant devices connect over **BLE, Wi-Fi Direct, QR, or ultrasonic sound** (details below).
- The buyer's device builds a canonical transaction payload and signs it with **Ed25519**.
- The **Trust Agent** evaluates the request deterministically — mode (Offline Value vs. Authorization), remaining exposure, and any merchant risk signal — before the payload is even signed.
- The merchant verifies the signature locally and accepts or rejects the transaction into a pending queue.
- Status: `OFFLINE_ACCEPTED` → `PENDING_SYNC`.

### 3. Sync & Real Settlement (online again)
- Signature re-verified server-side, nonce checked against a replay table, duplicate transaction IDs rejected.
- A real HTTPS call executes the mandate drawdown against Razorpay's Recurring Payments API.
- **Strict status integrity**: `SETTLED` is only ever set once Razorpay confirms a captured payment (via webhook, HMAC-SHA256 verified) — never fabricated, never inferred from order creation alone. If the backend is unreachable or the mandate isn't yet authorized, the transaction shows `SETTLEMENT_PENDING` or `SETTLEMENT_FAILED` with the real reason, not a fake success.

### 4. Post-Transaction Fraud Analysis
- A local rule-based engine scores every synced transaction on velocity, amount deviation, exposure ratio, and sync delay.
- A separately deployed, **real trained XGBoost model** (validated on a labeled synthetic dataset with measured precision/recall on a held-out test set) provides a second, independent risk score — shown side-by-side with the rule-based score, never replacing it.
- Gemini AI explains *why* a transaction was flagged, in plain language, across four languages — Gemini never influences the actual accept/decline decision.

---

## 🏗️ System Architecture

```
                     Buyer Device                Merchant Device
                          │                             │
                 ┌────────┴────────┐           ┌────────┴────────┐
                 │  CryptoEngine    │           │  CryptoEngine    │
                 │  (Ed25519 sign)  │           │  (verify sig)    │
                 └────────┬────────┘           └────────┬────────┘
                          │                             │
        ┌─────────────────┼─────────────────────────────┼─────────────────┐
        ▼                 ▼                 ▼           ▼                 ▼
   Bluetooth LE      Wi-Fi Direct         QR Code    Ultrasonic       (any channel,
   (GATT + custom    (WifiP2pManager,     (ZXing +   Audio (FSK       either device
   service UUID)     DNS-SD, TCP)         CameraX)   over AudioTrack/ can send OR
                                                      AudioRecord)     receive — P2P)
        └─────────────────┴─────────────────┴───────────┴─────────────────┘
                                        │
                              Local Room DB (SQLite)
                          transactions, nonces, exposure
                                        │
                               ── connectivity returns ──
                                        ▼
                         SyncEngine (7-step reconciliation:
                    sig verify → nonce check → duplicate check →
                    exposure reconcile → fraud scoring → settlement)
                                        │
                ┌───────────────────────┼───────────────────────┐
                ▼                       ▼                       ▼
        Razorpay Backend         Fraud Model Service       Supabase
        Proxy (Node/Express,     (Python/FastAPI,          (Postgres + Auth,
        Render) — real mandate   trained XGBoost,          RLS-protected
        creation + settlement,   Render) — real             cloud ledger)
        HMAC webhook verified    fraud_probability
```

---

## 🌟 Key Features

**Security & Trust**
- Real Ed25519 asymmetric signing and verification for every offline transaction
- Deterministic nonce-based replay protection and duplicate transaction detection (database-enforced, not just application logic)
- Bounded offline exposure limits, enforced before signing — not a suggestion, a hard gate
- 6-digit PIN authorization gate before any signature is generated, backed by PBKDF2 hashing
- Live tamper-simulation toggle demonstrating real signature rejection on modified payloads

**Offline Transports**
- Bluetooth LE (custom GATT service, MTU negotiation, automatic retry on connection errors)
- Wi-Fi Direct (DNS-SD filtered peer discovery, dynamic group-owner negotiation, TCP data exchange)
- QR Code (real ZXing generation + CameraX live camera scanning)
- Ultrasonic Audio (BFSK modulation over 17.5–19.5kHz, Goertzel-algorithm demodulation, CRC-32 integrity check)
- Symmetric peer-to-peer: any account can send or receive, independent of account type

**Settlement & Finance**
- Real Razorpay test-mode mandate creation and recurring-payment settlement execution
- Honest, three-state settlement reporting: `SETTLED` / `SETTLEMENT_PENDING` / `SETTLEMENT_FAILED` — never a fabricated success
- HMAC-SHA256 verified webhook confirmation before any transaction is marked settled

**AI & Explainability**
- Dual fraud scoring: local rule-based heuristic engine + a separately trained, deployed XGBoost model with measured precision/recall
- Gemini-powered fraud explainability for compliance review, isolated from the actual decision path
- Unified multilingual conversational assistant (voice + text) for balance, exposure, and settlement queries — English, Hindi, Kannada, Malayalam — architecturally incapable of executing or influencing a payment

**Access & Inclusion**
- Real Supabase Auth (Create Account / Login), Row-Level Security protected
- Direct `*99#` NPCI USSD banking hand-off for feature-phone-style access, via the native Android dialer

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| App | Kotlin, Jetpack Compose, Material 3 |
| Local Storage | Android Room (SQLite) |
| Cryptography | Ed25519 (ECDSA P-256 fallback), Android Keystore |
| Offline Transports | BluetoothLeScanner/Advertiser, WifiP2pManager, ZXing + CameraX, AudioTrack/AudioRecord |
| Cloud Database & Auth | Supabase (Postgres + Auth + RLS) |
| Payments | Razorpay Test-Mode API (Orders, Recurring Payments, Webhooks) |
| Backend Proxy | Node.js + Express (Razorpay secret isolation), deployed on Render |
| Fraud ML | XGBoost, FastAPI, deployed as an independent service on Render |
| Conversational AI | Google Gemini (gemini-2.5-flash) |
| Voice | Android SpeechRecognizer + TextToSpeech |

---

## 📂 Project Structure

```
Razor/
├── app/src/main/java/com/example/
│   ├── crypto/
│   │   └── CryptoEngine.kt              # Ed25519 signing & verification
│   ├── engine/
│   │   ├── BluetoothPaymentEngine.kt
│   │   ├── WifiDirectPaymentEngine.kt
│   │   ├── QrPaymentEngine.kt
│   │   ├── UltrasonicEngine.kt
│   │   ├── TrustAgent.kt                # Deterministic accept/decline logic
│   │   ├── FraudDetector.kt             # Local rule-based scoring
│   │   ├── MlFraudEngine.kt             # Remote XGBoost model client
│   │   ├── RazorpayService.kt
│   │   ├── SyncEngine.kt                # 7-step reconciliation pipeline
│   │   ├── ChatbotEngine.kt
│   │   ├── VoiceAssistantEngine.kt
│   │   └── GeminiExplainabilityService.kt
│   ├── data/
│   │   ├── local/                       # Room DB, DAOs, entities
│   │   ├── model/                       # Domain models & enums
│   │   └── remote/                      # Supabase repositories
│   ├── security/
│   │   └── PinSecurityManager.kt        # PBKDF2-hashed PIN gate
│   └── ui/
│       ├── TrustPayViewModel.kt
│       ├── screens/                     # Buyer, Merchant, Admin, Auth, Settings
│       └── components/                  # Shared Compose components
├── backend/                              # Razorpay proxy (Node/Express)
│   └── server.js
└── fraud-service/                        # XGBoost model service (Python/FastAPI)
    └── fraud_service.py
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (Kotlin, Jetpack Compose)
- Two physical Android devices (BLE / Wi-Fi Direct peripheral advertising doesn't work on emulators)
- A free [Supabase](https://supabase.com) project
- A [Razorpay](https://razorpay.com) test-mode account
- A [Google AI Studio](https://aistudio.google.com) Gemini API key

### 1. Clone & Configure
```bash
git clone https://github.com/vaishaldsouza/Trust_Pay.git
cd Trust_Pay
```

Add to `local.properties`:
```properties
SUPABASE_URL=https://your-project-ref.supabase.co
SUPABASE_ANON_KEY=your_supabase_anon_key
RAZORPAY_KEY_ID=rzp_test_your_key_id
RAZORPAY_KEY_SECRET=your_razorpay_secret
GEMINI_API_KEY=your_gemini_api_key
```

### 2. Deploy the Backend Services
```bash
# Razorpay proxy
cd backend
npm install
# deploy to Render (Node runtime) with RAZORPAY_KEY_ID / RAZORPAY_KEY_SECRET as env vars

# Fraud model service
cd ../fraud-service
pip install -r requirements.txt
# deploy to Render (Python runtime)
```

### 3. Build & Run
Open in Android Studio, sync Gradle, run on two physical devices for the full offline-transport demo.

---

## 🔒 Security

- Razorpay and Supabase secrets are never bundled client-side in plaintext; the Razorpay secret lives only in the server-side proxy
- Row-Level Security enforced on all Supabase tables
- PIN stored as a PBKDF2 hash with per-user salt, never plaintext
- Nonces and transaction IDs are database-unique-constrained, not just checked in application logic
- Voice and chatbot layers have zero imports of, or references to, the cryptographic signing or payment execution code paths — verified, not just instructed

---

## ✅ Why TrustPay

- Solves a genuine market-access problem, not a simulated one — offline connectivity failure is real and common
- Every money action is bounded, gated, and explainable, with a real audit trail
- Settlement honesty is architectural, not aspirational: a transaction is `SETTLED` only when Razorpay has actually confirmed it
- Four real, independently working offline transports — not one mocked demo path
- Fraud detection validated with real precision/recall metrics, not a vague claim

---

## 🗺️ Roadmap

- [x] Ed25519-signed offline transactions across BLE, Wi-Fi Direct, QR, and Ultrasonic
- [x] Real Razorpay mandate creation and webhook-verified settlement
- [x] Dual rule-based + trained XGBoost fraud scoring
- [x] Symmetric peer-to-peer sending and receiving
- [x] PIN-gated transaction authorization
- [x] Multilingual conversational assistant (voice + text)
- [x] `*99#` USSD banking access for feature-phone users
- [ ] Production-grade KYC and RBI compliance review for mandate onboarding
- [ ] Biometric authorization as an alternative to PIN
- [ ] Merchant-side settlement analytics dashboard
- [ ] Expanded regional language support

---

## 📚 References

1. Razorpay — Recurring Payments & UPI Autopay documentation. https://razorpay.com/docs/payments/subscriptions/
2. Supabase — Postgres database, authentication, and row-level security. https://supabase.com/docs
3. Android Developers — Jetpack Compose, Bluetooth LE, Wi-Fi Direct, CameraX. https://developer.android.com
4. Google Gemini — generative AI platform. https://ai.google.dev
5. XGBoost — gradient boosting library. https://xgboost.readthedocs.io

---

## 👥 Team

| Name | Role |
|---|---|
| [Name] | Android Development & Cryptography |
| [Name] | Backend & Payment Integration |
| [Name] | Fraud ML & AI Integration |
| [Name] | UI/UX & Presentation |

---

## 📄 License

This project is licensed under the MIT License.

*"Offline is not a failure state. It's a condition to design for."*

