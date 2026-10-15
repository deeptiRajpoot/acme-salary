-- token_hash is the SHA-256 of the invite token; the raw token only exists in the invite link.
CREATE TABLE invite (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    token_hash TEXT NOT NULL UNIQUE,
    email      TEXT NOT NULL COLLATE NOCASE,
    role       TEXT NOT NULL,
    created_by INTEGER NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    expires_at TEXT NOT NULL,
    used_at    TEXT
);
