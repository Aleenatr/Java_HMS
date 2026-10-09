-- Hospital Management System: database schema (MySQL 8.0 or newer)
--
-- 13 tables in third normal form. Every table has a surrogate primary key and
-- every relationship is a foreign key, so MySQL itself rejects orphan rows
-- (for example an appointment for a doctor who does not exist).
--
-- Rules the database enforces on its own, even if the Java code had a bug:
--   * CHECK constraints: no negative prices, stock or quantities
--   * UNIQUE keys: usernames, room numbers, one booking per doctor per slot,
--     one open admission per patient
--   * Prices are copied onto each charge row (admission, lab test, dispense) when
--     it happens, so later price changes never rewrite old bills.

-- Login accounts. The role decides what the user can do in the application.
CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM ('ADMIN', 'DOCTOR', 'PATIENT') NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS departments (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(80) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    CONSTRAINT uq_departments_name UNIQUE (name)
);

-- A doctor may have a login (user_id); UNIQUE keeps it one login per doctor.
CREATE TABLE IF NOT EXISTS doctors (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    department_id  INT          NOT NULL,
    phone          VARCHAR(15)  NOT NULL,
    email          VARCHAR(100),
    user_id        INT,
    CONSTRAINT uq_doctors_user UNIQUE (user_id),
    CONSTRAINT fk_doctors_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_doctors_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Date of birth is stored instead of age, because age is derived and changes every year.
CREATE TABLE IF NOT EXISTS patients (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    gender        ENUM ('MALE', 'FEMALE', 'OTHER') NOT NULL,
    date_of_birth DATE         NOT NULL,
    phone         VARCHAR(15)  NOT NULL,
    email         VARCHAR(100),
    address       VARCHAR(255),
    user_id       INT,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_patients_user UNIQUE (user_id),
    CONSTRAINT fk_patients_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_patients_name (name)
);

CREATE TABLE IF NOT EXISTS rooms (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10)   NOT NULL,
    type        ENUM ('GENERAL', 'SEMI_PRIVATE', 'PRIVATE', 'ICU') NOT NULL,
    daily_rate  DECIMAL(10, 2) NOT NULL,
    status      ENUM ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT uq_rooms_number UNIQUE (room_number),
    CONSTRAINT chk_rooms_rate CHECK (daily_rate >= 0)
);

-- One bill collects charges from three modules: room stays, pharmacy and lab.
-- total is a generated column, so it can never disagree with its three parts.
CREATE TABLE IF NOT EXISTS bills (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    patient_id       INT            NOT NULL,
    room_charges     DECIMAL(10, 2) NOT NULL DEFAULT 0,
    medicine_charges DECIMAL(10, 2) NOT NULL DEFAULT 0,
    lab_charges      DECIMAL(10, 2) NOT NULL DEFAULT 0,
    total            DECIMAL(12, 2) AS (room_charges + medicine_charges + lab_charges) STORED,
    status           ENUM ('UNPAID', 'PAID') NOT NULL DEFAULT 'UNPAID',
    created_at       DATETIME       NOT NULL,
    paid_at          DATETIME,
    CONSTRAINT fk_bills_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
);

-- open_patient_id equals patient_id while the patient is still admitted and becomes
-- NULL on discharge. A UNIQUE key allows any number of NULLs, so a patient can have
-- many past admissions but only one open admission.
CREATE TABLE IF NOT EXISTS admissions (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    patient_id      INT            NOT NULL,
    room_id         INT            NOT NULL,
    doctor_id       INT            NOT NULL,
    reason          VARCHAR(255)   NOT NULL,
    daily_rate      DECIMAL(10, 2) NOT NULL,
    admitted_at     DATETIME       NOT NULL,
    discharged_at   DATETIME,
    bill_id         INT,
    open_patient_id INT AS (IF(discharged_at IS NULL, patient_id, NULL)) STORED,
    CONSTRAINT uq_admissions_one_open UNIQUE (open_patient_id),
    CONSTRAINT chk_admissions_dates CHECK (discharged_at IS NULL OR discharged_at >= admitted_at),
    CONSTRAINT fk_admissions_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_admissions_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT fk_admissions_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_admissions_bill FOREIGN KEY (bill_id) REFERENCES bills (id)
);

-- holds_slot is 1 while the booking holds its time slot and NULL once it is cancelled,
-- so the UNIQUE key stops double booking but lets a cancelled slot be booked again.
CREATE TABLE IF NOT EXISTS appointments (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    patient_id       INT          NOT NULL,
    doctor_id        INT          NOT NULL,
    appointment_date DATE         NOT NULL,
    slot             TIME         NOT NULL,
    reason           VARCHAR(255) NOT NULL,
    status           ENUM ('SCHEDULED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    holds_slot       TINYINT AS (IF(status = 'CANCELLED', NULL, 1)) STORED,
    CONSTRAINT uq_appointments_doctor_slot UNIQUE (doctor_id, appointment_date, slot, holds_slot),
    CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    INDEX idx_appointments_date (appointment_date)
);

CREATE TABLE IF NOT EXISTS medical_records (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    patient_id   INT           NOT NULL,
    doctor_id    INT           NOT NULL,
    visit_date   DATE          NOT NULL,
    diagnosis    VARCHAR(500)  NOT NULL,
    prescription VARCHAR(1000),
    notes        VARCHAR(1000),
    CONSTRAINT fk_records_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_records_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id)
);

-- Price list for lab tests.
CREATE TABLE IF NOT EXISTS lab_test_types (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(100)   NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT uq_lab_test_types_name UNIQUE (name),
    CONSTRAINT chk_lab_test_types_price CHECK (price >= 0)
);

CREATE TABLE IF NOT EXISTS lab_tests (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    patient_id   INT            NOT NULL,
    doctor_id    INT            NOT NULL,
    test_type_id INT            NOT NULL,
    price        DECIMAL(10, 2) NOT NULL,
    status       ENUM ('ORDERED', 'COMPLETED') NOT NULL DEFAULT 'ORDERED',
    result       VARCHAR(1000),
    ordered_at   DATETIME       NOT NULL,
    completed_at DATETIME,
    bill_id      INT,
    CONSTRAINT chk_lab_tests_price CHECK (price >= 0),
    CONSTRAINT fk_lab_tests_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_lab_tests_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_lab_tests_type FOREIGN KEY (test_type_id) REFERENCES lab_test_types (id),
    CONSTRAINT fk_lab_tests_bill FOREIGN KEY (bill_id) REFERENCES bills (id)
);

CREATE TABLE IF NOT EXISTS medicines (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100)   NOT NULL,
    category      VARCHAR(50)    NOT NULL,
    manufacturer  VARCHAR(100),
    unit_price    DECIMAL(10, 2) NOT NULL,
    stock         INT            NOT NULL DEFAULT 0,
    reorder_level INT            NOT NULL DEFAULT 20,
    CONSTRAINT uq_medicines_name UNIQUE (name),
    CONSTRAINT chk_medicines_price CHECK (unit_price >= 0),
    CONSTRAINT chk_medicines_stock CHECK (stock >= 0),
    CONSTRAINT chk_medicines_reorder CHECK (reorder_level >= 0)
);

CREATE TABLE IF NOT EXISTS medicine_dispenses (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    patient_id   INT            NOT NULL,
    medicine_id  INT            NOT NULL,
    quantity     INT            NOT NULL,
    unit_price   DECIMAL(10, 2) NOT NULL,
    dispensed_at DATETIME       NOT NULL,
    bill_id      INT,
    CONSTRAINT chk_dispenses_quantity CHECK (quantity > 0),
    CONSTRAINT fk_dispenses_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_dispenses_medicine FOREIGN KEY (medicine_id) REFERENCES medicines (id),
    CONSTRAINT fk_dispenses_bill FOREIGN KEY (bill_id) REFERENCES bills (id)
);
