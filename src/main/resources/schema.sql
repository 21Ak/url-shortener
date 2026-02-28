CREATE TABLE IF NOT EXISTS url_mappings (
    id              BIGSERIAL       PRIMARY KEY,
    short_code      VARCHAR(10)     NOT NULL UNIQUE,
    original_url    TEXT            NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMP       NULL,
    click_count     BIGINT          NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_short_code ON url_mappings (short_code);
CREATE INDEX IF NOT EXISTS idx_expires_at ON url_mappings (expires_at) WHERE expires_at IS NOT NULL;
