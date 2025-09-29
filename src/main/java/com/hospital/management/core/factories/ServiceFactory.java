package com.hospital.management.core.factories;

import com.hospital.management.patient.service.PatientService;
import com.hospital.management.patient.service.impl.PatientServiceImpl;
import com.hospital.management.doctor.service.DoctorService;
import com.hospital.management.doctor.service.impl.DoctorServiceImpl;
import com.hospital.management.appointment.service.AppointmentService;
import com.hospital.management.appointment.service.impl.AppointmentServiceImpl;
import com.hospital.management.department.service.DepartmentService;
import com.hospital.management.department.service.impl.DepartmentServiceImpl;
import com.hospital.management.staff.service.StaffService;
import com.hospital.management.staff.service.impl.StaffServiceImpl;
import com.hospital.management.medicalrecord.service.MedicalRecordService;
import com.hospital.management.medicalrecord.service.impl.MedicalRecordServiceImpl;
import com.hospital.management.billing.service.BillingService;
import com.hospital.management.billing.service.impl.BillingServiceImpl;
import com.hospital.management.inventory.service.InventoryService;
import com.hospital.management.inventory.service.impl.InventoryServiceImpl;
import com.hospital.management.pharmacy.service.PharmacyService;
import com.hospital.management.pharmacy.service.impl.PharmacyServiceImpl;
import com.hospital.management.laboratory.service.LaboratoryService;
import com.hospital.management.laboratory.service.impl.LaboratoryServiceImpl;
import com.hospital.management.security.service.UserService;
import com.hospital.management.security.service.impl.UserServiceImpl;
import com.hospital.management.reporting.service.ReportingService;
import com.hospital.management.reporting.service.impl.ReportingServiceImpl;
import com.hospital.management.notification.service.NotificationService;
import com.hospital.management.notification.service.impl.NotificationServiceImpl;

public class ServiceFactory {

    public static PatientService getPatientService() {
        return new PatientServiceImpl();
    }

    public static DoctorService getDoctorService() {
        return new DoctorServiceImpl();
    }

    public static AppointmentService getAppointmentService() {
        return new AppointmentServiceImpl();
    }

    public static DepartmentService getDepartmentService() {
        return new DepartmentServiceImpl();
    }

    public static StaffService getStaffService() {
        return new StaffServiceImpl();
    }

    public static MedicalRecordService getMedicalRecordService() {
        return new MedicalRecordServiceImpl();
    }

    public static BillingService getBillingService() {
        return new BillingServiceImpl();
    }

    public static InventoryService getInventoryService() {
        return new InventoryServiceImpl();
    }

    public static PharmacyService getPharmacyService() {
        return new PharmacyServiceImpl();
    }

    public static LaboratoryService getLaboratoryService() {
        return new LaboratoryServiceImpl();
    }

    public static UserService getUserService() {
        return new UserServiceImpl();
    }

    public static ReportingService getReportingService() {
        return new ReportingServiceImpl();
    }

    public static NotificationService getNotificationService() {
        return new NotificationServiceImpl();
    }
}
