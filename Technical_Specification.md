# Hospital Management System - Technical Specification

## Core Technologies

### 1. Frontend
- **Java Swing** - Primary GUI framework for the desktop application
  - Used for all user interfaces and forms
  - Handles user input and displays data
  - Components used: JFrame, JPanel, JTable, JButton, JTextField, etc.

### 2. Backend
- **Java SE** - Core programming language
  - Object-oriented programming
  - Exception handling
  - File I/O operations

- **JDBC (Java Database Connectivity)**
  - Manages database connections
  - Executes SQL queries
  - Handles database transactions

- **SQL (via database_setup.sql)**
  - Database schema definition
  - Table creation and relationships
  - Initial data population

## Key Components and Their Purposes

### Frontend Components
1. **Login System** (`Login.java`)
   - User authentication
   - Role-based access control

2. **Patient Management**
   - `NEW_PATIENT.java` - New patient registration
   - `update_patient_details.java` - Update patient information
   - `All_Patient_info.java` - View all patients
   - `patient_discharge.java` - Handle patient discharge process

3. **Appointment System**
   - `AppointmentBooking.java` - Schedule new appointments
   - `ViewAppointments.java` - View and manage appointments

4. **Medical Services**
   - `MedicalRecords.java` - Maintain patient medical history
   - `LabTests.java` - Manage laboratory tests
   - `Pharmacy.java` - Handle medication and prescriptions
   - `Room.java` & `SearchRoom.java` - Manage hospital rooms
   - `Ambulance.java` - Ambulance service management

5. **Administration**
   - `Employee_info.java` - Staff management
   - `Department.java` - Department management
   - `Billing.java` - Financial transactions and invoices

### Backend Components
1. **Database Connection** (`conn.java`)
   - Establishes connection to the database
   - Manages database resources

2. **Data Access**
   - Direct SQL queries in each component
   - `DBResultSet.java` - Handles database result sets

3. **Business Logic**
   - Implemented within each GUI class
   - Handles data validation
   - Processes user inputs
   - Manages application flow

## Architecture
- **Type**: Desktop Application (2-tier architecture)
  - Presentation Layer: Java Swing
  - Data Layer: Database (via JDBC)
- **Pattern**: Model-View-Controller (MVC) inspired
  - View: Swing components
  - Controller: Event handlers in each class
  - Model: Database tables and Java classes

## External Dependencies
- **ResultSet2xml.jar**
  - Converts database ResultSet to XML format
  - Used for data transformation and reporting

## Build and Deployment
- **Build Tool**: Basic batch/shell scripts
  - `compile.bat` - Compiles all Java files
  - `run_hospital.bat`/`run_hospital.sh` - Launches the application
