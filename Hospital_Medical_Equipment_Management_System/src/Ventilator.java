import java.util.LinkedHashMap;
import java.util.Map;

public class Ventilator extends MedicalEquipment {
    private double oxygenLevel;
    private int breathingRate;

    public Ventilator(String equipmentId, String name, String manufacturer, String model) {
        super(equipmentId, name, manufacturer, model);
        this.oxygenLevel = 98.0;
        this.breathingRate = 16;
    }

    @Override
    public void startOperation() {
        this.status = EquipmentStatus.OPERATIONAL;
        System.out.println("[Ventilator " + equipmentId + "] Mechanical ventilation delivery active.");
    }

    @Override
    public void stopOperation() {
        this.status = EquipmentStatus.OUT_OF_SERVICE;
        System.out.println("[Ventilator " + equipmentId + "] Ventilation safely paused.");
    }

    public void adjustOxygenLevel(double level) {
        if (level < 21.0 || level > 100.0) {
            throw new IllegalArgumentException("Oxygen level must be between 21% and 100%.");
        }
        this.oxygenLevel = level;
        System.out.println("[Ventilator " + equipmentId + "] Oxygen concentration adjusted to " + level + "%.");
    }

    public double getOxygenLevel() { return oxygenLevel; }

    public int getBreathingRate() { return breathingRate; }

    @Override
    public Map<String, String> getSpecificDetails() {
        Map<String, String> d = new LinkedHashMap<>();
        d.put("Oxygen Level", oxygenLevel + " %");
        d.put("Breathing Rate", breathingRate + " breaths/min");
        return d;
    }
}
