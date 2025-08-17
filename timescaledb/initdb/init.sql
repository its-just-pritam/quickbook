-- init.sql: creates schema and hypertable for operations (ops)
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

CREATE TABLE IF NOT EXISTS ops (
  doc_id TEXT NOT NULL,
  user_id TEXT NOT NULL,
  ts TIMESTAMPTZ NOT NULL DEFAULT now(),
  op_type TEXT NOT NULL, -- e.g., 'insert','delete'
  position BIGINT,       -- optional position in doc (if available)
  content TEXT,           -- the inserted character/word
  PRIMARY KEY (doc_id, user_id, ts)
);

-- Create hypertable on ts
SELECT create_hypertable('ops', 'ts', if_not_exists => TRUE);

-- Indexes for fast lookups
CREATE INDEX IF NOT EXISTS idx_ops_doc_ts ON ops (doc_id, ts DESC);
CREATE INDEX IF NOT EXISTS idx_ops_user_ts ON ops (user_id, ts DESC);
