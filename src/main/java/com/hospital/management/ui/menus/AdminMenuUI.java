package com.hospital.management.ui.menus;

import com.hospital.management.controllers.AdminController;
import com.hospital.management.services.impl.UserServiceImpl;
import com.hospital.management.services.impl.AppointmentServiceImpl;
import com.hospital.management.services.impl.BillingServiceImpl;
import com.hospital.management.services.impl.PaymentServiceImpl;
import com.hospital.management.commands.AdminCommands.ViewReportsCommand.ReportType;
import com.hospital.management.commands.AdminCommands.ManageUsersCommand.UserManagementAction;
import com.hospital.management.ui.InputHandler;
import com.hospital.management.commands.CommandResult;
import com.hospital.management.models.User;
import com.hospital.management.models.Department;
import com.hospital.management.models.Appointment;
import com.hospital.management.common.enums.UserRole;
import com.hospital.management.common.utils.InputValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.List;
import java.util.Map;

/**
 * Admin Menu UI with login/logout functionality and proper controller integration
 */
public class AdminMenuUI {
    private final AdminController adminController;
    private final UserServiceImpl userService;
    private final InputHandler input;

    // Session management
    private User currentUser = null;
    private boolean isLoggedIn = false;

    public AdminMenuUI() {
        this.userService = new UserServiceImpl();
        this.adminController = new AdminController(
                userService,
                new AppointmentServiceImpl(),
                new BillingServiceImpl(),
                new PaymentServiceImpl()
        );
        this.input = InputHandler.getInstance();
    }

    public void show() {
        while (true) {
            if (!isLoggedIn) {
                // Show login menu
                if (!showLoginMenu()) {
                    return; // Back to main menu
                }
            } else {
                // Show admin dashboard
                if (!showAdminDashboard()) {
                    return; // Logout and back to main menu
                }
            }
        }
    }

    private boolean showLoginMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🔐 ADMIN PORTAL");
        System.out.println("=".repeat(50));
        System.out.println("⚠️  Restricted Access - Admin Login Required");
        System.out.println();
        System.out.println("1. 🔑 Admin Login");
        System.out.println("2. ❓ Forgot Password");
        System.out.println("0. ⬅️  Back to Main Menu");
        System.out.println("=".repeat(50));

        int choice = input.getInt("Select an option (0-2): ", 0, 2);

        switch (choice) {
            case 1 -> handleLogin();
            case 2 -> handleForgotPassword();
            case 0 -> { return false; }
            default -> System.out.println("❌ Invalid option.");
        }
        return true;
    }

    private boolean showAdminDashboard() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔐 ADMIN DASHBOARD - Welcome " + currentUser.getUsername());
        System.out.println("=".repeat(60));
        System.out.println("👥 USER MANAGEMENT:");
        System.out.println("  1. 👤 View All Users");
        System.out.println("  2. 🔍 Search User Details");
        System.out.println("  3. 🗑️  Delete User");
        System.out.println();
        System.out.println("📊 REPORTS & ANALYTICS:");
        System.out.println("  4. 📈 Dashboard Summary");
        System.out.println("  5. 📅 View All Appointments");  // ✅ CHANGED
        System.out.println();
        System.out.println("🏥 HOSPITAL MANAGEMENT:");
        System.out.println("  6. 🏢 Manage Departments");      // ✅ MOVED UP
        System.out.println("  7. 👨‍⚕️ Add Doctor");              // ✅ NEW OPTION
        System.out.println("  8. ⚙️  System Settings");
        System.out.println("  0. 🚪 Logout");
        System.out.println("=".repeat(60));

        int choice = input.getInt("Select an option (0-8): ", 0, 8);

        switch (choice) {
            case 1 -> handleViewAllUsers();
            case 2 -> handleSearchUserDetails();
            case 3 -> handleDeleteUser();
            case 4 -> handleDashboardSummary();
            case 5 -> handleViewAllAppointments();      // ✅ CHANGED
            case 6 -> handleManageDepartments();        // ✅ NOW IMPLEMENTED
            case 7 -> handleAddDoctor();               // ✅ NEW
            case 8 -> handleSystemSettings();          // ✅ NOW IMPLEMENTED
            case 0 -> {
                handleLogout();
                return false;
            }
            default -> System.out.println("❌ Invalid option.");
        }
        return true;
    }

    private void handleLogin() {
        System.out.println("\n🔑 ADMIN LOGIN");
        System.out.println("=" .repeat(20));

        try {
            String loginId = input.getString("👤 Admin Username: ");
            String password = input.getPasswordInput("🔑 Admin Password: ");

            if (loginId.isEmpty() || password.isEmpty()) {
                System.out.println("❌ Admin credentials are required");
                return;
            }

            System.out.println("\n🔄 Verifying admin access...");

            // Authenticate using UserService
            boolean authenticated = userService.authenticate(loginId, password);

            if (authenticated) {
                // Get user details and verify admin role
                Optional<User> userOpt = userService.findUserByUsername(loginId);
                if (userOpt.isEmpty()) {
                    // Try finding by email if username didn't work
                    userOpt = userService.findAllUsers().stream()
                            .filter(u -> u.getEmail().equals(loginId))
                            .findFirst();
                }

                if (userOpt.isPresent() && userOpt.get().getRole() == UserRole.ADMIN) {
                    currentUser = userOpt.get();
                    isLoggedIn = true;
                    System.out.println("✅ Admin authentication successful!");
                    System.out.println("🔐 Welcome Admin " + currentUser.getUsername() + "!");
                } else {
                    System.out.println("❌ Access denied: Not an admin account");
                    System.out.println("⚠️  Unauthorized access attempt logged");
                }
            } else {
                System.out.println("❌ Admin login failed: Invalid credentials");
                System.out.println("⚠️  Unauthorized access attempt logged");
            }

        } catch (Exception e) {
            System.out.println("❌ Authentication error: " + e.getMessage());
        }
    }

    private void handleLogout() {
        System.out.println("\n🔄 Logging out...");
        System.out.println("👋 Goodbye, Admin " + (currentUser != null ? currentUser.getUsername() : "User") + "!");
        currentUser = null;
        isLoggedIn = false;
        System.out.println("✅ Admin session ended successfully!");
    }

    private void handleViewAllUsers() {
        System.out.println("\n👥 VIEW ALL USERS");
        System.out.println("=" .repeat(20));

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            Long adminId = currentUser.getId();

            System.out.println("🔄 Fetching all users...");

            // Call AdminController to view all users
            CommandResult result = adminController.viewAllUsers(adminId);

            if (result.isSuccess()) {
                System.out.println("✅ " + result.getMessage());

                if (result.getData() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<User> users = (List<User>) result.getData();

                    if (users.isEmpty()) {
                        System.out.println("📋 No users found");
                    } else {
                        System.out.println("\n👥 System Users:");
                        System.out.println("─".repeat(80));
                        System.out.printf("%-4s %-15s %-25s %-15s %-10s%n",
                                "ID", "Username", "Email", "Phone", "Role");
                        System.out.println("─".repeat(80));

                        for (User user : users) {
                            System.out.printf("%-4s %-15s %-25s %-15s %-10s%n",
                                    user.getId() != null ? user.getId() : "N/A",
                                    user.getUsername() != null ? user.getUsername() : "N/A",
                                    user.getEmail() != null ? user.getEmail() : "N/A",
                                    user.getPhone() != null ? user.getPhone() : "N/A",
                                    user.getRole() != null ? user.getRole() : "N/A");
                        }
                        System.out.println("─".repeat(80));
                        System.out.println("Total Users: " + users.size());
                    }
                }
            } else {
                System.out.println("❌ " + result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ View users error: " + e.getMessage());
        }
    }

    private void handleSearchUserDetails() {
        System.out.println("\n🔍 SEARCH USER DETAILS");
        System.out.println("=" .repeat(25));

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            Long adminId = currentUser.getId();

            // ✅ ADDED VALIDATION: Check if user ID is provided
            String userIdInput = input.getString("👤 Enter User ID: ");
            if (userIdInput.trim().isEmpty()) {
                System.out.println("❌ User ID is required");
                return;
            }

            // ✅ ADDED VALIDATION: Check if user ID is a valid number
            Long targetUserId;
            try {
                targetUserId = Long.valueOf(userIdInput);
                if (targetUserId <= 0) {
                    System.out.println("❌ User ID must be a positive number");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid User ID format. Please enter a valid number.");
                return;
            }

            System.out.println("🔄 Fetching user details...");

            // Call AdminController to get user details
            CommandResult result = adminController.getUserDetails(adminId, targetUserId);

            if (result.isSuccess()) {
                System.out.println("✅ " + result.getMessage());

                if (result.getData() instanceof User) {
                    User user = (User) result.getData();
                    System.out.println("\n👤 User Details:");
                    System.out.println("─".repeat(40));
                    System.out.println("ID: " + user.getId());
                    System.out.println("Username: " + user.getUsername());
                    System.out.println("Email: " + user.getEmail());
                    System.out.println("Phone: " + user.getPhone());
                    System.out.println("Role: " + user.getRole());
                    System.out.println("Active: " + (user.isActive() ? "Yes" : "No"));
                    System.out.println("─".repeat(40));
                }
            } else {
                System.out.println("❌ " + result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ Search user error: " + e.getMessage());
        }
    }

    private void handleDeleteUser() {
        System.out.println("\n🗑️ DELETE USER");
        System.out.println("=" .repeat(15));
        System.out.println("⚠️  WARNING: This action cannot be undone!");

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            Long adminId = currentUser.getId();

            // ✅ ADDED VALIDATION: Check if user ID is provided and valid
            String userIdInput = input.getString("👤 Enter User ID to delete: ");
            if (userIdInput.trim().isEmpty()) {
                System.out.println("❌ User ID is required");
                return;
            }

            Long targetUserId;
            try {
                targetUserId = Long.valueOf(userIdInput);
                if (targetUserId <= 0) {
                    System.out.println("❌ User ID must be a positive number");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid User ID format. Please enter a valid number.");
                return;
            }

            // ✅ ADDED VALIDATION: Prevent admin from deleting their own account
            if (targetUserId.equals(adminId)) {
                System.out.println("❌ You cannot delete your own admin account!");
                return;
            }

            // Confirm deletion
            String confirmation = input.getString("Type 'DELETE' to confirm: ");
            if (!"DELETE".equals(confirmation)) {
                System.out.println("❌ Operation cancelled");
                return;
            }

            System.out.println("🔄 Deleting user...");

            // Call AdminController to delete user
            CommandResult result = adminController.deleteUser(adminId, targetUserId);

            if (result.isSuccess()) {
                System.out.println("✅ " + result.getMessage());
                System.out.println("🗑️ User deleted successfully");
            } else {
                System.out.println("❌ " + result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ Delete user error: " + e.getMessage());
        }
    }

    private void handleDashboardSummary() {
        System.out.println("\n📈 DASHBOARD SUMMARY");
        System.out.println("=" .repeat(25));

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            Long adminId = currentUser.getId();

            System.out.println("🔄 Generating dashboard summary...");

            // Call AdminController to generate dashboard summary
            CommandResult result = adminController.generateDashboardSummary(adminId);

            if (result.isSuccess()) {
                System.out.println("✅ " + result.getMessage());

                if (result.getData() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> summary = (Map<String, Object>) result.getData();

                    System.out.println("\n📊 Hospital Management System Summary:");
                    System.out.println("═".repeat(50));
                    System.out.println("👥 Total Users: " + summary.get("totalUsers"));
                    System.out.println("📅 Total Appointments: " + summary.get("totalAppointments"));
                    System.out.println("💰 Total Bills: " + summary.get("totalBills"));
                    System.out.println("💳 Total Payments: " + summary.get("totalPayments"));
                    System.out.println("🕒 Generated At: " + summary.get("generatedAt"));
                    System.out.println("═".repeat(50));
                }
            } else {
                System.out.println("❌ " + result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ Dashboard summary error: " + e.getMessage());
        }
    }

    // ✅ NEW: View All Appointments (instead of appointment reports)
    private void handleViewAllAppointments() {
        System.out.println("\n📅 VIEW ALL APPOINTMENTS");
        System.out.println("=" .repeat(30));

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            Long adminId = currentUser.getId();

            System.out.println("🔄 Fetching all appointments...");

            // Call AdminController to view all appointments
            CommandResult result = adminController.viewAllAppointments(adminId);

            if (result.isSuccess()) {
                System.out.println("✅ " + result.getMessage());

                if (result.getData() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Appointment> appointments = (List<Appointment>) result.getData();

                    if (appointments.isEmpty()) {
                        System.out.println("📋 No appointments found");
                    } else {
                        System.out.println("\n📅 System Appointments:");
                        System.out.println("─".repeat(100));
                        System.out.printf("%-4s %-12s %-12s %-15s %-15s %-15s %-25s%n",
                                "ID", "Patient ID", "Doctor ID", "Date", "Time", "Status", "Reason");
                        System.out.println("─".repeat(100));

                        for (Appointment appointment : appointments) {
                            String reason = appointment.getReason();
                            if (reason != null && reason.length() > 22) {
                                reason = reason.substring(0, 19) + "...";
                            }

                            System.out.printf("%-4s %-12s %-12s %-15s %-15s %-15s %-25s%n",
                                    appointment.getId() != null ? appointment.getId() : "N/A",
                                    appointment.getPatientId() != null ? appointment.getPatientId() : "N/A",
                                    appointment.getDoctorId() != null ? appointment.getDoctorId() : "N/A",
                                    appointment.getAppointmentDate() != null ? appointment.getAppointmentDate() : "N/A",
                                    appointment.getAppointmentTime() != null ? appointment.getAppointmentTime() : "N/A",
                                    appointment.getStatus() != null ? appointment.getStatus() : "N/A",
                                    reason != null ? reason : "N/A");
                        }
                        System.out.println("─".repeat(100));
                        System.out.println("Total Appointments: " + appointments.size());
                    }
                }
            } else {
                System.out.println("❌ " + result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ View appointments error: " + e.getMessage());
        }
    }

    // ✅ IMPLEMENTED: Manage Departments
    private void handleManageDepartments() {
        while (true) {
            System.out.println("\n🏢 MANAGE DEPARTMENTS");
            System.out.println("=" .repeat(25));
            System.out.println("1. 👁️  View Existing Departments");
            System.out.println("2. ➕ Add New Department");
            System.out.println("3. 🗑️  Delete Existing Department");
            System.out.println("0. ⬅️  Back to Dashboard");
            System.out.println("=" .repeat(35));

            int choice = input.getInt("Select an option (0-3): ", 0, 3);

            switch (choice) {
                case 1 -> handleViewDepartments();
                case 2 -> handleAddDepartment();
                case 3 -> handleDeleteDepartment();
                case 0 -> { return; }
                default -> System.out.println("❌ Invalid option.");
            }
        }
    }

    private void handleViewDepartments() {
        System.out.println("\n👁️ VIEW DEPARTMENTS");
        System.out.println("=" .repeat(20));

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            Long adminId = currentUser.getId();

            System.out.println("🔄 Fetching departments...");

            CommandResult result = adminController.viewAllDepartments(adminId);

            if (result.isSuccess()) {
                System.out.println("✅ " + result.getMessage());

                if (result.getData() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Department> departments = (List<Department>) result.getData();

                    if (departments.isEmpty()) {
                        System.out.println("📋 No departments found");
                    } else {
                        System.out.println("\n🏢 Hospital Departments:");
                        System.out.println("─".repeat(90));
                        System.out.printf("%-4s %-20s %-30s %-20s %-10s%n",
                                "ID", "Name", "Description", "Location", "Status");
                        System.out.println("─".repeat(90));

                        for (Department dept : departments) {
                            String desc = dept.getDescription();
                            if (desc != null && desc.length() > 27) {
                                desc = desc.substring(0, 24) + "...";
                            }

                            System.out.printf("%-4s %-20s %-30s %-20s %-10s%n",
                                    dept.getId() != null ? dept.getId() : "N/A",
                                    dept.getName() != null ? dept.getName() : "N/A",
                                    desc != null ? desc : "N/A",
                                    dept.getLocation() != null ? dept.getLocation() : "N/A",
                                    dept.isActive() ? "Active" : "Inactive");
                        }
                        System.out.println("─".repeat(90));
                        System.out.println("Total Departments: " + departments.size());
                    }
                }
            } else {
                System.out.println("❌ " + result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ View departments error: " + e.getMessage());
        }
    }

    private void handleAddDepartment() {
        System.out.println("\n➕ ADD NEW DEPARTMENT");
        System.out.println("=" .repeat(22));

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            Long adminId = currentUser.getId();

            // ✅ ADDED VALIDATIONS: Collect department information with validation
            String name = getValidatedInput("🏢 Department Name: ", InputValidator::isValidName);
            String description = input.getString("📝 Description: ");
            String location = input.getString("📍 Location: ");
            String phone = input.getString("📞 Phone (optional): ");

            // ✅ ADDED VALIDATION: Phone validation if provided
            if (!phone.trim().isEmpty() && !InputValidator.isValidPhone(phone)) {
                System.out.println("❌ Invalid phone number format");
                return;
            }

            System.out.println("🔄 Creating department...");

            CommandResult result = adminController.addDepartment(adminId, name.trim(),
                    description.trim(), location.trim(), phone.trim().isEmpty() ? null : phone.trim());

            if (result.isSuccess()) {
                System.out.println("✅ " + result.getMessage());
                System.out.println("🏢 Department '" + name + "' created successfully!");
            } else {
                System.out.println("❌ " + result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ Add department error: " + e.getMessage());
        }
    }

    private void handleDeleteDepartment() {
        System.out.println("\n🗑️ DELETE DEPARTMENT");
        System.out.println("=" .repeat(20));
        System.out.println("⚠️  WARNING: This action cannot be undone!");

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            Long adminId = currentUser.getId();

            // ✅ ADDED VALIDATION: Check if department ID is provided and valid
            String deptIdInput = input.getString("🏢 Enter Department ID to delete: ");
            if (deptIdInput.trim().isEmpty()) {
                System.out.println("❌ Department ID is required");
                return;
            }

            Long departmentId;
            try {
                departmentId = Long.valueOf(deptIdInput);
                if (departmentId <= 0) {
                    System.out.println("❌ Department ID must be a positive number");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid Department ID format. Please enter a valid number.");
                return;
            }

            // Confirm deletion
            String confirmation = input.getString("Type 'DELETE' to confirm: ");
            if (!"DELETE".equals(confirmation)) {
                System.out.println("❌ Operation cancelled");
                return;
            }

            System.out.println("🔄 Deleting department...");

            CommandResult result = adminController.deleteDepartment(adminId, departmentId);

            if (result.isSuccess()) {
                System.out.println("✅ " + result.getMessage());
                System.out.println("🗑️ Department deleted successfully");
            } else {
                System.out.println("❌ " + result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ Delete department error: " + e.getMessage());
        }
    }

    // ✅ NEW: Add Doctor functionality
    private void handleAddDoctor() {
        System.out.println("\n👨‍⚕️ ADD NEW DOCTOR");
        System.out.println("=" .repeat(20));

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            Long adminId = currentUser.getId();

            // ✅ ADDED VALIDATIONS: Collect doctor information with validation
            System.out.println("👤 Doctor Account Information:");
            String username = getValidatedInput("Username: ", InputValidator::isValidUsername);

            // ✅ ADDED CONFIRM PASSWORD VALIDATION
            String password = getValidatedPassword("Password: ");
            String confirmPassword = input.getPasswordInput("🔑 Confirm Password: ");

            if (!password.equals(confirmPassword)) {
                System.out.println("❌ Passwords do not match");
                return;
            }

            String email = getValidatedInput("Email: ", InputValidator::isValidEmail);
            String phone = getValidatedInput("Phone: ", InputValidator::isValidPhone);

            System.out.println("\n👨‍⚕️ Doctor Personal Information:");
            String firstName = getValidatedInput("First Name: ", InputValidator::isValidName);
            String lastName = getValidatedInput("Last Name: ", InputValidator::isValidName);
            String specialization = getValidatedInput("Specialization: ", InputValidator::isValidName);
            String licenseNumber = input.getString("License Number: ");

            System.out.println("\n🏥 Hospital Information:");

            // ✅ ADDED VALIDATION: Department ID validation
            String deptIdInput = input.getString("Department ID: ");
            if (deptIdInput.trim().isEmpty()) {
                System.out.println("❌ Department ID is required");
                return;
            }

            Long departmentId;
            try {
                departmentId = Long.valueOf(deptIdInput);
                if (departmentId <= 0) {
                    System.out.println("❌ Department ID must be a positive number");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid Department ID format. Please enter a valid number.");
                return;
            }

            String qualification = input.getString("Qualification: ");
            int experienceYears = input.getInt("Experience (years): ", 0, 50);

            // ✅ ADDED VALIDATION: Consultation fee validation
            String feeInput = input.getString("Consultation Fee (₹): ");
            BigDecimal consultationFee;
            try {
                consultationFee = new BigDecimal(feeInput);
                if (consultationFee.compareTo(BigDecimal.ZERO) < 0) {
                    System.out.println("❌ Consultation fee cannot be negative");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid fee format. Please enter a valid number.");
                return;
            }

            System.out.println("🔄 Adding doctor to system...");

            CommandResult result = adminController.addDoctor(adminId, username, password, email, phone,
                    firstName, lastName, specialization, licenseNumber, departmentId,
                    qualification, experienceYears, consultationFee);

            if (result.isSuccess()) {
                System.out.println("✅ " + result.getMessage());
                System.out.println("👨‍⚕️ Dr. " + firstName + " " + lastName + " added successfully!");
            } else {
                System.out.println("❌ " + result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ Add doctor error: " + e.getMessage());
        }
    }

    // ✅ IMPLEMENTED: System Settings
    private void handleSystemSettings() {
        while (true) {
            System.out.println("\n⚙️ SYSTEM SETTINGS");
            System.out.println("=" .repeat(18));
            System.out.println("1. 👁️  View Admin Details");
            System.out.println("2. 🔑 Change Admin Password");
            System.out.println("0. ⬅️  Back to Dashboard");
            System.out.println("=" .repeat(28));

            int choice = input.getInt("Select an option (0-2): ", 0, 2);

            switch (choice) {
                case 1 -> handleViewAdminDetails();
                case 2 -> handleChangeAdminPassword();
                case 0 -> { return; }
                default -> System.out.println("❌ Invalid option.");
            }
        }
    }

    private void handleViewAdminDetails() {
        System.out.println("\n👁️ ADMIN DETAILS");
        System.out.println("=" .repeat(17));

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            Long adminId = currentUser.getId();

            System.out.println("🔄 Retrieving admin profile...");

            CommandResult result = adminController.viewAdminProfile(adminId);

            if (result.isSuccess() && result.getData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> profileData = (Map<String, Object>) result.getData();

                System.out.println("\n👤 Admin Profile:");
                System.out.println("─".repeat(40));
                System.out.println("Admin ID: " + profileData.get("id"));
                System.out.println("Username: " + profileData.get("username"));
                System.out.println("Email: " + profileData.get("email"));
                System.out.println("Phone: " + profileData.get("phone"));
                System.out.println("Role: " + profileData.get("role"));
                System.out.println("Account Status: " + ((Boolean) profileData.get("isActive") ? "Active" : "Inactive"));
                System.out.println("Created: " + profileData.get("createdAt"));
                System.out.println("Last Updated: " + profileData.get("updatedAt"));
                System.out.println("─".repeat(40));
            } else {
                System.out.println("❌ " + result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ View admin details error: " + e.getMessage());
        }
    }

    private void handleChangeAdminPassword() {
        System.out.println("\n🔑 CHANGE ADMIN PASSWORD");
        System.out.println("=" .repeat(25));

        try {
            if (!isLoggedIn || currentUser == null) {
                System.out.println("❌ Please login first");
                return;
            }

            Long adminId = currentUser.getId();

            // ✅ ADDED VALIDATIONS: Password change with validation
            String currentPassword = input.getPasswordInput("🔑 Current Password: ");
            String newPassword = getValidatedPassword("🔑 New Password: ");

            // ✅ ADDED CONFIRM PASSWORD VALIDATION
            String confirmPassword = input.getPasswordInput("🔑 Confirm New Password: ");

            if (!newPassword.equals(confirmPassword)) {
                System.out.println("❌ New passwords do not match");
                return;
            }

            // ✅ ADDED VALIDATION: Prevent using same password
            if (currentPassword.equals(newPassword)) {
                System.out.println("❌ New password cannot be the same as current password");
                return;
            }

            System.out.println("🔄 Changing password...");

            CommandResult result = adminController.changeAdminPassword(adminId, currentPassword, newPassword);

            if (result.isSuccess()) {
                System.out.println("✅ " + result.getMessage());
                System.out.println("🔑 Admin password changed successfully!");
                System.out.println("💡 Please use your new password for future logins");
            } else {
                System.out.println("❌ " + result.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ Change password error: " + e.getMessage());
        }
    }

    // Placeholder method for forgot password
    private void handleForgotPassword() {
        System.out.println("\n🔑 ADMIN PASSWORD RECOVERY");
        System.out.println("🔐 For security reasons, admin password recovery requires manual verification");
        System.out.println("📧 Please contact the system administrator for password reset");
        System.out.println("🚧 Automated admin password recovery - Coming soon!");
    }

    // ✅ ADDED HELPER METHODS FOR VALIDATION (similar to PatientMenuUI)

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
}