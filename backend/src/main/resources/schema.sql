-- =============================================================================
-- NEXUS TICKETING — Schema (idempotent CREATE IF NOT EXISTS)
-- =============================================================================

-- ----------------------------------------------------------------------------
-- 1. SHIFT HOURS  (must exist before employees references it)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS shift_hours (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    shift_name  VARCHAR(100) NOT NULL,
    start_time  TIME         NOT NULL,
    end_time    TIME         NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 2. EMPLOYEES  (merged Employee + User)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS employees (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN','PROJECT_MANAGER','EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE',
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    employee_name VARCHAR(100) NOT NULL,
    designation   VARCHAR(100),
    department    VARCHAR(100),
    status        ENUM('ACTIVE','INACTIVE','ON_LEAVE') NOT NULL DEFAULT 'ACTIVE',
    shift_id      BIGINT       NULL,
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_emp_shift FOREIGN KEY (shift_id) REFERENCES shift_hours(id) ON DELETE SET NULL
);

-- ----------------------------------------------------------------------------
-- 3. PROJECTS
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS projects (
    id             BIGINT      AUTO_INCREMENT PRIMARY KEY,
    project_code   VARCHAR(30) NOT NULL UNIQUE,
    project_name   VARCHAR(100) NOT NULL,
    description    TEXT,
    project_status ENUM('ACTIVE','ON_HOLD','COMPLETED','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    start_date     DATE,
    end_date       DATE,
    created_at     DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 4. EMPLOYEE ↔ PROJECT  (Many-to-Many)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS employee_projects (
    employee_id BIGINT NOT NULL,
    project_id  BIGINT NOT NULL,
    PRIMARY KEY (employee_id, project_id),
    CONSTRAINT fk_ep_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_ep_project  FOREIGN KEY (project_id)  REFERENCES projects(id)  ON DELETE CASCADE
);

-- ----------------------------------------------------------------------------
-- 5. TICKETS
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tickets (
    id                   BIGINT      AUTO_INCREMENT PRIMARY KEY,
    ticket_id            VARCHAR(20) NOT NULL UNIQUE,
    project_assignment   VARCHAR(100) NOT NULL,
    issue_description    TEXT        NOT NULL,
    assigned_employee    VARCHAR(100),
    support_level        ENUM('L1','L2','L3') NOT NULL,
    priority             ENUM('P1_CRITICAL','P2_HIGH','P3_MEDIUM','P4_LOW') NOT NULL,
    generation_date_time DATETIME,
    response_date_time   DATETIME,
    resolution_time      DATETIME,
    current_status       ENUM('OPEN','IN_PROGRESS','RESOLVED','CLOSED') NOT NULL DEFAULT 'OPEN',
    resolution_details   TEXT,
    remarks              TEXT,
    created_at           DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 6. HOLIDAYS  (weekends excluded automatically in code)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS holidays (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    holiday_name VARCHAR(150) NOT NULL,
    holiday_date DATE         NOT NULL UNIQUE,
    description  TEXT,
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 7. SLA CONFIG
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sla_config (
    id                    BIGINT       AUTO_INCREMENT PRIMARY KEY,
    priority              VARCHAR(20)  NOT NULL,
    support_level         VARCHAR(10)  NOT NULL,
    response_time_hours   DOUBLE       NOT NULL,
    resolution_time_hours DOUBLE       NOT NULL,
    is_active             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at            DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_sla_priority_level (priority, support_level)
);

-- -- ----------------------------------------------------------------------------
-- -- 8. Migration: add shift_id column to existing employees table if missing
-- -- ----------------------------------------------------------------------------
-- ALTER TABLE employees
--     ADD COLUMN shift_id BIGINT NULL;

-- -- Add FK only if it doesn't already exist (MySQL 8+ supports IF NOT EXISTS on FK)
-- -- For safety we use a stored procedure approach
-- DROP PROCEDURE IF EXISTS add_shift_fk;
-- DELIMITER $$
-- CREATE PROCEDURE add_shift_fk()
-- BEGIN
--   IF NOT EXISTS (
--     SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
--     WHERE CONSTRAINT_SCHEMA = DATABASE()
--       AND TABLE_NAME = 'employees'
--       AND CONSTRAINT_NAME = 'fk_emp_shift'
--       AND CONSTRAINT_TYPE = 'FOREIGN KEY'
--   ) THEN
--     ALTER TABLE employees
--       ADD CONSTRAINT fk_emp_shift FOREIGN KEY (shift_id) REFERENCES shift_hours(id) ON DELETE SET NULL;
--   END IF;
-- END$$
-- DELIMITER ;
-- CALL add_shift_fk();
-- DROP PROCEDURE IF EXISTS add_shift_fk;
