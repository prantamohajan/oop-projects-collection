import java.util.Date;

/**
 * Contract that guarantees every equipment type can be maintained
 * in a consistent way (Interface / Realization).
 */
public interface Maintainable {
    void scheduleMaintenance(Date date);

    void performMaintenance();

    Date getNextMaintenanceDate();
}
