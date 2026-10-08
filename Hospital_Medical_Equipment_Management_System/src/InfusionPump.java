import java.util.LinkedHashMap;
import java.util.Map;

public class InfusionPump extends MedicalEquipment {
    private double flowRate;
    private String medicationName;

    public InfusionPump(String equipmentId, String name, String manufacturer, String model) {
        super(equipmentId, name, manufacturer, model);
        this.flowRate = 20.0;
        this.medicationName = "Saline Solution";
    }

    @Override
    public void startOperation() {
        this.status = EquipmentStatus.OPERATIONAL;
        System.out.println("[InfusionPump " + equipmentId + "] Administering " + medicationName + " at rate: " + flowRate + " mL/h.");
    }

    @Override
    public void stopOperation() {
        this.status = EquipmentStatus.OUT_OF_SERVICE;
        System.out.println("[InfusionPump " + equipmentId + "] Medication infusion halted.");
    }

    public void setFlowRate(double rate) {
        if (rate <= 0 || rate > 1000) {
            throw new IllegalArgumentException("Flow rate must be greater than 0 and at most 1000 mL/h.");
        }
        this.flowRate = rate;
    }

    public void setMedicationName(String medicationName) {
        if (medicationName == null || medicationName.trim().isEmpty()) {
            throw new IllegalArgumentException("Medication name cannot be empty.");
        }
        this.medicationName = medicationName.trim();
    }

    public double getFlowRate() { return flowRate; }

    public String getMedicationName() { return medicationName; }

    @Override
    public Map<String, String> getSpecificDetails() {
        Map<String, String> d = new LinkedHashMap<>();
        d.put("Medication", medicationName);
        d.put("Flow Rate", flowRate + " mL/h");
        return d;
    }
}
