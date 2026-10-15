CREATE TABLE app_user (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name     TEXT NOT NULL,
    username      TEXT NOT NULL UNIQUE COLLATE NOCASE,
    email         TEXT NOT NULL UNIQUE COLLATE NOCASE,
    password_hash TEXT NOT NULL,
    role          TEXT NOT NULL,
    created_at    TEXT NOT NULL
);

-- token_hash is the SHA-256 of the bearer token; the raw token is never stored.
CREATE TABLE auth_session (
    token_hash TEXT PRIMARY KEY,
    user_id    INTEGER NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    expires_at TEXT NOT NULL
);
