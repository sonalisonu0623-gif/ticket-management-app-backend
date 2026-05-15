-- ============================================================
-- Ticket Management System - MySQL Schema & Sample Data
-- ============================================================

CREATE DATABASE IF NOT EXISTS ticket_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ticket_db;

-- Projects Table
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_name VARCHAR(255) NOT NULL UNIQUE
);

-- Employees Table
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_name VARCHAR(255) NOT NULL,
    support_level VARCHAR(10)
);

-- Tickets Table
CREATE TABLE IF NOT EXISTS tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_number VARCHAR(20) NOT NULL UNIQUE,
    project_id BIGINT NOT NULL,
    issue_description TEXT NOT NULL,
    assigned_employee_id BIGINT,
    support_level VARCHAR(10),
    priority VARCHAR(20),
    generation_datetime DATETIME,
    response_datetime DATETIME,
    resolution_time VARCHAR(50),
    current_status VARCHAR(20) DEFAULT 'Open',
    resolution_details TEXT,
    remarks TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (assigned_employee_id) REFERENCES employees(id)
);

-- ============================================================
-- Sample Data
-- ============================================================

INSERT INTO projects (project_name) VALUES
('HR Portal'),
('ERP System'),
('Telemedicine'),
('Payroll System'),
('Inventory Management');

INSERT INTO employees (employee_name, support_level) VALUES
('John.D', 'L1'),
('Smith.K', 'L2'),
('David.R', 'L3'),
('Maria.T', 'L1'),
('Alex.P', 'L2'),
('Chen.W', 'L3');

INSERT INTO tickets (ticket_number, project_id, issue_description, assigned_employee_id, support_level, priority, generation_datetime, response_datetime, resolution_time, current_status, resolution_details, remarks) VALUES
('INC-1001', 1, 'Unable to login to HR portal. Password reset not working for multiple users.', 1, 'L1', 'P2 - High', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 2 DAY, '24h 0m', 'Resolved', 'Reset password policy updated and users notified.', 'Affected 15 users in HR department'),
('INC-1002', 2, 'ERP module crashing on report generation. Error occurs for reports >500 rows.', 3, 'L3', 'P1 - Critical', NOW() - INTERVAL 2 DAY, NULL, NULL, 'In Progress', NULL, 'Critical for month-end closing'),
('INC-1003', 3, 'Video consultation feature not working on iOS devices.', 2, 'L2', 'P2 - High', NOW() - INTERVAL 1 DAY, NULL, NULL, 'Open', NULL, 'Patients affected'),
('INC-1004', 4, 'Salary slips not generating for contract employees this month.', 2, 'L2', 'P1 - Critical', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 4 DAY, '20h 30m', 'Closed', 'Payroll configuration updated for contract type employees.', NULL),
('INC-1005', 5, 'Inventory stock count mismatch between warehouse and system.', 1, 'L1', 'P3 - Medium', NOW() - INTERVAL 6 HOUR, NULL, NULL, 'Open', NULL, 'Warehouse team flagged'),
('INC-1006', 1, 'Performance issue - employee search taking over 30 seconds.', 3, 'L3', 'P3 - Medium', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 3 DAY, '18h 45m', 'Resolved', 'Database indexes optimized. Query time reduced to <1s.', NULL),
('INC-1007', 2, 'Purchase order approval workflow stuck - notifications not sent.', 2, 'L2', 'P2 - High', NOW() - INTERVAL 12 HOUR, NULL, NULL, 'In Progress', NULL, 'Finance team blocked'),
('INC-1008', 3, 'Patient data not syncing between mobile app and web portal.', 3, 'L3', 'P1 - Critical', NOW() - INTERVAL 2 HOUR, NULL, NULL, 'On Hold', NULL, 'Waiting for vendor response');
