/**
 * Fixed set of valid operational states used by every equipment item.
 */
public enum EquipmentStatus {
    OPERATIONAL("Operational"),
    UNDER_MAINTENANCE("Under Maintenance"),
    OUT_OF_SERVICE("Out of Service");

    private final String label;

    EquipmentStatus(String label) {
        this.label = label;
    }

    /** Human friendly text used in the GUI. */
    public String getLabel() {
        return label;
    }
}
