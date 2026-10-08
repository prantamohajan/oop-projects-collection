import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Coordinates maintenance scheduling across equipment (Association with MedicalEquipment).
 */
public class MaintenanceScheduler {
    private List<MaintenanceRecord> scheduledTasks;

    public MaintenanceScheduler() {
        this.scheduledTasks = new ArrayList<>();
    }

    public void scheduleTask(MedicalEquipment equipment, String technician, String description, Date date) {
        equipment.scheduleMaintenance(date);
        String recordId = "SCHED-" + (scheduledTasks.size() + 101);
        MaintenanceRecord record = new MaintenanceRecord(recordId, equipment.getEquipmentId(), date,
                technician, description + " [" + equipment.getName() + "]", false);
        scheduledTasks.add(record);
    }

    /** Marks every pending task of this equipment as completed. */
    public void completeTasksFor(MedicalEquipment equipment) {
        for (MaintenanceRecord r : scheduledTasks) {
            if (r.getEquipmentId().equals(equipment.getEquipmentId()) && !r.isCompleted()) {
                r.markCompleted();
            }
        }
    }

    /** Removes all tasks of an equipment (used when the equipment is deleted). */
    public void removeTasksFor(String equipmentId) {
        scheduledTasks.removeIf(r -> r.getEquipmentId().equalsIgnoreCase(equipmentId));
    }

    /** Tasks that are still waiting to be performed. */
    public List<MaintenanceRecord> getUpcomingTasks() {
        List<MaintenanceRecord> pending = new ArrayList<>();
        for (MaintenanceRecord r : scheduledTasks) {
            if (!r.isCompleted()) {
                pending.add(r);
            }
        }
        return pending;
    }

    public List<MaintenanceRecord> getAllTasks() {
        return scheduledTasks;
    }
}
