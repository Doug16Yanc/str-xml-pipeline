DROP INDEX idx_batch_window_date;
CREATE INDEX idx_batch_window_date_participant ON file_batch (window_code, reference_date, participant_id, status);