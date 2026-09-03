-- ==========================================================
-- TRUSTPAY SUPABASE CLOUD POSTGRES SCHEMA & RLS POLICIES
-- ==========================================================

-- 1. Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. Users Table
CREATE TABLE IF NOT EXISTS public.users (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('BUYER', 'MERCHANT', 'ADMIN')),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. Device Public Key Registry
CREATE TABLE IF NOT EXISTS public.devices (
    id TEXT PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    user_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    public_key TEXT NOT NULL,
    key_algorithm TEXT DEFAULT 'Ed25519',
    trust_tier TEXT DEFAULT 'STANDARD',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT unique_user_key UNIQUE (user_id, public_key)
);

-- 4. Transactions Ledger
CREATE TABLE IF NOT EXISTS public.transactions (
    id TEXT PRIMARY KEY,
    buyer_id TEXT NOT NULL,
    merchant_id TEXT NOT NULL,
    amount BIGINT NOT NULL,
    mode TEXT NOT NULL CHECK (mode IN ('OFFLINE_VALUE', 'AUTHORIZATION')),
    status TEXT NOT NULL,
    nonce TEXT NOT NULL,
    payload TEXT NOT NULL,
    signature TEXT NOT NULL,
    fraud_score DOUBLE PRECISION DEFAULT 0.0,
    anomaly_score DOUBLE PRECISION DEFAULT 0.0,
    fraud_reasons JSONB DEFAULT '[]'::JSONB,
    created_at TIMESTAMPTZ NOT NULL,
    synced_at TIMESTAMPTZ,
    settled_at TIMESTAMPTZ,
    razorpay_payment_id TEXT,
    razorpay_settlement_id TEXT
);

-- 5. Used Nonces (Global Replay Protection)
CREATE TABLE IF NOT EXISTS public.used_nonces (
    nonce TEXT PRIMARY KEY,
    transaction_id TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. Fraud Alerts & Audits
CREATE TABLE IF NOT EXISTS public.fraud_alerts (
    id TEXT PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    transaction_id TEXT NOT NULL REFERENCES public.transactions(id) ON DELETE CASCADE,
    severity TEXT NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    score DOUBLE PRECISION NOT NULL,
    reasons JSONB DEFAULT '[]'::JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    resolved BOOLEAN DEFAULT FALSE
);

-- ==========================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ==========================================================

ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.devices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.used_nonces ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.fraud_alerts ENABLE ROW LEVEL SECURITY;

-- Users RLS: Users can read their own profile; Admins can read all
CREATE POLICY "Users view own profile or Admin"
ON public.users FOR SELECT
USING (auth.uid()::TEXT = id OR EXISTS (
    SELECT 1 FROM public.users u WHERE u.id = auth.uid()::TEXT AND u.role = 'ADMIN'
));

-- Devices RLS: Users manage their own keys; Public read for signature verification
CREATE POLICY "Public read for signature validation"
ON public.devices FOR SELECT
USING (true);

CREATE POLICY "Users register own device keys"
ON public.devices FOR INSERT
WITH CHECK (auth.uid()::TEXT = user_id OR auth.role() = 'anon');

-- Transactions RLS: Buyers and Merchants can view their own; Admins view all
CREATE POLICY "Transaction participant visibility"
ON public.transactions FOR SELECT
USING (
    buyer_id = auth.uid()::TEXT 
    OR merchant_id = auth.uid()::TEXT 
    OR auth.role() = 'anon'
    OR EXISTS (SELECT 1 FROM public.users u WHERE u.id = auth.uid()::TEXT AND u.role = 'ADMIN')
);

CREATE POLICY "Transactions insert for verified clients"
ON public.transactions FOR INSERT
WITH CHECK (true);

CREATE POLICY "Transactions update for sync engine"
ON public.transactions FOR UPDATE
USING (true);

-- Used Nonces RLS: Public read for distributed replay detection
CREATE POLICY "Allow global nonce check"
ON public.used_nonces FOR SELECT
USING (true);

CREATE POLICY "Allow nonce registration on sync"
ON public.used_nonces FOR INSERT
WITH CHECK (true);

-- Fraud Alerts RLS: Admin visibility & service creation
CREATE POLICY "Admin view fraud alerts"
ON public.fraud_alerts FOR SELECT
USING (true);

CREATE POLICY "Insert fraud alerts"
ON public.fraud_alerts FOR INSERT
WITH CHECK (true);

-- ==========================================================
-- STORED FUNCTION / RPC: get_admin_metrics()
-- ==========================================================

CREATE OR REPLACE FUNCTION public.get_admin_metrics()
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_total_tx BIGINT;
    v_total_vol BIGINT;
    v_settled_tx BIGINT;
    v_pending_tx BIGINT;
    v_fraud_tx BIGINT;
    v_offline_tx BIGINT;
    v_auth_tx BIGINT;
    v_fraud_rate DOUBLE PRECISION;
BEGIN
    SELECT COUNT(*), COALESCE(SUM(amount), 0)
    INTO v_total_tx, v_total_vol
    FROM public.transactions;

    SELECT COUNT(*)
    INTO v_settled_tx
    FROM public.transactions
    WHERE status = 'SETTLED';

    SELECT COUNT(*)
    INTO v_pending_tx
    FROM public.transactions
    WHERE status IN ('PENDING_SYNC', 'OFFLINE_ACCEPTED', 'FRAUD_CHECKED', 'SETTLEMENT_PENDING');

    SELECT COUNT(*)
    INTO v_fraud_tx
    FROM public.transactions
    WHERE status = 'FRAUD_REVIEW';

    SELECT COUNT(*)
    INTO v_offline_tx
    FROM public.transactions
    WHERE mode = 'OFFLINE_VALUE';

    SELECT COUNT(*)
    INTO v_auth_tx
    FROM public.transactions
    WHERE mode = 'AUTHORIZATION';

    IF v_total_tx > 0 THEN
        v_fraud_rate := (v_fraud_tx::DOUBLE PRECISION / v_total_tx::DOUBLE PRECISION) * 100.0;
    ELSE
        v_fraud_rate := 0.0;
    END IF;

    RETURN jsonb_build_object(
        'total_transactions', v_total_tx,
        'total_volume', v_total_vol,
        'settled_transactions', v_settled_tx,
        'pending_transactions', v_pending_tx,
        'fraud_transactions', v_fraud_tx,
        'fraud_rate', v_fraud_rate,
        'offline_transactions', v_offline_tx,
        'authorization_transactions', v_auth_tx
    );
END;
$$;
