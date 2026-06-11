DROP INDEX IF EXISTS uq_batch_active_window;
CREATE UNIQUE INDEX uq_batch_active_window
    ON file_batch (window_code, reference_date, participant_id)
    WHERE status NOT IN ('REJECTED', 'REJECTED_CUTOFF');