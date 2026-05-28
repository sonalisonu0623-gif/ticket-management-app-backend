-- ============================================================
-- TicketOps Enterprise — Full Schema & Seed Data
-- Compatible with MySQL 8+
-- ============================================================

-- Drop in dependency order
DROP TABLE IF EXISTS sla_configs;
DROP TABLE IF EXISTS employee_projects;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS shifts;
DROP TABLE IF EXISTS users;

-- ── users ────────────────────────────────────────────────────
CREATE TABLE users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(30)  NOT NULL DEFAULT 'USER',
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- ── shifts ───────────────────────────────────────────────────
CREATE TABLE shifts (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_name   VARCHAR(100) NOT NULL UNIQUE,
    start_time   VARCHAR(10)  NOT NULL,
    end_time     VARCHAR(10)  NOT NULL,
    working_days VARCHAR(200),
    timezone     VARCHAR(60)  DEFAULT 'Asia/Kolkata'
);

-- ── projects ─────────────────────────────────────────────────
CREATE TABLE projects (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_name VARCHAR(150) NOT NULL UNIQUE,
    project_code VARCHAR(30)  UNIQUE,
    description  TEXT,
    support_email VARCHAR(150),
    sla_hours    INT          DEFAULT 24,
    shift_timing VARCHAR(50),
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ── employees ────────────────────────────────────────────────
CREATE TABLE employees (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id   VARCHAR(30)  UNIQUE,
    employee_name VARCHAR(150) NOT NULL,
    email         VARCHAR(150) UNIQUE,
    support_level VARCHAR(10),
    role          VARCHAR(30)  DEFAULT 'L1_SUPPORT',
    designation   VARCHAR(100),
    shift         VARCHAR(50),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ── employee_projects (join table) ───────────────────────────
CREATE TABLE employee_projects (
    employee_id BIGINT NOT NULL,
    project_id  BIGINT NOT NULL,
    PRIMARY KEY (employee_id, project_id),
    CONSTRAINT fk_ep_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_ep_project  FOREIGN KEY (project_id)  REFERENCES projects(id)  ON DELETE CASCADE
);

-- ── sla_configs ──────────────────────────────────────────────
CREATE TABLE sla_configs (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id          BIGINT      NOT NULL,
    priority_level      VARCHAR(30) NOT NULL,
    response_time_sla   INT         NOT NULL DEFAULT 4,
    resolution_time_sla INT         NOT NULL DEFAULT 24,
    escalation_time_sla INT                  DEFAULT 8,
    UNIQUE (project_id, priority_level),
    CONSTRAINT fk_sla_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- ── tickets ──────────────────────────────────────────────────
CREATE TABLE tickets (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_number          VARCHAR(30)  NOT NULL UNIQUE,
    project_id             BIGINT       NOT NULL,
    issue_description      TEXT         NOT NULL,
    assigned_employee_id   BIGINT,
    support_level          VARCHAR(10),
    priority               VARCHAR(30),
    generation_datetime    DATETIME,
    response_datetime      DATETIME,
    resolution_time        VARCHAR(50),
    business_hours_elapsed INT          DEFAULT 0,
    current_status         VARCHAR(30)  DEFAULT 'Open',
    resolution_details     TEXT,
    remarks                TEXT,
    sla_breached           BOOLEAN      DEFAULT FALSE,
    created_at             DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_ticket_project  FOREIGN KEY (project_id)           REFERENCES projects(id),
    CONSTRAINT fk_ticket_employee FOREIGN KEY (assigned_employee_id) REFERENCES employees(id),

    INDEX idx_ticket_project  (project_id),
    INDEX idx_ticket_employee (assigned_employee_id),
    INDEX idx_ticket_status   (current_status),
    INDEX idx_ticket_priority (priority),
    INDEX idx_ticket_sla      (sla_breached)
);


-- ============================================================
-- SEED DATA
-- ============================================================

-- ── Shifts ───────────────────────────────────────────────────
INSERT INTO shifts (shift_name, start_time, end_time, working_days, timezone) VALUES
('Morning Shift',   '09:00', '18:00', 'Monday,Tuesday,Wednesday,Thursday,Friday', 'Asia/Kolkata'),
('Night Shift',     '22:00', '06:00', 'Monday,Tuesday,Wednesday,Thursday,Friday', 'Asia/Kolkata'),
('Weekend Support', '10:00', '19:00', 'Saturday,Sunday',                          'UTC');

-- ── Projects ─────────────────────────────────────────────────
INSERT INTO projects (project_name, project_code, description, support_email, sla_hours, shift_timing, status) VALUES
('HR Portal',          'HRP',  'Human Resources management and employee self-service portal.',         'support-hr@company.com',   24, '09:00-18:00', 'ACTIVE'),
('ERP System',         'ERP',  'Enterprise Resource Planning — finance, procurement, and reporting.',  'support-erp@company.com',  16, '09:00-18:00', 'ACTIVE'),
('Telemedicine',       'TLM',  'Remote patient consultation and healthcare records platform.',         'support-tlm@company.com',  8,  '09:00-18:00', 'ACTIVE'),
('Payroll System',     'PAY',  'Payroll processing, salary slips, and statutory compliance.',          'support-pay@company.com',  12, '09:00-18:00', 'ACTIVE'),
('Inventory Mgmt',     'INV',  'Warehouse stock tracking and inventory reconciliation system.',        'support-inv@company.com',  24, '09:00-18:00', 'ACTIVE'),
('Customer Portal',    'CUS',  'B2C customer self-service and order tracking platform.',               'support-cus@company.com',  48, '09:00-18:00', 'ACTIVE'),
('DevOps Pipeline',    'DEV',  'CI/CD pipeline, infrastructure monitoring, and deployment support.',   'support-dev@company.com',  4,  '09:00-18:00', 'ACTIVE'),
('Legacy Migration',   'MIG',  'Legacy system migration and data conversion project.',                 'support-mig@company.com',  32, '09:00-18:00', 'INACTIVE');

-- ── Employees ────────────────────────────────────────────────
INSERT INTO employees (employee_id, employee_name, email, support_level, role, designation, shift, status) VALUES
('EMP-0001', 'John Davis',     'john.davis@company.com',     'L1', 'L1_SUPPORT',     'Junior Support Analyst',     'Morning Shift', 'ACTIVE'),
('EMP-0002', 'Sarah Kim',      'sarah.kim@company.com',      'L2', 'L2_SUPPORT',     'Senior Support Engineer',    'Morning Shift', 'ACTIVE'),
('EMP-0003', 'David Raj',      'david.raj@company.com',      'L3', 'L3_SUPPORT',     'Principal Engineer',         'Morning Shift', 'ACTIVE'),
('EMP-0004', 'Maria Torres',   'maria.torres@company.com',   'L1', 'L1_SUPPORT',     'Support Analyst',            'Morning Shift', 'ACTIVE'),
('EMP-0005', 'Alex Park',      'alex.park@company.com',      'L2', 'L2_SUPPORT',     'Support Engineer',           'Morning Shift', 'ACTIVE'),
('EMP-0006', 'Chen Wei',       'chen.wei@company.com',       'L3', 'L3_SUPPORT',     'Systems Architect',          'Night Shift',   'ACTIVE'),
('EMP-0007', 'Priya Sharma',   'priya.sharma@company.com',   'L1', 'L1_SUPPORT',     'Help Desk Specialist',       'Morning Shift', 'ACTIVE'),
('EMP-0008', 'Marcus Johnson', 'marcus.j@company.com',       'L2', 'PROJECT_MANAGER','Project Manager - ERP',      'Morning Shift', 'ACTIVE'),
('EMP-0009', 'Fatima Al-Said', 'fatima.as@company.com',      'L2', 'L2_SUPPORT',     'Application Support Lead',   'Morning Shift', 'ACTIVE'),
('EMP-0010', 'Carlos Mendes',  'carlos.m@company.com',       'L3', 'L3_SUPPORT',     'Database Engineer',          'Night Shift',   'ACTIVE');

-- ── Employee ↔ Project Assignments ───────────────────────────
-- HR Portal  (project 1): EMP 1,2,3,4
INSERT INTO employee_projects (employee_id, project_id) VALUES
(1,1),(2,1),(3,1),(4,1),
-- ERP System (project 2): EMP 2,3,5,8,9
(2,2),(3,2),(5,2),(8,2),(9,2),
-- Telemedicine (project 3): EMP 2,3,6,9
(2,3),(3,3),(6,3),(9,3),
-- Payroll (project 4): EMP 1,2,5,9
(1,4),(2,4),(5,4),(9,4),
-- Inventory (project 5): EMP 1,4,7
(1,5),(4,5),(7,5),
-- Customer Portal (project 6): EMP 4,5,7,9
(4,6),(5,6),(7,6),(9,6),
-- DevOps (project 7): EMP 3,6,10
(3,7),(6,7),(10,7);

-- ── SLA Configs ──────────────────────────────────────────────
-- HR Portal SLAs
INSERT INTO sla_configs (project_id, priority_level, response_time_sla, resolution_time_sla, escalation_time_sla) VALUES
(1, 'P1 - Critical', 1,  8,  2),
(1, 'P2 - High',     2,  16, 4),
(1, 'P3 - Medium',   4,  24, 8),
(1, 'P4 - Low',      8,  48, 16),
-- ERP System SLAs (tighter — critical for business)
(2, 'P1 - Critical', 1,  4,  1),
(2, 'P2 - High',     2,  8,  3),
(2, 'P3 - Medium',   4,  16, 6),
(2, 'P4 - Low',      8,  32, 12),
-- Telemedicine SLAs (tightest — patient safety)
(3, 'P1 - Critical', 0,  2,  1),
(3, 'P2 - High',     1,  6,  2),
(3, 'P3 - Medium',   2,  12, 4),
(3, 'P4 - Low',      4,  24, 8);

-- ── Users (admin + sample accounts) ─────────────────────────
-- Passwords are BCrypt hashes of: admin123, manager123, support123, user123
INSERT INTO users (username, email, password, role, is_active) VALUES
('admin',      'admin@company.com',      '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN',           TRUE),
('pm_marcus',  'pm.marcus@company.com',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'PROJECT_MANAGER', TRUE),
('l1_john',    'l1.john@company.com',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'L1_SUPPORT',      TRUE),
('l2_sarah',   'l2.sarah@company.com',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'L2_SUPPORT',      TRUE),
('l3_david',   'l3.david@company.com',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'L3_SUPPORT',      TRUE),
('user_test',  'user.test@company.com',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'USER',            TRUE);

-- NOTE: All seed users have password = "password" (BCrypt hash above)
-- Change ALL passwords immediately in production!

-- ── Tickets ──────────────────────────────────────────────────
INSERT INTO tickets (
    ticket_number, project_id, issue_description, assigned_employee_id,
    support_level, priority, generation_datetime, response_datetime,
    resolution_time, business_hours_elapsed, current_status,
    resolution_details, remarks, sla_breached
) VALUES

-- HR Portal tickets
('INC-1001', 1,
 'Unable to login to HR portal. Password reset not working for multiple users in the Finance department.',
 1, 'L1', 'P2 - High',
 NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 2 DAY,
 '24h 0m', 24, 'Resolved',
 'Password reset policy was reconfigured and affected accounts unlocked. Users notified via email.',
 'Affected 15 users in Finance department', FALSE),

('INC-1002', 1,
 'Employee self-service portal showing incorrect leave balance for all employees hired after Jan 2024.',
 2, 'L2', 'P2 - High',
 NOW() - INTERVAL 5 DAY, NULL,
 NULL, 72, 'In Progress',
 NULL,
 'Leave accrual calculation issue post system upgrade', TRUE),

('INC-1003', 1,
 'Performance appraisal form submission failing with 500 error. Deadline is in 2 days.',
 3, 'L3', 'P1 - Critical',
 NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 6 HOUR,
 '18h 0m', 18, 'Resolved',
 'Database constraint violation fixed. Form submissions now working correctly.',
 'Appraisal cycle at risk', FALSE),

-- ERP System tickets
('INC-1004', 2,
 'ERP month-end financial report crashing when data exceeds 10,000 rows. Finance team blocked.',
 3, 'L3', 'P1 - Critical',
 NOW() - INTERVAL 2 DAY, NULL,
 NULL, 48, 'In Progress',
 NULL,
 'Critical for month-end closing — escalated to vendor', TRUE),

('INC-1005', 2,
 'Purchase order approval workflow emails not being delivered to approvers. 47 POs stuck.',
 5, 'L2', 'P2 - High',
 NOW() - INTERVAL 12 HOUR, NULL,
 NULL, 12, 'Open',
 NULL,
 'SMTP relay configuration suspected', FALSE),

('INC-1006', 2,
 'User access provisioning taking over 24 hours for new joiners. IT onboarding severely delayed.',
 9, 'L2', 'P3 - Medium',
 NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 3 DAY,
 '8h 30m', 8, 'Resolved',
 'Auto-provisioning script fixed. New accounts now created within 15 minutes.',
 NULL, FALSE),

-- Telemedicine tickets
('INC-1007', 3,
 'Video consultation feature completely broken on iOS 17.4+. Patients unable to connect with doctors.',
 6, 'L3', 'P1 - Critical',
 NOW() - INTERVAL 2 HOUR, NULL,
 NULL, 2, 'Open',
 NULL,
 'Approx 200 appointments affected today', FALSE),

('INC-1008', 3,
 'Patient medical history not syncing between mobile app and web portal after latest app update v3.2.',
 2, 'L2', 'P2 - High',
 NOW() - INTERVAL 6 HOUR, NULL,
 NULL, 6, 'Escalated',
 NULL,
 'Waiting for vendor SDK patch', TRUE),

('INC-1009', 3,
 'Prescription module intermittently displaying wrong patient data. Data integrity concern flagged.',
 3, 'L3', 'P1 - Critical',
 NOW() - INTERVAL 8 HOUR, NOW() - INTERVAL 1 HOUR,
 '7h 0m', 7, 'Resolved',
 'Race condition in session management fixed. Extensive regression testing completed.',
 'HIPAA compliance issue — incident report filed', FALSE),

-- Payroll tickets
('INC-1010', 4,
 'Salary slips not generating for contract employees this month. 120 employees affected.',
 5, 'L2', 'P1 - Critical',
 NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 4 DAY,
 '20h 30m', 20, 'Closed',
 'Payroll configuration updated for contract employment type. Slip generation re-run successfully.',
 NULL, FALSE),

('INC-1011', 4,
 'TDS deduction incorrect for employees in the new tax regime. Compliance risk.',
 9, 'L2', 'P2 - High',
 NOW() - INTERVAL 3 DAY, NULL,
 NULL, 36, 'Pending',
 NULL,
 'Awaiting tax consultant confirmation before applying fix', TRUE),

-- Inventory tickets
('INC-1012', 5,
 'Stock count mismatch between warehouse system and physical count. Discrepancy of 340 units.',
 1, 'L1', 'P3 - Medium',
 NOW() - INTERVAL 6 HOUR, NULL,
 NULL, 6, 'Open',
 NULL,
 'Quarterly audit triggered this', FALSE),

('INC-1013', 5,
 'Barcode scanner integration failing for bulk import. Slowing down daily receiving operations.',
 4, 'L1', 'P3 - Medium',
 NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY,
 '6h 15m', 6, 'Resolved',
 'USB driver updated and scanner reconfigured. All 12 scanners now operational.',
 NULL, FALSE),

-- Customer Portal tickets
('INC-1014', 6,
 'Customers unable to track orders — status page showing 404 after last deployment.',
 5, 'L2', 'P2 - High',
 NOW() - INTERVAL 4 HOUR, NULL,
 NULL, 4, 'In Progress',
 NULL,
 'Post-deployment regression — rollback being considered', FALSE),

('INC-1015', 6,
 'Payment gateway timeout errors for credit card transactions above INR 50,000.',
 9, 'L2', 'P1 - Critical',
 NOW() - INTERVAL 1 HOUR, NULL,
 NULL, 1, 'Open',
 NULL,
 'Revenue impact — escalate immediately', FALSE),

-- DevOps tickets
('INC-1016', 7,
 'Production CI/CD pipeline failing at Docker build stage. All releases blocked.',
 10, 'L3', 'P1 - Critical',
 NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 30 MINUTE,
 '2h 30m', 2, 'Resolved',
 'Base Docker image updated and cache invalidated. Pipeline green across all branches.',
 NULL, FALSE),

('INC-1017', 7,
 'Kubernetes cluster memory utilisation at 94%. Risk of OOM pod evictions during peak hours.',
 3, 'L3', 'P2 - High',
 NOW() - INTERVAL 10 HOUR, NULL,
 NULL, 10, 'In Progress',
 NULL,
 'Node autoscaling being enabled', FALSE);
