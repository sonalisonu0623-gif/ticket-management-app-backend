-- =============================================================================
-- DATA MIGRATION SCRIPT
-- Migrates existing employees + users into the merged employees table.
-- Run ONCE after deploying the new schema.
-- =============================================================================

-- STEP 1: If you had a separate `users` table, merge user auth data into `employees`.
-- This example assumes the old tables are still present as `employees_old` / `users_old`.
-- Adjust column names to match your original schema if different.

/*
-- Migrate existing user accounts that don't yet have an employee record
INSERT INTO employees (username, email, password, role, is_active, employee_name, status, created_at, updated_at)
SELECT
    u.username,
    u.email,
    u.password,
    u.role,
    u.is_active,
    COALESCE(e.employee_name, u.username),   -- use employee name if available
    COALESCE(e.status, 'ACTIVE'),
    u.created_at,
    u.updated_at
FROM users_old u
LEFT JOIN employees_old e ON LOWER(e.email) = LOWER(u.email)
WHERE NOT EXISTS (
    SELECT 1 FROM employees WHERE email = u.email
);
*/

-- STEP 2: Populate employee_projects from any existing project_employee records
/*
INSERT IGNORE INTO employee_projects (employee_id, project_id)
SELECT emp.id, p.id
FROM employees emp
JOIN employees_old old_emp ON old_emp.email = emp.email
JOIN project_assignments pa ON pa.employee_id = old_emp.id
JOIN projects p ON p.project_name = pa.project_name;
*/

-- STEP 3: Seed default shift hours if not already present
INSERT IGNORE INTO shift_hours (shift_name, start_time, end_time, is_active)
SELECT 'Standard Shift', '09:00:00', '18:00:00', 1
WHERE NOT EXISTS (SELECT 1 FROM shift_hours LIMIT 1);

-- STEP 4: Seed default SLA configs if not already present
INSERT IGNORE INTO sla_config (priority, support_level, response_time_hours, resolution_time_hours, is_active)
VALUES
    ('P1_CRITICAL', 'L1', 1,   4,  1),
    ('P1_CRITICAL', 'L2', 0.5, 2,  1),
    ('P1_CRITICAL', 'L3', 0.5, 1,  1),
    ('P2_HIGH',     'L1', 2,   8,  1),
    ('P2_HIGH',     'L2', 1,   4,  1),
    ('P2_HIGH',     'L3', 1,   3,  1),
    ('P3_MEDIUM',   'L1', 4,   24, 1),
    ('P3_MEDIUM',   'L2', 2,   16, 1),
    ('P3_MEDIUM',   'L3', 2,   12, 1),
    ('P4_LOW',      'L1', 8,   48, 1),
    ('P4_LOW',      'L2', 4,   32, 1),
    ('P4_LOW',      'L3', 4,   24, 1);

-- STEP 5: Drop old tables (ONLY after verifying data integrity)
-- DROP TABLE IF EXISTS users_old;
-- DROP TABLE IF EXISTS employees_old;
