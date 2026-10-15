CREATE TABLE country (
    code     TEXT PRIMARY KEY,
    name     TEXT NOT NULL,
    currency TEXT NOT NULL
);

CREATE TABLE department (
    name TEXT PRIMARY KEY
);

CREATE TABLE job_level (
    code       TEXT PRIMARY KEY,
    sort_order INTEGER NOT NULL
);

-- Static rates: 1 unit of currency = rate_to_usd USD
CREATE TABLE currency_rate (
    currency    TEXT PRIMARY KEY,
    rate_to_usd REAL NOT NULL
);

CREATE TABLE employee (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT NOT NULL,
    last_name  TEXT NOT NULL,
    email      TEXT NOT NULL,
    country    TEXT NOT NULL,
    department TEXT NOT NULL,
    job_level  TEXT NOT NULL,
    job_title  TEXT NOT NULL,
    gender     TEXT NOT NULL CHECK (gender IN ('F', 'M', 'X')),
    hire_date  TEXT NOT NULL,
    salary     REAL NOT NULL,
    currency   TEXT NOT NULL,
    salary_usd REAL NOT NULL
);

CREATE UNIQUE INDEX ux_employee_email ON employee (email);
CREATE INDEX ix_employee_last_name ON employee (last_name, id);
CREATE INDEX ix_employee_country ON employee (country);
CREATE INDEX ix_employee_department ON employee (department);
CREATE INDEX ix_employee_level ON employee (job_level);
CREATE INDEX ix_employee_salary_usd ON employee (salary_usd);

-- Append-only audit trail: one row per create and per salary change
CREATE TABLE salary_history (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL REFERENCES employee (id) ON DELETE CASCADE,
    old_salary  REAL,
    new_salary  REAL NOT NULL,
    currency    TEXT NOT NULL,
    reason      TEXT NOT NULL,
    changed_by  TEXT NOT NULL,
    changed_at  TEXT NOT NULL
);

CREATE INDEX ix_salary_history_employee ON salary_history (employee_id, id);
