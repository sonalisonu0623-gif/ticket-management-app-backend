DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS projects;

CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_name VARCHAR(255) NOT NULL,
    support_level VARCHAR(10)
);

CREATE TABLE tickets (
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

    CONSTRAINT fk_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id),

    CONSTRAINT fk_employee
        FOREIGN KEY (assigned_employee_id)
        REFERENCES employees(id)
);


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

INSERT INTO tickets (
ticket_number,
project_id,
issue_description,
assigned_employee_id,
support_level,
priority,
generation_datetime,
response_datetime,
resolution_time,
current_status,
resolution_details,
remarks
) VALUES

('INC-1001', 1,
'Unable to login to HR portal. Password reset not working for multiple users.',
1, 'L1', 'P2 - High',
NOW() - INTERVAL 3 DAY,
NOW() - INTERVAL 2 DAY,
'24h 0m',
'Resolved',
'Reset password policy updated and users notified.',
'Affected 15 users in HR department'),

('INC-1002', 2,
'ERP module crashing on report generation.',
3, 'L3', 'P1 - Critical',
NOW() - INTERVAL 2 DAY,
NULL,
NULL,
'In Progress',
NULL,
'Critical for month-end closing');