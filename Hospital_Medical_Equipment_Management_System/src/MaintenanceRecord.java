import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A single completed or scheduled maintenance event tied to one equipment item.
 */
public class MaintenanceRecord {
    private final String recordId;
    private final String equipmentId;
    private final Date date;
    private final String technician;
    private final String description;
    private boolean completed;

    /** Original constructor (kept for compatibility) - treated as a completed record. */
    public MaintenanceRecord(String recordId, Date date, String technician, String description) {
        this(recordId, "-", date, technician, description, true);
    }

    public MaintenanceRecord(String recordId, String equipmentId, Date date,
                             String technician, String description, boolean completed) {
        this.recordId = recordId;
        this.equipmentId = equipmentId;
        this.date = date;
        this.technician = technician;
        this.description = description;
        this.completed = completed;
    }

    public String getRecordId() { return recordId; }

    public String getEquipmentId() { return equipmentId; }

    public Date getDate() { return date; }

    public String getTechnician() { return technician; }

    public String getDescription() { return description; }

    public boolean isCompleted() { return completed; }

    public void markCompleted() { this.completed = true; }

    public String getSummary() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return String.format("[%s] Record ID: %s | Tech: %s | Details: %s",
                sdf.format(date), recordId, technician, description);
    }
}
