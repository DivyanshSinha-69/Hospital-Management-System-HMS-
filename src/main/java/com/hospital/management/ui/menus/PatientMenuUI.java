package com.hospital.management.ui.menus;

import com.hospital.management.common.enums.PaymentMethod;
import com.hospital.management.controllers.PatientController;
import com.hospital.management.models.*;
import com.hospital.management.services.impl.UserServiceImpl;
import com.hospital.management.ui.InputHandler;
import com.hospital.management.commands.CommandResult;
import com.hospital.management.common.utils.InputValidator;
import com.hospital.management.common.enums.UserRole;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Map;
import java.util.List;

/**
 * Patient Menu UI with login/logout functionality
 */
public class PatientMenuUI {
    private final PatientController patientController;
    private final UserServiceImpl userService;
    private final InputHandler input;

    // Session management
    private User currentUser = null;
    private boolean isLoggedIn = false;

    public PatientMenuUI(PatientController patientController) {
        this.patientController = patientController;
        this.userService = new UserServiceImpl();
        this.input = InputHandler.getInstance();
    }

    public void show() {
        while (true) {
            if (!isLoggedIn) {
                // Show login/register menu
                if (!showLoginMenu()) {
                    return; // Back to main menu
                }
            } else {
                // Show patient dashboard
                if (!showPatientDashboard()) {
                    return; // Logout and back to main menu
                }
            }
        }
    }

    private boolean showLoginMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("👤 PATIENT PORTAL");
        System.out.println("=".repeat(50));
        System.out.println("1. 📝 Register New Account");
        System.out.println("2. 🔑 Login to Existing Account");
        System.out.println("3. ❓ Forgot Password");
        System.out.println("0. ⬅️  Back to Main Menu");
        System.out.println("=".repeat(50));

        int choice = input.getInt("Select an option (0-3): ", 0, 3);

        switch (choice) {
            case 1 -> handleRegistration();
            case 2 -> handleLogin();
            case 3 -> handleForgotPassword();
            case 0 -> { return false; }
            default -> System.out.println("❌ Invalid option.");
        }
        return true;
    }

    private boolean showPatientDashboard() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("👤 PATIENT DASHBOARD - Welcome " + currentUser.getUsername() + "!");
        System.out.println("=".repeat(50));
        System.out.println("1. 📅 Book Appointment");
        System.out.println("2. 👁️  View My Appointments");
        System.out.println("3. 📋 Update Profile");
        System.out.println("4. 💰 View Bills & Payments");
        System.out.println("9. 🔧 Account Settings");
        System.out.println("0. 🚪 Logout");
        System.out.println("=".repeat(50));

        int choice = input.getInt("Select an option (0-9): ", 0, 9);

        switch (choice) {
            case 1 -> handleBookAppointment();
            case 2 -> handleViewAppointments();
            case 3 -> handleUpdateProfile();
            case 4 -> handleViewBills();
            case 9 -> handleAccountSettings();
            case 0 -> {
                handleLogout();
                return false;
            }
            default -> System.out.println("❌ Invalid option.");
        }
        return true;
    }

    private void handleLogin() {
        System.out.println("\n🔑 PATIENT LOGIN");
        System.out.println("=" .repeat(20));

        try {
            String loginId = input.getString("👤 Username: ");
            String password = input.getPasswordInput("🔑 Password: ");

            if (loginId.isEmpty() || password.isEmpty()) {
                System.out.println("❌ Username and password are required");
                return;
            }

            System.out.println("\n🔄 Authenticating...");

            // Authenticate using UserService
            boolean authenticated = userService.authenticate(loginId, password);

            if (authenticated) {
                // Get user details
                Optional<User> userOpt = userService.findUserByUsername(loginId);
                if (userOpt.isEmpty()) {
                    // Try finding by email if username didn't work
                    userOpt = userService.findAllUsers().stream()
                            .filter(u -> u.getEmail().equals(loginId))
                            .findFirst();
                }

                if (userOpt.isPresent() && userOpt.get().getRole() == UserRole.PATIENT) {
                    currentUser = userOpt.get();
                    isLoggedIn = true;
                    System.out.println("✅ Login successful!");
                    System.out.println("👋 Welcome back, " + currentUser.getUsername() + "!");
                } else {
                    System.out.println("❌ Access denied: Not a patient account");
                }
            } else {
                System.out.println("❌ Login failed: Invalid credentials");
                System.out.println("💡 Please check your username/email and password");
            }

        } catch (Exception e) {
            System.out.println("❌ Login error: " + e.getMessage());
        }
    }

    private void handleLogout() {
        System.out.println("\n🔄 Logging out...");
        System.out.println("👋 Goodbye, " + (currentUser != null ? currentUser.getUsername() : "User") + "!");
        currentUser = null;
        isLoggedIn = false;
        System.out.println("✅ Logged out successfully!");
    }

    private void handleRegistration() {
        System.out.println("\n📝 PATIENT REGISTRATION");
        System.out.println("=" .repeat(30));

        try {
            // Collect basic required information
            String firstName = getValidatedInput("👤 First Name: ", InputValidator::isValidName);
            String lastName = getValidatedInput("👤 Last Name: ", InputValidator::isValidName);
            String email = getValidatedInput("📧 Email: ", InputValidator::isValidEmail);
            String phone = getValidatedInput("📱 Phone Number: ", InputValidator::isValidPhone);
            String username = getValidatedInput("👤 Username: ", InputValidator::isValidUsername);

            // ✅ FIX: Validate the original password, not confirm password
            String password = getValidatedPassword("🔑 Password: ");
            String confirmPassword = input.getPasswordInput("🔑 Confirm Password: ");


            if (!password.equals(confirmPassword)) {
                System.out.println("❌ Passwords do not match");
                return;
            }

            // Get date of birth
            LocalDate dateOfBirth = getDateInput("📅 Date of Birth (YYYY-MM-DD): ");

            // Get gender
            Patient.Gender gender = getGenderInput();

            // Ask if user wants to provide additional info
            System.out.print("📋 Would you like to provide additional information? (y/n): ");
            String addInfo = input.getString("").toLowerCase();

            CommandResult result;

            if (addInfo.equals("y") || addInfo.equals("yes")) {
                // Collect additional information
                String bloodGroup = input.getString("🩸 Blood Group (optional): ");
                if (bloodGroup.trim().isEmpty()) bloodGroup = null;

                String address = input.getString("🏠 Address (optional): ");
                if (address.trim().isEmpty()) address = null;

                String emergencyContactName = input.getString("👤 Emergency Contact Name (optional): ");
                if (emergencyContactName.trim().isEmpty()) emergencyContactName = null;

                String emergencyContactPhone = input.getString("📞 Emergency Contact Phone (optional): ");
                if (emergencyContactPhone.trim().isEmpty()) emergencyContactPhone = null;

                System.out.println("\n🔄 Processing full registration...");

                // Call PatientController with full registration
                result = patientController.registerPatientFull(username, password, email, phone,
                        firstName, lastName, dateOfBirth, gender,
                        bloodGroup, address, emergencyContactName, emergencyContactPhone);
            } else {
                System.out.println("\n🔄 Processing basic registration...");

                // Call PatientController with basic registration
                result = patientController.registerPatient(username, password, email, phone,
                        firstName, lastName, dateOfBirth, gender);
            }

            // Handle result
            if (result.isSuccess()) {
                System.out.println("✅ " + result.getMessage());
                System.out.println("📧 Account created for: " + firstName + " " + lastName);
                System.out.println("👤 Username: " + username);
                System.out.println("💡 You can now login with your credentials");
            } else {
                System.out.println("❌ " + result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ Registration error: " + e.getMessage());
        }
    }

    private void handleBookAppointment() {
        System.out.println("\n📅 BOOK APPOINTMENT");
        System.out.println("=" .repeat(25));

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            // ✅ FIX: Get the actual patient ID from database
            Long userId = currentUser.getId();
            System.out.println("🔍 Debug: User ID = " + userId);

            // Query to get the actual patient ID
            Long patientId = getPatientIdFromDatabase(userId);
            if (patientId == null) {
                System.out.println("❌ Patient record not found for user ID: " + userId);
                return;
            }

            System.out.println("🔍 Debug: Patient ID = " + patientId);

            // Step 1: Select Department
            System.out.println("\n🏥 STEP 1: Select Department");
            CommandResult deptResult = patientController.getAllDepartments();
            if (!deptResult.isSuccess()) {
                System.out.println("❌ " + deptResult.getMessage());
                return;
            }

            @SuppressWarnings("unchecked")
            List<Department> departments = (List<Department>) deptResult.getData();

            if (departments.isEmpty()) {
                System.out.println("❌ No departments available");
                return;
            }

            System.out.println("\nAvailable Departments:");
            for (int i = 0; i < departments.size(); i++) {
                Department dept = departments.get(i);
                System.out.printf("%d. %s - %s%n", i + 1, dept.getName(), dept.getDescription());
            }

            int deptChoice = input.getInt("Select department (1-" + departments.size() + "): ", 1, departments.size());
            Department selectedDept = departments.get(deptChoice - 1);

            // Step 2: Select Doctor
            System.out.println("\n👨‍⚕️ STEP 2: Select Doctor from " + selectedDept.getName());
            CommandResult doctorResult = patientController.getDoctorsByDepartment(selectedDept.getId());
            if (!doctorResult.isSuccess()) {
                System.out.println("❌ " + doctorResult.getMessage());
                return;
            }

            @SuppressWarnings("unchecked")
            List<Doctor> doctors = (List<Doctor>) doctorResult.getData();

            if (doctors.isEmpty()) {
                System.out.println("❌ No doctors available in this department");
                return;
            }

            System.out.println("\nAvailable Doctors:");
            for (int i = 0; i < doctors.size(); i++) {
                Doctor doctor = doctors.get(i);
                System.out.printf("%d. Dr. %s %s - %s (Fee: ₹%s)%n",
                        i + 1, doctor.getFirstName(), doctor.getLastName(),
                        doctor.getSpecialization(), doctor.getConsultationFee());
            }

            int doctorChoice = input.getInt("Select doctor (1-" + doctors.size() + "): ", 1, doctors.size());
            Doctor selectedDoctor = doctors.get(doctorChoice - 1);

            // Step 3: Select Date
            System.out.println("\n📅 STEP 3: Select Appointment Date");
            LocalDate appointmentDate = getDateInput("Enter appointment date (YYYY-MM-DD): ");

            // Step 4: Select Time Slot
            System.out.println("\n⏰ STEP 4: Select Time Slot");
            CommandResult slotsResult = patientController.getAvailableTimeSlots(selectedDoctor.getId(), appointmentDate);
            if (!slotsResult.isSuccess()) {
                System.out.println("❌ " + slotsResult.getMessage());
                return;
            }

            @SuppressWarnings("unchecked")
            List<LocalTime> availableSlots = (List<LocalTime>) slotsResult.getData();

            if (availableSlots.isEmpty()) {
                System.out.println("❌ No time slots available for the selected date");
                return;
            }

            System.out.println("\nAvailable Time Slots:");
            for (int i = 0; i < availableSlots.size(); i++) {
                LocalTime slot = availableSlots.get(i);
                System.out.printf("%d. %s%n", i + 1, slot.toString());
            }

            int timeChoice = input.getInt("Select time slot (1-" + availableSlots.size() + "): ", 1, availableSlots.size());
            LocalTime selectedTime = availableSlots.get(timeChoice - 1);

            // Step 5: Enter reason (optional)
            String reason = input.getString("\n📝 Reason for appointment (optional): ");
            if (reason.trim().isEmpty()) reason = null;

            // Step 6: Show bill summary
            BigDecimal consultationFee = selectedDoctor.getConsultationFee();
            BigDecimal taxAmount = consultationFee.multiply(BigDecimal.valueOf(0.18));
            BigDecimal totalAmount = consultationFee.add(taxAmount);

            System.out.println("\n💰 BILL SUMMARY:");
            System.out.println("═".repeat(40));
            System.out.println("Consultation Fee: ₹" + consultationFee);
            System.out.println("Tax (18%):        ₹" + taxAmount);
            System.out.println("─".repeat(40));
            System.out.println("Total Amount:     ₹" + totalAmount);
            System.out.println("═".repeat(40));

            // ✅ FIX: Step 7: Select Payment Method
            System.out.println("\n💳 STEP 7: Select Payment Method");
            System.out.println("1. 💵 Cash");
            System.out.println("2. 💳 Credit Card");
            System.out.println("3. 💳 Debit Card");
            System.out.println("4. 📱 UPI");

            int paymentMethodChoice = input.getInt("Select payment method (1-4): ", 1, 4);

            PaymentMethod selectedPaymentMethod = switch (paymentMethodChoice) {
                case 1 -> PaymentMethod.CASH;
                case 2 -> PaymentMethod.CREDIT_CARD;
                case 3 -> PaymentMethod.DEBIT_CARD;
                case 4 -> PaymentMethod.UPI;
                default -> PaymentMethod.CASH;
            };

            String paymentMethodName = switch (paymentMethodChoice) {
                case 1 -> "Cash";
                case 2 -> "Credit Card";
                case 3 -> "Debit Card";
                case 4 -> "UPI";
                default -> "Cash";
            };

            System.out.println("✅ Selected Payment Method: " + paymentMethodName);

            // Step 8: Final Payment Confirmation
            System.out.println("\n💳 Payment Confirmation:");
            System.out.println("1. ✅ Complete Payment");
            System.out.println("2. ❌ Go Back");

            int paymentChoice = input.getInt("Select option (1-2): ", 1, 2);
            if (paymentChoice == 2) {
                System.out.println("❌ Appointment booking cancelled");
                return;
            }

            // Process appointment with payment using the correct patientId
            System.out.println("\n🔄 Processing appointment and payment...");
            CommandResult result = patientController.bookAppointmentWithPayment(patientId, selectedDoctor.getId(),
                    appointmentDate, selectedTime, reason, selectedPaymentMethod);

            if (result.isSuccess()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) result.getData();

                Appointment appointment = (Appointment) data.get("appointment");
                Bill bill = (Bill) data.get("bill");
                Payment payment = (Payment) data.get("payment");

                System.out.println("✅ " + result.getMessage());
                System.out.println("\n📋 APPOINTMENT CONFIRMATION");
                System.out.println("═".repeat(40));
                System.out.println("📅 Appointment ID: " + appointment.getId());
                System.out.println("📅 Date & Time: " + appointmentDate + " at " + selectedTime);
                System.out.println("👨‍⚕️ Doctor: Dr. " + selectedDoctor.getFirstName() + " " + selectedDoctor.getLastName());
                System.out.println("💳 Payment Method: " + paymentMethodName);
                System.out.println("💳 Payment ID: " + ((Payment) payment).getId());
                System.out.println("💰 Amount Paid: ₹" + totalAmount);
                System.out.println("═".repeat(40));
            } else {
                System.out.println("❌ " + result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ Booking error: " + e.getMessage());
            e.printStackTrace(); // ✅ ADD: Debug stack trace
        }
    }

    // ✅ ADD THIS HELPER METHOD:
    private Long getPatientIdFromDatabase(Long userId) {
        try {
            String sql = "SELECT p.id FROM patients p WHERE p.user_id = ?";
            try (java.sql.Connection conn = com.hospital.management.common.config.DatabaseConfig.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, userId);
                java.sql.ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Database error getting patient ID: " + e.getMessage());
        }
        return null;
    }

    // ... REST OF YOUR EXISTING METHODS REMAIN THE SAME ...

    private void handleViewAppointments() {
        System.out.println("📅 VIEW APPOINTMENTS");
        System.out.println("═".repeat(20));

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            Long patientId = currentUser.getId();
            System.out.println("🔍 Fetching your appointments...");

            // Call PatientController to view appointments
            CommandResult result = patientController.viewAppointments(patientId);

            // Handle result
            if (result.isSuccess()) {
                System.out.println("✅ " + result.getMessage());

                // Extract and display appointments from result data
                if (result.getData() != null) {
                    @SuppressWarnings("unchecked")
                    List<Appointment> appointments = (List<Appointment>) result.getData();

                    if (appointments.isEmpty()) {
                        System.out.println("📋 No appointments found.");
                    } else {
                        displayAppointmentDetails(appointments);
                    }
                } else {
                    System.out.println("📋 No appointments found.");
                }
            } else {
                System.out.println("❌ " + result.getMessage());
            }
        } catch (Exception e) {
            System.out.println("❌ View appointments error: " + e.getMessage());
        }
    }

    // ADD THIS NEW METHOD to display appointment details
    private void displayAppointmentDetails(List<Appointment> appointments) {
        System.out.println("\n📅 YOUR APPOINTMENTS (" + appointments.size() + " found)");
        System.out.println("═".repeat(80));

        for (int i = 0; i < appointments.size(); i++) {
            Appointment apt = appointments.get(i);
            System.out.printf("%d. 🏥 Appointment ID: %d%n", (i + 1), apt.getId());
            System.out.printf("   📅 Date: %s at %s%n", apt.getAppointmentDate(), apt.getAppointmentTime());
            System.out.printf("   👨‍⚕️ Doctor ID: %d%n", apt.getDoctorId());
            System.out.printf("   📋 Status: %s%n", apt.getStatus());
            System.out.printf("   📝 Reason: %s%n", apt.getReason() != null ? apt.getReason() : "Not specified");
            System.out.printf("   🗓️ Created: %s%n", apt.getCreatedAt() != null ? apt.getCreatedAt().toLocalDate() : "N/A");
            System.out.println("   " + "─".repeat(76));
        }

        System.out.println("═".repeat(80));
        input.getString("Press Enter to continue...");
    }


    // Placeholder methods for future implementation
    private void handleForgotPassword() {
        System.out.println("\n🔑 PASSWORD RECOVERY");
        System.out.println("📧 Enter your registered email to receive password reset instructions:");
        String email = input.getString("Email: ");
        System.out.println("✅ Password reset instructions sent to: " + email);
        System.out.println("💡 Check your email for further instructions");
        System.out.println("🚧 Password recovery functionality - Coming soon!");
    }

    private void handleUpdateProfile() {
        System.out.println("\n📋 UPDATE PROFILE");
        System.out.println("=" .repeat(20));

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            Long patientId = currentUser.getId();

            System.out.println("📝 Update your profile information");
            System.out.println("💡 Leave fields blank to keep current values");
            System.out.println();

            // Collect updated information
            String firstName = input.getString("👤 First Name: ");
            String lastName = input.getString("👤 Last Name: ");
            String email = input.getString("📧 Email: ");
            String phone = input.getString("📱 Phone Number: ");

            // Validate basic fields if provided
            if (!firstName.isEmpty() && !InputValidator.isValidName(firstName)) {
                System.out.println("❌ Invalid first name format");
                return;
            }
            if (!lastName.isEmpty() && !InputValidator.isValidName(lastName)) {
                System.out.println("❌ Invalid last name format");
                return;
            }
            if (!email.isEmpty() && !InputValidator.isValidEmail(email)) {
                System.out.println("❌ Invalid email format");
                return;
            }
            if (!phone.isEmpty() && !InputValidator.isValidPhone(phone)) {
                System.out.println("❌ Invalid phone format");
                return;
            }

            // Ask if user wants to update additional information
            System.out.print("\n📋 Would you like to update additional medical information? (y/n): ");
            String updateMedical = input.getString("").toLowerCase();

            CommandResult result;

            if (updateMedical.equals("y") || updateMedical.equals("yes")) {
                // Collect additional medical information
                System.out.println("\n🏥 Medical Information Update:");

                LocalDate dateOfBirth = null;
                String dobInput = input.getString("📅 Date of Birth (YYYY-MM-DD, blank to skip): ");
                if (!dobInput.isEmpty()) {
                    try {
                        dateOfBirth = LocalDate.parse(dobInput);
                    } catch (Exception e) {
                        System.out.println("❌ Invalid date format, skipping date of birth update");
                    }
                }

                Patient.Gender gender = null;
                String genderInput = input.getString("⚧ Gender (MALE/FEMALE/OTHER, blank to skip): ");
                if (!genderInput.isEmpty()) {
                    try {
                        gender = Patient.Gender.valueOf(genderInput.toUpperCase());
                    } catch (Exception e) {
                        System.out.println("❌ Invalid gender, skipping gender update");
                    }
                }

                String bloodGroup = input.getString("🩸 Blood Group: ");
                String address = input.getString("🏠 Address: ");
                String emergencyContactName = input.getString("👤 Emergency Contact Name: ");
                String emergencyContactPhone = input.getString("📞 Emergency Contact Phone: ");

                System.out.println("\n🔄 Updating complete profile...");

                // Convert empty strings to null
                String firstNameParam = firstName.isEmpty() ? null : firstName;
                String lastNameParam = lastName.isEmpty() ? null : lastName;
                String emailParam = email.isEmpty() ? null : email;
                String phoneParam = phone.isEmpty() ? null : phone;
                String bloodGroupParam = bloodGroup.isEmpty() ? null : bloodGroup;
                String addressParam = address.isEmpty() ? null : address;
                String emergencyNameParam = emergencyContactName.isEmpty() ? null : emergencyContactName;
                String emergencyPhoneParam = emergencyContactPhone.isEmpty() ? null : emergencyContactPhone;

                // Call PatientController with full profile update
                result = patientController.updatePatientProfile(patientId, firstNameParam, lastNameParam,
                        emailParam, phoneParam, dateOfBirth, gender, bloodGroupParam, addressParam,
                        emergencyNameParam, emergencyPhoneParam);
            } else {
                System.out.println("\n🔄 Updating basic profile...");

                // Convert empty strings to null
                String firstNameParam = firstName.isEmpty() ? null : firstName;
                String lastNameParam = lastName.isEmpty() ? null : lastName;
                String emailParam = email.isEmpty() ? null : email;
                String phoneParam = phone.isEmpty() ? null : phone;

                // Call PatientController with basic profile update
                result = patientController.updatePatientProfile(patientId, firstNameParam,
                        lastNameParam, emailParam, phoneParam);
            }

            // Handle result
            if (result.isSuccess()) {
                System.out.println("✅ " + result.getMessage());
                System.out.println("📝 Profile updated successfully!");

                // Update current user info if email/phone was changed
                if (result.getData() instanceof User) {
                    User updatedUser = (User) result.getData();
                    currentUser = updatedUser;
                }
            } else {
                System.out.println("❌ " + result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ Update profile error: " + e.getMessage());
        }
    }

    private void handleViewBills() {
        System.out.println("💰 VIEW BILLS & PAYMENTS");
        System.out.println("═".repeat(30));

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            // ✅ FIX: Get the actual patient ID from database (same as appointments)
            Long userId = currentUser.getId();
            Long patientId = getPatientIdFromDatabase(userId);  // Convert user_id → patient_id

            if (patientId == null) {
                System.out.println("❌ Patient record not found for user ID " + userId);
                return;
            }

            System.out.println("🔍 Fetching your bills and payments...");

            // Call PatientController to get bills and payments
            CommandResult result = patientController.viewPatientBills(patientId);  // Use patient_id = 2

            if (result.isSuccess() && result.getData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) result.getData();

                @SuppressWarnings("unchecked")
                List<Bill> bills = (List<Bill>) data.get("bills");
                @SuppressWarnings("unchecked")
                List<Payment> payments = (List<Payment>) data.get("payments");

                displayBillsAndPayments(bills, payments);
            } else {
                System.out.println("❌ " + result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ View bills error: " + e.getMessage());
        }
    }

    private void displayBillsAndPayments(List<Bill> bills, List<Payment> payments) {
        System.out.println("\n" + "═".repeat(90));
        System.out.println("💰 YOUR BILLS & PAYMENTS SUMMARY");
        System.out.println("═".repeat(90));

        // Display Bills
        if (bills.isEmpty()) {
            System.out.println("📋 No bills found");
        } else {
            System.out.println("\n💳 BILLS:");
            System.out.println("─".repeat(90));
            System.out.printf("%-4s %-10s %-12s %-10s %-10s %-12s %-15s %-15s%n",
                    "ID", "Total", "Tax", "Discount", "Final", "Status", "Bill Date", "Due Date");
            System.out.println("─".repeat(90));

            for (Bill bill : bills) {
                System.out.printf("%-4s %-10s %-12s %-10s %-10s %-12s %-15s %-15s%n",
                        bill.getId(),
                        "₹" + bill.getTotalAmount(),
                        "₹" + bill.getTaxAmount(),
                        "₹" + bill.getDiscountAmount(),
                        "₹" + bill.getFinalAmount(),
                        bill.getStatus(),
                        bill.getBillDate(),
                        bill.getDueDate());
            }
            System.out.println("─".repeat(90));
        }

        System.out.println();

        // Display Payments
        if (payments.isEmpty()) {
            System.out.println("📋 No payments found");
        } else {
            System.out.println("💳 PAYMENTS:");
            System.out.println("─".repeat(90));
            System.out.printf("%-4s %-12s %-15s %-15s %-20s %-15s%n",
                    "ID", "Amount", "Method", "Status", "Transaction ID", "Date");
            System.out.println("─".repeat(90));

            for (Payment payment : payments) {
                String txnId = payment.getTransactionId() != null ?
                        (payment.getTransactionId().length() > 18 ?
                                payment.getTransactionId().substring(0, 15) + "..." :
                                payment.getTransactionId()) : "N/A";

                System.out.printf("%-4s %-12s %-15s %-15s %-20s %-15s%n",
                        payment.getId(),
                        payment.getFormattedAmount(),
                        payment.getDisplayMethod(),
                        payment.getDisplayStatus(),
                        txnId,
                        payment.getPaymentDate().toLocalDate());
            }
            System.out.println("─".repeat(90));
        }

        System.out.println("═".repeat(90));
        input.getString("Press Enter to continue...");
    }

    private void handleAccountSettings() {
        while (true) {
            System.out.println("\n🔧 ACCOUNT SETTINGS");
            System.out.println("=" .repeat(20));
            System.out.println("1. 👁️  View My Complete Profile");
            System.out.println("2. 🔑 Change Password");
            System.out.println("3. 📧 Change Email");
            System.out.println("4. 📱 Change Phone Number");
            System.out.println("5. ❌ Delete Account");
            System.out.println("0. ⬅️  Back to Dashboard");
            System.out.println("=" .repeat(30));

            int choice = input.getInt("Select an option (0-5): ", 0, 5);

            switch (choice) {
                case 1 -> handleViewCompleteProfile();
                case 2 -> handleChangePassword();
                case 3 -> handleChangeEmail();
                case 4 -> handleChangePhone();
                case 5 -> handleDeleteAccount();
                case 0 -> { return; }
                default -> System.out.println("❌ Invalid option.");
            }
        }
    }

    private void handleViewCompleteProfile() {
        System.out.println("\n👁️ MY COMPLETE PROFILE");
        System.out.println("=" .repeat(25));

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            Long patientId = currentUser.getId();

            System.out.println("🔄 Retrieving your complete profile...");

            // Call PatientController to get complete profile
            CommandResult result = patientController.viewPatientProfile(patientId);

            if (result.isSuccess() && result.getData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> profileData = (Map<String, Object>) result.getData();

                displayCompleteProfile(profileData);
            } else {
                System.out.println("❌ " + result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ View profile error: " + e.getMessage());
        }
    }

    private void displayCompleteProfile(Map<String, Object> profileData) {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("👤 COMPLETE PATIENT PROFILE");
        System.out.println("═".repeat(60));

        // Account Information
        System.out.println("🔐 ACCOUNT INFORMATION:");
        System.out.println("─".repeat(30));
        System.out.printf("%-20s: %s%n", "User ID", profileData.get("id"));
        System.out.printf("%-20s: %s%n", "Username", profileData.get("username"));
        System.out.printf("%-20s: %s%n", "Email", profileData.get("email"));
        System.out.printf("%-20s: %s%n", "Phone", profileData.get("phone"));
        System.out.printf("%-20s: %s%n", "Role", profileData.get("role"));
        System.out.printf("%-20s: %s%n", "Account Status",
                (Boolean) profileData.get("isActive") ? "Active" : "Inactive");
        System.out.printf("%-20s: %s%n", "Member Since", profileData.get("createdAt"));
        System.out.printf("%-20s: %s%n", "Last Updated", profileData.get("updatedAt"));

        System.out.println();

        // Personal Information
        System.out.println("👤 PERSONAL INFORMATION:");
        System.out.println("─".repeat(30));
        System.out.printf("%-20s: %s%n", "Full Name",
                (profileData.get("firstName") != null && profileData.get("lastName") != null)
                        ? profileData.get("firstName") + " " + profileData.get("lastName")
                        : "Not provided");
        System.out.printf("%-20s: %s%n", "Date of Birth",
                profileData.get("dateOfBirth") != null ? profileData.get("dateOfBirth") : "Not provided");
        System.out.printf("%-20s: %s%n", "Age",
                profileData.get("age") != null ? profileData.get("age") + " years" : "Not calculated");
        System.out.printf("%-20s: %s%n", "Gender",
                profileData.get("gender") != null ? profileData.get("gender") : "Not provided");

        System.out.println();

        // Medical Information
        System.out.println("🏥 MEDICAL INFORMATION:");
        System.out.println("─".repeat(30));
        System.out.printf("%-20s: %s%n", "Blood Group",
                profileData.get("bloodGroup") != null ? profileData.get("bloodGroup") : "Not provided");
        System.out.printf("%-20s: %s%n", "Insurance Number",
                profileData.get("insuranceNumber") != null ? profileData.get("insuranceNumber") : "Not provided");
        System.out.printf("%-20s: %s%n", "Medical History",
                profileData.get("medicalHistory") != null ? profileData.get("medicalHistory") : "None recorded");
        System.out.printf("%-20s: %s%n", "Allergies",
                profileData.get("allergies") != null ? profileData.get("allergies") : "None recorded");

        System.out.println();

        // Contact Information
        System.out.println("📞 CONTACT INFORMATION:");
        System.out.println("─".repeat(30));
        System.out.printf("%-20s: %s%n", "Address",
                profileData.get("address") != null ? profileData.get("address") : "Not provided");
        System.out.printf("%-20s: %s%n", "Emergency Contact",
                profileData.get("emergencyContactName") != null ? profileData.get("emergencyContactName") : "Not provided");
        System.out.printf("%-20s: %s%n", "Emergency Phone",
                profileData.get("emergencyContactPhone") != null ? profileData.get("emergencyContactPhone") : "Not provided");

        System.out.println("═".repeat(60));

        // Wait for user to read
        input.getString("Press Enter to continue...");
    }

    // Placeholder methods for other account settings
    private void handleChangePassword() {
        System.out.println("\n🔐 CHANGE PASSWORD");
        System.out.println("─".repeat(20));

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            // Get current password for verification
            String currentPassword = input.getPasswordInput("🔒 Enter current password: ");
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                System.out.println("❌ Current password is required");
                return;
            }

            // Verify current password
            boolean isValidPassword = userService.verifyPassword(currentUser.getUsername(), currentPassword);
            if (!isValidPassword) {
                System.out.println("❌ Current password is incorrect");
                return;
            }

            // Get new password
            String newPassword = input.getPasswordInput("🔑 Enter new password: ");
            if (newPassword == null || newPassword.trim().length() < 6) {
                System.out.println("❌ New password must be at least 6 characters long");
                return;
            }

            // Confirm new password
            String confirmPassword = input.getPasswordInput("🔑 Confirm new password: ");
            if (!newPassword.equals(confirmPassword)) {
                System.out.println("❌ Passwords do not match");
                return;
            }

            System.out.println("🔄 Updating password...");

            // Update password using UserService
            boolean success = userService.updatePassword(currentUser.getId(), newPassword);

            if (success) {
                System.out.println("✅ Password updated successfully!");
                System.out.println("🔒 Please use your new password for future logins");
            } else {
                System.out.println("❌ Failed to update password");
            }

        } catch (Exception e) {
            System.out.println("❌ Error updating password: " + e.getMessage());
        }

        input.getString("\nPress Enter to continue...");
    }


    private void handleChangeEmail() {
        System.out.println("📧 CHANGE EMAIL - Coming soon!");
        System.out.println("🚧 This feature will allow you to update your email address");
    }

    private void handleChangePhone() {
        System.out.println("📱 CHANGE PHONE - Coming soon!");
        System.out.println("🚧 This feature will allow you to update your phone number");
    }

    private void handleDeleteAccount() {
        System.out.println("❌ DELETE ACCOUNT - Coming soon!");
        System.out.println("⚠️  This feature will allow you to permanently delete your account");
        System.out.println("🔒 For security reasons, this requires admin approval");
    }

    // Helper methods for input validation (same as before)
    private String getValidatedInput(String prompt, java.util.function.Predicate<String> validator) {
        while (true) {
            String input = this.input.getString(prompt);
            if (validator.test(input)) {
                return input;
            }
            System.out.println("❌ Invalid input format. Please try again.");
        }
    }

    private String getValidatedPassword(String prompt) {
        while (true) {
            // ✅ This will use the password masking from InputHandler
            String password = input.getPasswordInput(prompt);
            if (InputValidator.isValidPassword(password)) {
                return password;
            }
            System.out.println("❌ Password must be at least 8 characters with uppercase, lowercase, digit, and special character.");
        }
    }

    private LocalDate getDateInput(String prompt) {
        while (true) {
            try {
                String dateStr = input.getString(prompt);
                return LocalDate.parse(dateStr);
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid date format. Please use YYYY-MM-DD format.");
            }
        }
    }

    private Patient.Gender getGenderInput() {
        while (true) {
            try {
                System.out.println("⚧ Gender options: MALE, FEMALE, OTHER");
                String genderStr = input.getString("Enter gender: ").toUpperCase();
                return Patient.Gender.valueOf(genderStr);
            } catch (IllegalArgumentException e) {
                System.out.println("❌ Invalid gender. Please enter MALE, FEMALE, or OTHER.");
            }
        }
    }

    private LocalTime getTimeInput() {
        System.out.println("\n⏰ Available Time Slots:");
        System.out.println("1. 09:00 AM");
        System.out.println("2. 10:00 AM");
        System.out.println("3. 11:00 AM");
        System.out.println("4. 02:00 PM");
        System.out.println("5. 03:00 PM");
        System.out.println("6. 04:00 PM");

        int timeChoice = input.getInt("Select time slot (1-6): ", 1, 6);

        return switch (timeChoice) {
            case 1 -> LocalTime.of(9, 0);
            case 2 -> LocalTime.of(10, 0);
            case 3 -> LocalTime.of(11, 0);
            case 4 -> LocalTime.of(14, 0);
            case 5 -> LocalTime.of(15, 0);
            case 6 -> LocalTime.of(16, 0);
            default -> LocalTime.of(10, 0);
        };
    }
}
