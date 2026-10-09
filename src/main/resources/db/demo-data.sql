-- Demo data, loaded automatically the first time the app starts on an empty database.
-- Dates are relative to today (CURDATE / NOW), so the demo always has upcoming appointments.
--
-- Demo logins (each password hash below was made by PasswordHasher with its own random salt):
--   admin    / Admin@123    Administrator
--   dr.mehta / Doctor@123   Doctor (Cardiology)
--   dr.rao   / Doctor@123   Doctor (Pediatrics)
--   priya    / Patient@123  Patient
--   rahul    / Patient@123  Patient (currently admitted)

INSERT INTO users (id, username, password_hash, role) VALUES
    (1, 'admin',    'pbkdf2$600000$j7uYkBfn8vpgylR30ce+5A==$cP694NF3Sc201dygtlsjd3dbYwE+o6YAPvhHaWRiWwU=', 'ADMIN'),
    (2, 'dr.mehta', 'pbkdf2$600000$CAwzs/vvZSPxA4ToDe/uZA==$/UKyInnNfPb10/NTK8snWAXASqD4FOZ1Bnu1JIsI0L4=', 'DOCTOR'),
    (3, 'dr.rao',   'pbkdf2$600000$DjmZ7dDnbwBbnfKj6IuGTA==$AUhhlbor3fkVWNRJ9WOmjxIXi/r8nHLpmPVJbHXyUUc=', 'DOCTOR'),
    (4, 'priya',    'pbkdf2$600000$nupc4rPbc2i4w6GnDtLyKQ==$cOvIViNrVec9gg8KJMBnbmeaA89RauGJSYu4UuDklkA=', 'PATIENT'),
    (5, 'rahul',    'pbkdf2$600000$sD4KcCXANLDoVBJbbbRLMQ==$KRzyoyb05YVNEwFcq1o3l5Upgxwrev/2JUyDX1OT6SY=', 'PATIENT');

INSERT INTO departments (id, name, phone) VALUES
    (1, 'Cardiology',       '011-4155-0101'),
    (2, 'Pediatrics',       '011-4155-0102'),
    (3, 'Orthopedics',      '011-4155-0103'),
    (4, 'Neurology',        '011-4155-0104'),
    (5, 'General Medicine', '011-4155-0105'),
    (6, 'Emergency',        '011-4155-0106');

INSERT INTO doctors (id, name, specialization, department_id, phone, email, user_id) VALUES
    (1, 'Dr. Arjun Mehta',  'Cardiologist',       1, '9876501001', 'arjun.mehta@hospital.example', 2),
    (2, 'Dr. Kavita Rao',   'Pediatrician',       2, '9876501002', 'kavita.rao@hospital.example', 3),
    (3, 'Dr. Sameer Khan',  'Orthopedic Surgeon', 3, '9876501003', 'sameer.khan@hospital.example', NULL),
    (4, 'Dr. Neha Iyer',    'Neurologist',        4, '9876501004', 'neha.iyer@hospital.example', NULL),
    (5, 'Dr. Vikram Singh', 'General Physician',  5, '9876501005', 'vikram.singh@hospital.example', NULL),
    (6, 'Dr. Ananya Das',   'Emergency Medicine', 6, '9876501006', NULL, NULL);

INSERT INTO patients (id, name, gender, date_of_birth, phone, email, address, user_id) VALUES
    (1, 'Priya Sharma',   'FEMALE', '1992-04-18', '9812345601', 'priya.sharma@example.com', 'Sector 21, Chandigarh', 4),
    (2, 'Rahul Verma',    'MALE',   '1985-11-02', '9812345602', 'rahul.verma@example.com', 'Model Town, Ludhiana', 5),
    (3, 'Ananya Gupta',   'FEMALE', '2016-07-09', '9812345603', NULL, 'Phase 7, Mohali', NULL),
    (4, 'Mohammed Irfan', 'MALE',   '1978-01-25', '9812345604', 'irfan.m@example.com', 'Civil Lines, Delhi', NULL),
    (5, 'Sneha Reddy',    'FEMALE', '1999-09-30', '9812345605', 'sneha.reddy@example.com', NULL, NULL),
    (6, 'Arjun Nair',     'MALE',   '1965-03-14', '9812345606', NULL, 'Vyttila, Kochi', NULL),
    (7, 'Kiran Patel',    'FEMALE', '1990-12-05', '9812345607', 'kiran.patel@example.com', 'Navrangpura, Ahmedabad', NULL),
    (8, 'Vikas Yadav',    'MALE',   '2001-06-21', '9812345608', NULL, NULL, NULL);

-- Rooms 201 and 301 are occupied by the two open admissions below; 104 is being repaired.
INSERT INTO rooms (id, room_number, type, daily_rate, status) VALUES
    (1,  '101',   'GENERAL',      1200.00, 'AVAILABLE'),
    (2,  '102',   'GENERAL',      1200.00, 'AVAILABLE'),
    (3,  '103',   'GENERAL',      1200.00, 'AVAILABLE'),
    (4,  '104',   'GENERAL',      1200.00, 'MAINTENANCE'),
    (5,  '201',   'SEMI_PRIVATE', 2500.00, 'OCCUPIED'),
    (6,  '202',   'SEMI_PRIVATE', 2500.00, 'AVAILABLE'),
    (7,  '301',   'PRIVATE',      4500.00, 'OCCUPIED'),
    (8,  '302',   'PRIVATE',      4500.00, 'AVAILABLE'),
    (9,  'ICU-1', 'ICU',          9000.00, 'AVAILABLE'),
    (10, 'ICU-2', 'ICU',          9000.00, 'AVAILABLE');

INSERT INTO lab_test_types (id, name, price) VALUES
    (1, 'Complete Blood Count (CBC)',   350.00),
    (2, 'Lipid Profile',                600.00),
    (3, 'Thyroid Profile (T3, T4, TSH)', 550.00),
    (4, 'Blood Sugar (Fasting)',        120.00),
    (5, 'ECG',                          300.00),
    (6, 'Chest X-Ray',                  450.00),
    (7, 'Dengue NS1 Antigen',           800.00),
    (8, 'Liver Function Test',          700.00);

INSERT INTO medicines (id, name, category, manufacturer, unit_price, stock, reorder_level) VALUES
    (1,  'Paracetamol 500 mg',      'Analgesic',   'Cipla',       2.00, 490, 100),
    (2,  'Amoxicillin 250 mg',      'Antibiotic',  'Sun Pharma',  6.50, 300, 80),
    (3,  'Azithromycin 500 mg',     'Antibiotic',  'Dr. Reddy''s', 22.00, 120, 50),
    (4,  'Atorvastatin 40 mg',      'Cardiac',     'Lupin',       9.00, 185, 50),
    (5,  'Aspirin 75 mg',           'Cardiac',     'Zydus',       1.50, 370, 100),
    (6,  'Metformin 500 mg',        'Diabetes',    'Glenmark',    3.00, 350, 100),
    (7,  'ORS Sachet',              'Rehydration', 'Mankind',    18.00, 54, 40),
    (8,  'Salbutamol Inhaler',      'Respiratory', 'Cipla',     145.00, 15, 20),
    (9,  'Pantoprazole 40 mg',      'Gastric',     'Torrent',     7.00, 250, 60),
    (10, 'Insulin Glargine 100 IU', 'Diabetes',    'Biocon',    650.00, 8, 10),
    (11, 'Propranolol 10 mg',       'Cardiac',     'Sun Pharma',  2.50, 152, 50);

-- Bill 1: a finished, paid stay: 4 nights x 1,200 + medicines 128 + lab 1,150 = 6,078.
-- Bill 2: Priya's unpaid outpatient bill: ECG 300 + propranolol 70 = 370.
-- Still unbilled: Priya's thyroid test (ordered after that bill) and Rahul's medicines and tests
-- (billed when he is discharged).
INSERT INTO bills (id, patient_id, room_charges, medicine_charges, lab_charges, status, created_at, paid_at) VALUES
    (1, 4, 4800.00, 128.00, 1150.00, 'PAID',
     NOW() - INTERVAL 16 DAY, NOW() - INTERVAL 16 DAY + INTERVAL 2 HOUR),
    (2, 1, 0.00, 70.00, 300.00, 'UNPAID',
     NOW() - INTERVAL 14 DAY + INTERVAL 3 HOUR, NULL);

INSERT INTO admissions (id, patient_id, room_id, doctor_id, reason, daily_rate, admitted_at, discharged_at, bill_id) VALUES
    (1, 4, 2, 5, 'Dengue fever: platelet monitoring', 1200.00,
     NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 16 DAY, 1),
    (2, 2, 7, 1, 'Unstable angina: observation after angioplasty', 4500.00,
     NOW() - INTERVAL 3 DAY, NULL, NULL),
    (3, 6, 5, 3, 'Hip fracture: post-operative care', 2500.00,
     NOW() - INTERVAL 1 DAY, NULL, NULL);

INSERT INTO appointments (patient_id, doctor_id, appointment_date, slot, reason, status) VALUES
    (4, 5, CURDATE() - INTERVAL 21 DAY, '09:30', 'High fever and body ache',  'COMPLETED'),
    (1, 1, CURDATE() - INTERVAL 14 DAY, '10:30', 'Chest discomfort',          'COMPLETED'),
    (2, 1, CURDATE() - INTERVAL 5 DAY,  '11:00', 'Chest pain on exertion',    'COMPLETED'),
    (6, 3, CURDATE() - INTERVAL 3 DAY,  '10:00', 'Hip pain after a fall',     'CANCELLED'),
    (4, 1, CURDATE(),                   '16:00', 'ECG review',                'SCHEDULED'),
    (3, 2, CURDATE(),                   '16:30', 'Cough and cold',            'SCHEDULED'),
    (1, 1, CURDATE() + INTERVAL 1 DAY,  '10:00', 'Palpitations and fatigue',  'SCHEDULED'),
    (3, 2, CURDATE() + INTERVAL 1 DAY,  '09:30', 'Vaccination follow-up',     'SCHEDULED'),
    (5, 4, CURDATE() + INTERVAL 2 DAY,  '11:00', 'Recurring migraines',       'SCHEDULED'),
    (8, 3, CURDATE() + INTERVAL 2 DAY,  '15:00', 'Knee pain after football',  'SCHEDULED'),
    (7, 1, CURDATE() + INTERVAL 3 DAY,  '14:30', 'Blood pressure follow-up',  'SCHEDULED'),
    (1, 5, CURDATE() + INTERVAL 7 DAY,  '09:00', 'Annual health check-up',    'SCHEDULED');

INSERT INTO medical_records (patient_id, doctor_id, visit_date, diagnosis, prescription, notes) VALUES
    (3, 2, CURDATE() - INTERVAL 30 DAY, 'Viral upper respiratory infection',
     'Paracetamol syrup 5 ml if fever is above 100 F', 'Plenty of fluids. Review if fever lasts over 3 days.'),
    (4, 5, CURDATE() - INTERVAL 20 DAY, 'Dengue fever (NS1 positive)',
     'Paracetamol 500 mg when needed; oral rehydration', 'Admitted for platelet monitoring.'),
    (1, 1, CURDATE() - INTERVAL 14 DAY, 'Sinus tachycardia, likely stress-related',
     'Propranolol 10 mg twice daily for 2 weeks', 'ECG done. Review in 2 weeks with a thyroid profile.'),
    (2, 1, CURDATE() - INTERVAL 5 DAY, 'Unstable angina',
     'Aspirin 75 mg daily; Atorvastatin 40 mg at night', 'Admitted for angioplasty.');

INSERT INTO lab_tests (patient_id, doctor_id, test_type_id, price, status, result, ordered_at, completed_at, bill_id) VALUES
    (4, 5, 1, 350.00, 'COMPLETED', 'Platelets 85,000/uL (low)',
     NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY + INTERVAL 4 HOUR, 1),
    (4, 5, 7, 800.00, 'COMPLETED', 'Positive',
     NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY + INTERVAL 6 HOUR, 1),
    (1, 1, 5, 300.00, 'COMPLETED', 'Sinus rhythm, rate 102 bpm. No ischaemic changes.',
     NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 14 DAY + INTERVAL 1 HOUR, 2),
    (1, 1, 3, 550.00, 'ORDERED', NULL, NOW() - INTERVAL 1 DAY, NULL, NULL),
    (2, 1, 2, 600.00, 'COMPLETED', 'LDL 168 mg/dL (high); HDL 38 mg/dL',
     NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY + INTERVAL 5 HOUR, NULL),
    (2, 1, 5, 300.00, 'ORDERED', NULL, NOW() - INTERVAL 1 DAY, NULL, NULL);

INSERT INTO medicine_dispenses (patient_id, medicine_id, quantity, unit_price, dispensed_at, bill_id) VALUES
    (4, 1, 10,  2.00, NOW() - INTERVAL 19 DAY, 1),
    (4, 7,  6, 18.00, NOW() - INTERVAL 19 DAY, 1),
    (1, 11, 28, 2.50, NOW() - INTERVAL 14 DAY, 2),
    (2, 5, 30,  1.50, NOW() - INTERVAL 2 DAY, NULL),
    (2, 4, 15,  9.00, NOW() - INTERVAL 2 DAY, NULL);
