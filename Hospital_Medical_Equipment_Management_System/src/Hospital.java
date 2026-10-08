import java.util.ArrayList;
import java.util.List;

/**
 * Owns the collection of equipment (Aggregation) and answers
 * hospital-wide queries: search, filter, overdue maintenance.
 */
public class Hospital {
    private String name;
    private List<MedicalEquipment> equipmentList;

    public Hospital(String name) {
        this.name = name;
        this.equipmentList = new ArrayList<>();
    }

    /** Adds equipment. Returns false (and does nothing) if the ID already exists. */
    public boolean addEquipment(MedicalEquipment e) {
        if (e == null || findById(e.getEquipmentId()) != null) {
            return false;
        }
        equipmentList.add(e);
        return true;
    }

    public boolean removeEquipment(String id) {
        return equipmentList.removeIf(e -> e.getEquipmentId().equalsIgnoreCase(id));
    }

    public MedicalEquipment findById(String id) {
        for (MedicalEquipment e : equipmentList) {
            if (e.getEquipmentId().equalsIgnoreCase(id)) {
                return e;
            }
        }
        return null;
    }

    public List<MedicalEquipment> getOperationalEquipment() {
        return filterByStatus(EquipmentStatus.OPERATIONAL);
    }

    public List<MedicalEquipment> filterByStatus(EquipmentStatus status) {
        List<MedicalEquipment> result = new ArrayList<>();
        for (MedicalEquipment e : equipmentList) {
            if (e.checkStatus() == status) {
                result.add(e);
            }
        }
        return result;
    }

    public List<MedicalEquipment> filterByType(String typeName) {
        List<MedicalEquipment> result = new ArrayList<>();
        for (MedicalEquipment e : equipmentList) {
            if (e.getTypeName().equalsIgnoreCase(typeName)) {
                result.add(e);
            }
        }
        return result;
    }

    /** Case-insensitive search over ID, name, manufacturer, model and type. */
    public List<MedicalEquipment> search(String keyword) {
        List<MedicalEquipment> result = new ArrayList<>();
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        for (MedicalEquipment e : equipmentList) {
            String all = (e.getEquipmentId() + " " + e.getName() + " " + e.getManufacturer()
                    + " " + e.getModel() + " " + e.getTypeName()).toLowerCase();
            if (all.contains(k)) {
                result.add(e);
            }
        }
        return result;
    }

    /** FR5: equipment whose next-maintenance date is already in the past. */
    public List<MedicalEquipment> getOverdueEquipment() {
        List<MedicalEquipment> result = new ArrayList<>();
        for (MedicalEquipment e : equipmentList) {
            if (e.isMaintenanceOverdue()) {
                result.add(e);
            }
        }
        return result;
    }

    /** Generates a unique ID such as EQ-PM-06. */
    public String generateId(String code) {
        int n = equipmentList.size() + 1;
        String id;
        do {
            id = String.format("EQ-%s-%02d", code, n++);
        } while (findById(id) != null);
        return id;
    }

    public void displayAllEquipment() {
        System.out.println("\n===== " + name + " Equipment Inventory =====");
        for (MedicalEquipment e : equipmentList) {
            System.out.println(e.getEquipmentInfo());
        }
    }

    public List<MedicalEquipment> getEquipmentList() {
        return equipmentList;
    }

    public String getName() {
        return name;
    }
}
