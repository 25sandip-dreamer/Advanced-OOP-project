Healthcare Management System

A comprehensive desktop application for medication management and patient monitoring

================================================================================
TABLE OF CONTENTS
================================================================================
1. About The Project
2. Features
3. Technologies Used
4. Architecture
5. Installation
6. Configuration
7. Usage
8. Database Schema
9. Contributing
10. License
11. Contact
12. Acknowledgments

================================================================================
ABOUT THE PROJECT
================================================================================

The Healthcare Management System is a comprehensive JavaFX-based desktop 
application designed to streamline medication management and patient monitoring 
in healthcare facilities. It provides doctors and patients with a digital 
platform for prescription tracking, automated assessments, real-time 
communication, and intelligent analysis of patient responses.

Key Highlights:
- Secure Authentication - BCrypt password encryption with role-based access control
- Medication Tracking - Complete prescription lifecycle management
- AI-Powered Analysis - Automated patient response evaluation with risk classification
- Real-Time Chat - Direct doctor-patient communication
- Email Notifications - Automated reminders and alerts via SMTP
- PDF Reports - Professional assessment reports
- Multi-Threading - Responsive UI with background task processing
- SQL Injection Protection - PreparedStatements throughout

Problem Statement:
Traditional healthcare systems face challenges in manual medication tracking, 
lack of systematic patient monitoring, communication barriers between doctors 
and patients, delayed detection of adverse drug reactions, and administrative 
burden of manual scheduling. This system addresses these challenges through 
automation, intelligent analysis, and seamless communication.

================================================================================
FEATURES
================================================================================

For Doctors:
------------
Patient Management:
- Add, edit, view, and search patient records
- View complete medical history
- Track active medications per patient

Medication Prescription:
- Prescribe medications with full details (dosage, frequency, route)
- Automatic 7-day assessment scheduling
- Status tracking (Active/Completed/Discontinued)
- Email notification to patients

Assessment Review:
- View all patient assessments with risk levels
- AI-powered analysis with key findings
- Color-coded risk classification (LOW/MEDIUM/HIGH/CRITICAL)
- Add doctor notes and recommendations

Chat System:
- Initiate conversations with patients
- Real-time messaging with auto-refresh
- Message history and unread counts

Reports:
- Generate PDF assessment reports
- Comprehensive analysis summaries
- Printable and shareable

For Patients:
-------------
Medication Tracking:
- View active medications
- See prescription details and instructions
- Track assessment schedule

Assessment Completion:
- Complete 7-day medication assessments
- Dynamic questionnaires based on medication
- Easy-to-use interface

Doctor Communication:
- Send and receive messages
- Ask questions about medications
- Quick access to doctor

Email Notifications:
- Prescription notifications
- Assessment reminders
- Overdue warnings

For Administrators:
-------------------
User Management:
- Create doctor and patient accounts
- Manage user roles and permissions
- View system activity

================================================================================
TECHNOLOGIES USED
================================================================================

Core Technologies:
- Java 8+ - Primary programming language
- JavaFX - GUI framework for desktop UI
- MySQL 5.7+ - Relational database management
- Maven - Build automation and dependency management

Libraries & APIs:
- JDBC - Database connectivity
- JavaMail API 1.6.2 - Email notifications via SMTP
- BCrypt (jBCrypt) - Password hashing and encryption
- iText / Apache PDFBox - PDF report generation

Key Concepts Implemented:
- Multi-Threading - Background email sending, chat auto-refresh, scheduled tasks
- Networking - SMTP for emails, TCP/IP for database
- Design Patterns - MVC, DAO, Singleton, Factory
- Security - BCrypt hashing, PreparedStatements, input validation
- AI/Logic - Keyword-based analysis with weighted scoring

================================================================================
ARCHITECTURE
================================================================================

Three-Tier Architecture:
------------------------
Presentation Layer (JavaFX Controllers & UI)
    |
    v
Business Logic Layer (Services, AI Engine, Validation)
    |
    v
Data Access Layer (DAO Classes, Database)

Design Patterns:
- MVC (Model-View-Controller) - Separation of concerns
- DAO (Data Access Object) - Database abstraction
- Singleton - DatabaseConnection, SessionManager
- Factory - QuestionnaireGenerator

Package Structure:
------------------
com.healthcare/
  main/
    MainApp.java
  controller/
    LoginController.java
    MedicationController.java
    DoctorDashboardController.java
    PatientQuestionnaireController.java
    ChatController.java
    AIAnalysisEngine.java
    PDFReportGenerator.java
  model/
    User.java
    Patient.java
    Medication.java
    PatientMedication.java
    MedicationAssessment.java
    ChatConversation.java
    ChatMessage.java
  database/
    DatabaseConnection.java
    UserDAO.java
    PatientDAO.java
    MedicationDAO.java
    AssessmentDAO.java
    ChatDAO.java
    PasswordUtil.java
    SessionManager.java
  service/
    EmailService.java
    EmailConfig.java
    EmailScheduler.java
  test/
    TestEmail.java

================================================================================
INSTALLATION
================================================================================

Prerequisites:
- Java JDK 8 or higher
- MySQL Server 5.7+
- Maven 3.6+
- JavaFX SDK (if using Java 11+)

Step 1: Clone the Repository
-----------------------------
git clone https://github.com/yourusername/healthcare-management-system.git
cd healthcare-management-system

Step 2: Set Up Database
------------------------
1. Start MySQL Server

2. Create Database:
CREATE DATABASE health_record_db;
USE health_record_db;

3. Run Schema Script:
mysql -u root -p health_record_db < database/schema.sql

Or manually execute the SQL in database/schema.sql using MySQL Workbench 
or phpMyAdmin.

4. Verify Tables Created:
SHOW TABLES;
Should show: users, patients, medications_master, patient_medications, 
medication_assessment_responses, chat_conversations, chat_messages

Step 3: Configure Database Connection
--------------------------------------
Edit src/com/healthcare/database/DatabaseConnection.java:

private static final String URL = "jdbc:mysql://localhost:3306/health_record_db";
private static final String USER = "root";
private static final String PASSWORD = "your_password";

Step 4: Build Project
---------------------
mvn clean install

Step 5: Run Application
-----------------------
Using Maven:
mvn javafx:run

Using IDE:
- Open project in IntelliJ IDEA / Eclipse / NetBeans
- Run MainApp.java as Java Application

Using JAR:
java -jar target/healthcare-management-system-1.0.jar

================================================================================
CONFIGURATION
================================================================================

Email Configuration:
--------------------
To enable email notifications:

1. Get Gmail App Password:
   - Go to https://myaccount.google.com/
   - Enable 2-Step Verification
   - Generate App Password: Security > App passwords > Mail
   - Copy the 16-character password

2. Configure Email:

Option A: Run Configuration Wizard
java -cp target/classes com.healthcare.service.EmailConfig --wizard

Option B: Edit email.properties
Create email.properties in project root:

smtp.host=smtp.gmail.com
smtp.port=587
email.from=your-email@gmail.com
email.password=your-app-password
system.name=Healthcare Management System
email.enabled=true
smtp.use.tls=true

3. Test Email:
java -cp target/classes com.healthcare.test.TestEmail your-email@gmail.com

Default Login Credentials:
--------------------------
Admin Account:
- Username: admin
- Password: admin123

Sample Doctor Account:
- Username: doctor1
- Password: doctor123

Sample Patient Account:
- Username: patient1
- Password: patient123

WARNING: Change default passwords after first login!

================================================================================
USAGE
================================================================================

For Doctors:
------------
1. Login with doctor credentials
2. Add Patient:
   - Click "Add New Patient"
   - Fill patient details
   - Click "Save"
3. Prescribe Medication:
   - Click "Prescribe Medication"
   - Select patient and medication
   - Enter dosage, frequency, instructions
   - Click "Save"
   - System automatically schedules 7-day assessment
   - Email sent to patient
4. Review Assessments:
   - Click "Doctor Dashboard"
   - View pending assessments
   - Click assessment to see AI analysis
   - Review risk level and findings
   - Add doctor notes
5. Chat with Patient:
   - Click "Messages"
   - Click "New Chat"
   - Select patient
   - Send messages

For Patients:
-------------
1. Login with patient credentials
2. View Medications:
   - See active medications list
   - Check dosage and instructions
3. Complete Assessment:
   - Click pending assessment notification
   - Answer all questions
   - Click "Submit"
   - Receive confirmation
4. Chat with Doctor:
   - Click "Messages"
   - View conversation with doctor
   - Send messages
   - Receive replies

For Administrators:
-------------------
1. Login with admin credentials
2. Create User Account:
   - Click "Register New User"
   - Enter username, password, role
   - Click "Register"
3. Manage System:
   - View all patients
   - View all users
   - Monitor system activity

================================================================================
DATABASE SCHEMA
================================================================================

Entity Relationship Diagram:

Users -----> PatientMedications <----- Patients
                    |                       |
                    v                       v
              Assessments           Conversations
                                          |
                                          v
                                      Messages

Key Tables:
-----------
- users - User accounts (Admin, Doctor, Patient)
- patients - Patient demographics and medical history
- medications_master - Medication catalog
- patient_medications - Prescriptions
- medication_assessment_responses - Patient assessments and AI analysis
- chat_conversations - Doctor-patient conversations
- chat_messages - Chat messages

See database/schema.sql for complete schema.

================================================================================
API DOCUMENTATION
================================================================================

Key Classes:

UserDAO:
- User getUserByUsername(String username)
- boolean registerUser(User user)
- List<User> getAllUsers()

PatientDAO:
- boolean addPatient(Patient patient)
- List<Patient> getAllPatients()
- Patient getPatientById(int id)
- boolean updatePatient(Patient patient)

MedicationDAO:
- boolean prescribeMedication(PatientMedication pm)
- List<PatientMedication> getActiveMedications()
- boolean updateMedicationStatus(int id, String status)

ChatDAO:
- int getOrCreateConversation(int patientId, int doctorId)
- boolean sendMessage(ChatMessage message)
- List<ChatMessage> getMessages(int conversationId)

EmailService:
- boolean sendMedicationPrescribedEmail(...)
- boolean sendAssessmentReminderEmail(...)
- boolean sendCriticalAlertEmail(...)

================================================================================
CONTRIBUTING
================================================================================

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch:
   git checkout -b feature/AmazingFeature
3. Commit your changes:
   git commit -m 'Add some AmazingFeature'
4. Push to the branch:
   git push origin feature/AmazingFeature
5. Open a Pull Request

Coding Standards:
- Follow Java naming conventions
- Add JavaDoc comments for public methods
- Use meaningful variable names
- Write unit tests for new features
- Update README if adding new features

================================================================================
LICENSE
================================================================================

This project is licensed under the MIT License - see the LICENSE file for details.

================================================================================
CONTACT
================================================================================

 Name - Sandip Basak Rudro
email - srbasak109@gmail.com


================================================================================
ACKNOWLEDGMENTS
================================================================================

- JavaFX Documentation (https://openjfx.io/) - GUI framework
- MySQL Documentation (https://dev.mysql.com/doc/) - Database
- JavaMail API (https://javaee.github.io/javamail/) - Email functionality
- jBCrypt (https://www.mindrot.org/projects/jBCrypt/) - Password encryption
- Stack Overflow (https://stackoverflow.com/) - Community support
- GitHub (https://github.com/) - Version control and hosting
- Course Instructor - Guidance and support

================================================================================
ACADEMIC PROJECT
================================================================================

This project was developed as part of [Advanced object-oriented programming] at [United International University] 
under the supervision of [Rizvan Jawad Ruhan].

Academic Year: 2024-2025
Course: [CSE 2118] - [Advanced Object-Oriented Programming]

================================================================================
FUTURE ENHANCEMENTS
================================================================================

- Mobile application (iOS/Android)
- Web-based version
- Cloud deployment (AWS/Azure)
- Advanced ML-based analysis
- Telemedicine integration
- Multi-language support
- Dark mode UI theme
- Appointment scheduling
- Billing integration
- Multi-clinic support

================================================================================
DISCLAIMER
================================================================================

This is an educational project developed for academic purposes. It is not 
intended for production use in real healthcare environments without proper 
medical validation, security audits, and compliance with healthcare 
regulations (HIPAA, GDPR, etc.).

================================================================================

Made with love by [Sandip Basak]

Star this repo if you found it helpful!

================================================================================
