/**
 * Creates equipment objects from a type name.
 * The GUI uses this, so adding a new device type only needs
 * one new entry here plus the new subclass.
 */
public final class EquipmentFactory {
    public static final String[] TYPES = {
        "Patient Monitor", "Ventilator", "Infusion Pump", "ECG Machine", "Portable Ultrasound"
    };

    private EquipmentFactory() { }

    /** Short code used inside generated IDs. */
    public static String codeFor(String type) {
        switch (type) {
            case "Ventilator":          return "VN";
            case "Infusion Pump":       return "IP";
            case "ECG Machine":         return "ECG";
            case "Portable Ultrasound": return "US";
            default:                    return "PM";
        }
    }

    /** Label of the one device-specific field the user must fill in (or null if none). */
    public static String extraFieldLabel(String type) {
        switch (type) {
            case "ECG Machine":         return "Lead Count";
            case "Portable Ultrasound": return "Probe Type";
            default:                    return null;
        }
    }

    public static String extraFieldDefault(String type) {
        switch (type) {
            case "ECG Machine":         return "12";
            case "Portable Ultrasound": return "Convex";
            default:                    return "";
        }
    }

    public static MedicalEquipment create(String type, String id, String name,
                                          String manufacturer, String model, String extra) {
        switch (type) {
            case "Ventilator":
                return new Ventilator(id, name, manufacturer, model);
            case "Infusion Pump":
                return new InfusionPump(id, name, manufacturer, model);
            case "ECG Machine":
                int leads;
                try {
                    leads = Integer.parseInt(extra.trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Lead count must be a whole number (e.g. 12).");
                }
                if (leads < 1 || leads > 24) {
                    throw new IllegalArgumentException("Lead count must be between 1 and 24.");
                }
                return new ECGMachine(id, name, manufacturer, model, leads);
            case "Portable Ultrasound":
                String probe = (extra == null || extra.trim().isEmpty()) ? "Convex" : extra.trim();
                return new PortableUltrasound(id, name, manufacturer, model, probe);
            default:
                return new PatientMonitor(id, name, manufacturer, model);
        }
    }
}
