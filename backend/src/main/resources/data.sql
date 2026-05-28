-- ============================================================
-- SEED DATA — runs at startup (INSERT IGNORE = idempotent)
-- ============================================================

-- Default shift hours
INSERT IGNORE INTO shift_hours (id, shift_name, start_time, end_time, is_active)
VALUES
  (1, 'General Shift (9 AM – 6 PM)',  '09:00:00', '18:00:00', TRUE),
  (2, 'Morning Shift (7 AM – 4 PM)',  '07:00:00', '16:00:00', TRUE),
  (3, 'Night Shift (10 PM – 7 AM)',   '22:00:00', '07:00:00', FALSE);

-- Default SLA config (P1–P4 × L1–L3)
INSERT IGNORE INTO sla_config (priority, support_level, response_time_hours, resolution_time_hours, is_active)
VALUES
  ('P1_CRITICAL', 'L1', 0.5,  2,  TRUE),
  ('P1_CRITICAL', 'L2', 1,    4,  TRUE),
  ('P1_CRITICAL', 'L3', 2,    8,  TRUE),
  ('P2_HIGH',     'L1', 1,    4,  TRUE),
  ('P2_HIGH',     'L2', 2,    8,  TRUE),
  ('P2_HIGH',     'L3', 4,    16, TRUE),
  ('P3_MEDIUM',   'L1', 2,    8,  TRUE),
  ('P3_MEDIUM',   'L2', 4,    16, TRUE),
  ('P3_MEDIUM',   'L3', 8,    24, TRUE),
  ('P4_LOW',      'L1', 4,    16, TRUE),
  ('P4_LOW',      'L2', 8,    24, TRUE),
  ('P4_LOW',      'L3', 16,   40, TRUE);

-- Sample tickets
INSERT IGNORE INTO tickets (
    ticket_id, project_assignment, issue_description, assigned_employee,
    support_level, priority, generation_date_time, current_status, remarks
) VALUES
  ('INC-1001', 'HR-Portal',          'Login page not loading after latest deployment', 'John Smith',     'L2', 'P1_CRITICAL', NOW(), 'OPEN',        'Reported by multiple users'),
  ('INC-1002', 'ERP-Telemed',        'Patient records sync failing intermittently',    'Jane Doe',       'L3', 'P2_HIGH',     NOW(), 'IN_PROGRESS', 'Under investigation'),
  ('INC-1003', 'Payroll-System',     'Payslip generation delayed for December cycle',  'Bob Johnson',    'L1', 'P3_MEDIUM',   NOW(), 'RESOLVED',    'Fixed in v2.1.3'),
  ('INC-1004', 'Employee-Management','Onboarding form throwing 500 error',             'Alice Williams', 'L2', 'P2_HIGH',     NOW(), 'OPEN',        'Needs backend fix'),
  ('INC-1005', 'HR-Portal',          'Leave balance not updating after approval',      'John Smith',     'L1', 'P4_LOW',      NOW(), 'CLOSED',      'Resolved by cache clear');
