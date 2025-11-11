-- Hospital Management System Database Setup
-- Run this script in MySQL to create all necessary tables

-- Create Database
CREATE DATABASE IF NOT EXISTS hospital_management_system;
USE hospital_management_system;

-- Login Table
CREATE TABLE IF NOT EXISTS login (
    ID VARCHAR(50) PRIMARY KEY,
    PW VARCHAR(50) NOT NULL
);

-- Insert default admin login (username: admin, password: admin123)
INSERT INTO login (ID, PW) VALUES 
('admin', 'admin123'),
('user', 'user123');

-- Employee Information Table
CREATE TABLE IF NOT EXISTS EMP_INFO (
    Name VARCHAR(100) NOT NULL,
    Age INT,
    Phone VARCHAR(15),
    Salary DECIMAL(10,2),
    Gmail VARCHAR(100),
    Aadhar VARCHAR(20) PRIMARY KEY
);

-- Insert sample employee data
INSERT INTO EMP_INFO VALUES
('Dr. Rajesh Kumar', 45, '9876543210', 150000.00, 'rajesh.kumar@hospital.com', '1234-5678-9012'),
('Nurse Priya Sharma', 32, '9876543211', 45000.00, 'priya.sharma@hospital.com', '2234-5678-9012'),
('Dr. Anjali Patel', 38, '9876543212', 140000.00, 'anjali.patel@hospital.com', '3234-5678-9012'),
('Receptionist Ravi Singh', 28, '9876543213', 30000.00, 'ravi.singh@hospital.com', '4234-5678-9012'),
('Pharmacist Meera Joshi', 35, '9876543214', 50000.00, 'meera.joshi@hospital.com', '5234-5678-9012');

-- Department Table
CREATE TABLE IF NOT EXISTS department (
    Department VARCHAR(100) PRIMARY KEY,
    Phone_Number VARCHAR(15)
);

-- Insert department data
INSERT INTO department VALUES
('Surgery', '011-2345-6789'),
('Cardiology', '011-2345-6790'),
('Pediatrics', '011-2345-6791'),
('Neurology', '011-2345-6792'),
('Orthopedics', '011-2345-6793'),
('Emergency', '011-2345-6794'),
('Radiology', '011-2345-6795'),
('Laboratory', '011-2345-6796');

-- Room Table
CREATE TABLE IF NOT EXISTS Room (
    room_no VARCHAR(10) PRIMARY KEY,
    Availability VARCHAR(20) DEFAULT 'Available',
    Price DECIMAL(10,2),
    Bed_Type VARCHAR(20)
);

-- Insert room data
INSERT INTO Room VALUES
('101', 'Available', 5000.00, 'Single'),
('102', 'Available', 8000.00, 'Double'),
('103', 'Available', 5000.00, 'Single'),
('104', 'Occupied', 8000.00, 'Double'),
('105', 'Available', 5000.00, 'Single'),
('201', 'Available', 6000.00, 'Single'),
('202', 'Available', 10000.00, 'Double'),
('203', 'Available', 6000.00, 'Single'),
('204', 'Available', 10000.00, 'Double'),
('205', 'Occupied', 6000.00, 'Single'),
('301', 'Available', 7000.00, 'Single'),
('302', 'Available', 12000.00, 'Double'),
('303', 'Available', 7000.00, 'Single'),
('304', 'Available', 12000.00, 'Double'),
('305', 'Available', 7000.00, 'Single');

-- Patient Information Table
CREATE TABLE IF NOT EXISTS Patient_Info (
    ID VARCHAR(50),
    Number VARCHAR(20) PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Gender VARCHAR(10),
    Disease VARCHAR(200),
    Room_Number VARCHAR(10),
    Time VARCHAR(100),
    Deposite DECIMAL(10,2),
    FOREIGN KEY (Room_Number) REFERENCES Room(room_no)
);

-- Insert sample patient data
INSERT INTO Patient_Info VALUES
('Aadhar Card', '9876543201', 'Amit Verma', 'Male', 'Fever', '104', '2025-11-01 10:30:00', 5000.00),
('Voter ID', '9876543202', 'Sunita Devi', 'Female', 'Fracture', '205', '2025-11-02 14:20:00', 3000.00);

-- Ambulance Table
CREATE TABLE IF NOT EXISTS Ambulance (
    Name VARCHAR(100),
    Gender VARCHAR(10),
    Car_Name VARCHAR(50),
    Available VARCHAR(20),
    Location VARCHAR(100)
);

-- Insert ambulance data
INSERT INTO Ambulance VALUES
('Rajesh Kumar', 'Male', 'Mahindra Bolero', 'Available', 'Main Hospital'),
('Vikram Singh', 'Male', 'Toyota Innova', 'Available', 'North Wing'),
('Priya Sharma', 'Female', 'Maruti Eeco', 'Busy', 'Emergency Ward'),
('Amit Patel', 'Male', 'Mahindra Bolero', 'Available', 'South Wing'),
('Meera Joshi', 'Female', 'Toyota Innova', 'Available', 'Main Hospital');

-- Appointments Table
CREATE TABLE IF NOT EXISTS Appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(100),
    age INT,
    gender VARCHAR(10),
    department VARCHAR(100),
    doctor VARCHAR(100),
    appointment_date DATE,
    time_slot VARCHAR(20),
    symptoms TEXT,
    booking_date DATE,
    status VARCHAR(20) DEFAULT 'Confirmed'
);

-- Insert sample appointments
INSERT INTO Appointments (patient_name, phone, email, age, gender, department, doctor, 
                         appointment_date, time_slot, symptoms, booking_date, status) VALUES
('Rajesh Kumar', '9876543220', 'rajesh@email.com', 45, 'Male', 'Cardiology', 'Dr. Amit Sharma', 
 '2025-11-05', '10:00 AM', 'Chest pain and breathing difficulty', '2025-11-03', 'Confirmed'),
('Priya Singh', '9876543221', 'priya@email.com', 32, 'Female', 'Pediatrics', 'Dr. Sneha Joshi',
 '2025-11-06', '11:00 AM', 'Child vaccination', '2025-11-03', 'Confirmed'),
('Amit Patel', '9876543222', 'amit@email.com', 28, 'Male', 'Orthopedics', 'Dr. Rajesh Kumar',
 '2025-11-04', '02:00 PM', 'Knee pain', '2025-11-02', 'Confirmed');

-- Bills Table
CREATE TABLE IF NOT EXISTS Bills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_number VARCHAR(20),
    room_charge DECIMAL(10,2),
    medicine DECIMAL(10,2),
    lab_test DECIMAL(10,2),
    doctor_fee DECIMAL(10,2),
    other_charges DECIMAL(10,2),
    total_amount DECIMAL(10,2),
    bill_date DATETIME,
    FOREIGN KEY (patient_number) REFERENCES Patient_Info(Number)
);

-- Insert sample bills
INSERT INTO Bills (patient_number, room_charge, medicine, lab_test, doctor_fee, 
                   other_charges, total_amount, bill_date) VALUES
('9876543201', 5000.00, 1500.00, 800.00, 2000.00, 500.00, 9800.00, '2025-11-03 10:30:00'),
('9876543202', 3000.00, 1200.00, 600.00, 1500.00, 300.00, 6600.00, '2025-11-02 15:20:00');

-- Pharmacy Table
CREATE TABLE IF NOT EXISTS Pharmacy (
    id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_name VARCHAR(200) NOT NULL,
    category VARCHAR(50),
    manufacturer VARCHAR(100),
    quantity INT DEFAULT 0,
    price DECIMAL(10,2),
    added_date DATE
);

-- Insert sample pharmacy data
INSERT INTO Pharmacy (medicine_name, category, manufacturer, quantity, price, added_date) VALUES
('Paracetamol 500mg', 'Pain Relievers', 'Cipla Ltd', 500, 5.00, '2025-11-01'),
('Amoxicillin 250mg', 'Antibiotics', 'Sun Pharma', 300, 15.00, '2025-11-01'),
('Azithromycin 500mg', 'Antibiotics', 'Dr. Reddy\'s', 200, 25.00, '2025-11-01'),
('Crocin Advance', 'Pain Relievers', 'GSK', 400, 8.00, '2025-11-01'),
('Dettol Antiseptic', 'Antiseptics', 'Reckitt Benckiser', 150, 120.00, '2025-11-01'),
('Vitamin D3', 'Vitamins', 'HealthKart', 250, 200.00, '2025-11-01'),
('Metformin 500mg', 'Diabetic', 'Cipla Ltd', 350, 12.00, '2025-11-01'),
('Aspirin 75mg', 'Cardiac', 'Bayer', 400, 10.00, '2025-11-01'),
('Salbutamol Inhaler', 'Respiratory', 'GSK', 80, 180.00, '2025-11-01'),
('Omeprazole 20mg', 'Digestive', 'Sun Pharma', 300, 8.00, '2025-11-01');

-- Lab Tests Table
CREATE TABLE IF NOT EXISTS LabTests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_number VARCHAR(20),
    test_type VARCHAR(200),
    requested_by VARCHAR(100),
    notes TEXT,
    request_date DATE,
    status VARCHAR(20) DEFAULT 'Pending',
    result TEXT,
    completion_date DATE,
    FOREIGN KEY (patient_number) REFERENCES Patient_Info(Number)
);

-- Insert sample lab tests
INSERT INTO LabTests (patient_number, test_type, requested_by, notes, request_date, status) VALUES
('9876543201', 'Blood Test - Complete Blood Count (CBC)', 'Dr. Amit Sharma', 'Routine checkup', '2025-11-03', 'Pending'),
('9876543202', 'X-Ray - Bone', 'Dr. Rajesh Kumar', 'Check fracture healing', '2025-11-02', 'Pending'),
('9876543201', 'Blood Test - Blood Sugar', 'Dr. Amit Sharma', 'Monitor diabetes', '2025-11-01', 'Completed');

-- Medical Records Table
CREATE TABLE IF NOT EXISTS MedicalRecords (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_number VARCHAR(20),
    visit_date DATE,
    diagnosis TEXT,
    prescription TEXT,
    doctor_name VARCHAR(100),
    notes TEXT,
    FOREIGN KEY (patient_number) REFERENCES Patient_Info(Number)
);

-- Insert sample medical records
INSERT INTO MedicalRecords (patient_number, visit_date, diagnosis, prescription, doctor_name, notes) VALUES
('9876543201', '2025-11-01', 'Fever and cold', 'Paracetamol 500mg - 3 times daily\nAmoxicillin 250mg - 2 times daily', 
 'Dr. Amit Sharma', 'Patient advised complete rest for 3 days'),
('9876543202', '2025-11-02', 'Bone fracture (right hand)', 'Painkillers and plaster cast', 
 'Dr. Rajesh Kumar', 'Follow-up after 2 weeks');

-- Display all tables
SHOW TABLES;

-- Verify data
SELECT 'Login Table' AS Table_Name;
SELECT * FROM login;

SELECT 'Employee Table' AS Table_Name;
SELECT * FROM EMP_INFO;

SELECT 'Department Table' AS Table_Name;
SELECT * FROM department;

SELECT 'Room Table' AS Table_Name;
SELECT * FROM Room;

SELECT 'Patient Info Table' AS Table_Name;
SELECT * FROM Patient_Info;

SELECT 'Ambulance Table' AS Table_Name;
SELECT * FROM Ambulance;

SELECT 'Appointments Table' AS Table_Name;
SELECT * FROM Appointments;

SELECT 'Bills Table' AS Table_Name;
SELECT * FROM Bills;

SELECT 'Pharmacy Table' AS Table_Name;
SELECT * FROM Pharmacy;

SELECT 'Lab Tests Table' AS Table_Name;
SELECT * FROM LabTests;

SELECT 'Medical Records Table' AS Table_Name;
SELECT * FROM MedicalRecords;
