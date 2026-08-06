-- ── Terceira Milha: saga de compensação ─────────────────────────────────────
-- Evolui o BatchStatus para o fluxo completo:
--   PENDING -> EMITTED -> TRANSMITTED -> ACCEPTED (terminal)
--                                      \-> TRANSMISSION_REJECTED -> COMPENSATING -> COMPENSATED (terminal)
--
-- Existing rows: CONFIRMED -> ACCEPTED, REJECTED -> TRANSMISSION_REJECTED.
-- (No non-terminal EMITTED-only rows need remapping — EMITTED stays EMITTED.)

-- file_batch.status was VARCHAR(20) — 'TRANSMISSION_REJECTED' is 22 chars,
-- doesn't fit. Widen first, matching window_code's VARCHAR(30) convention.
ALTER TABLE file_batch ALTER COLUMN status TYPE VARCHAR(30);

UPDATE file_batch SET status = 'ACCEPTED'              WHERE status = 'CONFIRMED';
UPDATE file_batch SET status = 'TRANSMISSION_REJECTED' WHERE status = 'REJECTED';

ALTER TABLE file_batch DROP CONSTRAINT ck_batch_status;
ALTER TABLE file_batch ADD CONSTRAINT ck_batch_status CHECK (status IN (
    'PENDING', 'EMITTED', 'TRANSMITTED', 'ACCEPTED', 'TRANSMISSION_REJECTED', 'COMPENSATING', 'COMPENSATED'
));

-- Optimistic-lock version — doubles as the saga's audit trail (no orchestrator).
ALTER TABLE file_batch ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- A batch that failed transmission or already finished compensating no longer
-- occupies the window+date+participant slot — a new batch can be assembled
-- for the next cycle once orders are released.
DROP INDEX IF EXISTS uq_batch_active_window;
CREATE UNIQUE INDEX uq_batch_active_window
    ON file_batch (window_code, reference_date, participant_id)
    WHERE status NOT IN ('TRANSMISSION_REJECTED', 'COMPENSATING', 'COMPENSATED');

-- ── settlement_order: fix pre-existing CONFIRMED/ACCEPTED mismatch ──────────
-- OrderStatus.CONFIRMED never actually matched this CHECK constraint (only
-- 'ACCEPTED' was allowed) — an ACCEPTED return would have violated it at
-- runtime. Renaming the Kotlin enum to ACCEPTED fixes the bug; this migration
-- just needs to allow REJECTED orders to transition back to PENDING when
-- released by the compensating consumer (batch_id cleared in application code).

UPDATE settlement_order SET status = 'ACCEPTED' WHERE status = 'CONFIRMED';

COMMENT ON COLUMN file_batch.version IS
    'Optimistic-lock counter. Together with generated_at/sent_at forms the '
    'saga''s audit trail — choreography consumers react to status changes, '
    'no orchestrator table needed.';