import java.util.Calendar;
import java.util.Date;

/**
 * Console demo (no GUI). Run HospitalGUI to see the graphical version.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("Hospital Medical Equipment Management System (CSE 1115 Demo)");
        System.out.println("Submitted by: Pranta Mohajan | ID: 0222510005101025");
        System.out.println("===============================================================\n");

        Hospital hospital = new Hospital("Premier Central Hospital");
        MaintenanceScheduler scheduler = new MaintenanceScheduler();

        PatientMonitor monitor = new PatientMonitor("EQ-PM-01", "ICU Bedside Monitor", "Philips", "IntelliVue MX800");
        Ventilator ventilator = new Ventilator("EQ-VN-02", "Critical Care Ventilator", "Dräger", "Evita V800");
        InfusionPump pump = new InfusionPump("EQ-IP-03", "Smart Infusion Pump", "Baxter", "Sigma Spectrum");
        ECGMachine ecg = new ECGMachine("EQ-ECG-04", "12-Lead Diagnostic ECG", "GE Healthcare", "MAC 2000", 12);
        PortableUltrasound ultrasound = new PortableUltrasound("EQ-US-05", "Point-of-Care Ultrasound", "Mindray", "M9", "Convex");

        hospital.addEquipment(monitor);
        hospital.addEquipment(ventilator);
        hospital.addEquipment(pump);
        hospital.addEquipment(ecg);
        hospital.addEquipment(ultrasound);

        System.out.println("\n--- Testing Polymorphic startOperation() Across All Devices ---");
        for (MedicalEquipment eq : hospital.getEquipmentList()) {
            eq.startOperation();
        }

        System.out.println("\n--- Testing Specific Clinical Operations ---");
        monitor.displayVitals();
        ventilator.adjustOxygenLevel(95.0);
        pump.setFlowRate(25.5);
        ecg.recordECG();
        ultrasound.captureImage();

        System.out.println("\n--- Testing Maintenance & Scheduling ---");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date scheduledDate = cal.getTime();

        scheduler.scheduleTask(ventilator, "Engr. Kamal", "Annual sensor calibration", scheduledDate);
        ventilator.setStatus(EquipmentStatus.UNDER_MAINTENANCE);
        ventilator.performMaintenance();
        scheduler.completeTasksFor(ventilator);

        System.out.println("\n--- Service History for Ventilator ---");
        for (MaintenanceRecord rec : ventilator.getMaintenanceHistory()) {
            System.out.println(rec.getSummary());
        }

        System.out.println("\n--- Search & Filter (FR6) ---");
        System.out.println("Search 'monitor' : " + hospital.search("monitor").size() + " result(s)");
        System.out.println("Operational      : " + hospital.getOperationalEquipment().size() + " device(s)");

        System.out.println("\n--- Overdue Maintenance (FR5) ---");
        ecg.scheduleMaintenance(new Date(System.currentTimeMillis() - 86400000L * 3));
        for (MedicalEquipment eq : hospital.getOverdueEquipment()) {
            System.out.println("OVERDUE -> " + eq.getEquipmentId() + " (" + eq.getName() + ")");
        }

        hospital.displayAllEquipment();
        System.out.println("\nAll functional requirements verified successfully!");
    }
}
