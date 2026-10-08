import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base type for every piece of hospital equipment.
 * Implements the Maintainable interface once, so every subclass
 * (present or future) automatically supports maintenance.
 */
public abstract class MedicalEquipment implements Maintainable {
    /** Maintenance interval (days) used after a service is completed. */
    public static final int SERVICE_INTERVAL_DAYS = 180;

    protected String equipmentId;
    protected String name;
    protected String manufacturer;
    protected String model;
    protected EquipmentStatus status;
    protected Date lastMaintenanceDate;
    protected Date nextMaintenanceDate;

    // Composition: the maintenance records live and die with the equipment.
    protected List<MaintenanceRecord> maintenanceHistory;

    public MedicalEquipment(String equipmentId, String name, String manufacturer, String model) {
        this.equipmentId = equipmentId;
        this.name = name;
        this.manufacturer = manufacturer;
        this.model = model;
        this.status = EquipmentStatus.OPERATIONAL;
        this.maintenanceHistory = new ArrayList<>();
    }

    public abstract void startOperation();

    public abstract void stopOperation();

    /** Device specific attributes (shown in the GUI). Subclasses override this. */
    public Map<String, String> getSpecificDetails() {
        return new LinkedHashMap<>();
    }

    public EquipmentStatus checkStatus() {
        return this.status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }

    /** "PatientMonitor" becomes "Patient Monitor", "ECGMachine" becomes "ECG Machine". */
    public String getTypeName() {
        return getClass().getSimpleName().replaceAll("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])", " ");
    }

    public String getEquipmentInfo() {
        return String.format("ID: %s | Name: %s | Model: %s (%s) | Status: %s",
                equipmentId, name, model, manufacturer, status);
    }

    @Override
    public void scheduleMaintenance(Date date) {
        this.nextMaintenanceDate = date;
    }

    @Override
    public void performMaintenance() {
        performMaintenance("Certified Technician", "Routine calibration & inspection completed.");
    }

    /** Completes a service with a given technician / description and logs it in the history. */
    public void performMaintenance(String technician, String description) {
        this.lastMaintenanceDate = new Date();
        this.status = EquipmentStatus.OPERATIONAL;
        String recordId = "REC-" + (maintenanceHistory.size() + 1);
        maintenanceHistory.add(new MaintenanceRecord(recordId, equipmentId,
                this.lastMaintenanceDate, technician, description, true));
        // Next periodic service
        this.nextMaintenanceDate = new Date(lastMaintenanceDate.getTime()
                + SERVICE_INTERVAL_DAYS * 86400000L);
    }

    public void addMaintenanceRecord(MaintenanceRecord record) {
        this.maintenanceHistory.add(record);
        if (this.lastMaintenanceDate == null || record.getDate().after(this.lastMaintenanceDate)) {
            this.lastMaintenanceDate = record.getDate();
        }
    }

    /** True when a next-maintenance date exists and it is already in the past. */
    public boolean isMaintenanceOverdue() {
        return nextMaintenanceDate != null && nextMaintenanceDate.before(new Date());
    }

    @Override
    public Date getNextMaintenanceDate() {
        return this.nextMaintenanceDate;
    }

    public String getEquipmentId() { return equipmentId; }

    public String getName() { return name; }

    public String getManufacturer() { return manufacturer; }

    public String getModel() { return model; }

    public Date getLastMaintenanceDate() { return lastMaintenanceDate; }

    /** Read-only view: history can only change through the equipment's own methods. */
    public List<MaintenanceRecord> getMaintenanceHistory() {
        return Collections.unmodifiableList(maintenanceHistory);
    }
}
