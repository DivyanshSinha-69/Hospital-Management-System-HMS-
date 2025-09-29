package com.hospital.management.core.factories;

import com.hospital.management.patient.dao.PatientDAO;
import com.hospital.management.patient.dao.impl.PatientDAOImpl;
import com.hospital.management.doctor.dao.DoctorDAO;
import com.hospital.management.doctor.dao.impl.DoctorDAOImpl;
import com.hospital.management.appointment.dao.AppointmentDAO;
import com.hospital.management.appointment.dao.impl.AppointmentDAOImpl;
import com.hospital.management.department.dao.DepartmentDAO;
import com.hospital.management.department.dao.impl.DepartmentDAOImpl;
import com.hospital.management.staff.dao.StaffDAO;
import com.hospital.management.staff.dao.impl.StaffDAOImpl;
import com.hospital.management.medicalrecord.dao.MedicalRecordDAO;
import com.hospital.management.medicalrecord.dao.impl.MedicalRecordDAOImpl;
import com.hospital.management.billing.dao.BillingDAO;
import com.hospital.management.billing.dao.impl.BillingDAOImpl;
import com.hospital.management.inventory.dao.InventoryDAO;
import com.hospital.management.inventory.dao.impl.InventoryDAOImpl;
import com.hospital.management.pharmacy.dao.PharmacyDAO;
import com.hospital.management.pharmacy.dao.impl.PharmacyDAOImpl;
import com.hospital.management.laboratory.dao.LaboratoryDAO;
import com.hospital.management.laboratory.dao.impl.LaboratoryDAOImpl;
import com.hospital.management.security.dao.UserDAO;
import com.hospital.management.security.dao.impl.UserDAOImpl;
import com.hospital.management.reporting.dao.ReportingDAO;
import com.hospital.management.reporting.dao.impl.ReportingDAOImpl;
import com.hospital.management.notification.dao.NotificationDAO;
import com.hospital.management.notification.dao.impl.NotificationDAOImpl;

public class DAOFactory {

    public static PatientDAO getPatientDAO() {
        return new PatientDAOImpl();
    }

    public static DoctorDAO getDoctorDAO() {
        return new DoctorDAOImpl();
    }

    public static AppointmentDAO getAppointmentDAO() {
        return new AppointmentDAOImpl();
    }

    public static DepartmentDAO getDepartmentDAO() {
        return new DepartmentDAOImpl();
    }

    public static StaffDAO getStaffDAO() {
        return new StaffDAOImpl();
    }

    public static MedicalRecordDAO getMedicalRecordDAO() {
        return new MedicalRecordDAOImpl();
    }

    public static BillingDAO getBillingDAO() {
        return new BillingDAOImpl();
    }

    public static InventoryDAO getInventoryDAO() {
        return new InventoryDAOImpl();
    }

    public static PharmacyDAO getPharmacyDAO() {
        return new PharmacyDAOImpl();
    }

    public static LaboratoryDAO getLaboratoryDAO() {
        return new LaboratoryDAOImpl();
    }

    public static UserDAO getUserDAO() {
        return new UserDAOImpl();
    }

    public static ReportingDAO getReportingDAO() {
        return new ReportingDAOImpl();
    }

    public static NotificationDAO getNotificationDAO() {
        return new NotificationDAOImpl();
    }
}
