-- Keep pgvector extension available for any local Postgres instance.
CREATE EXTENSION IF NOT EXISTS vector;

-- Used by seed data.sql files (e.g. hub/promotion) for gen_random_uuid().
CREATE EXTENSION IF NOT EXISTS pgcrypto;
