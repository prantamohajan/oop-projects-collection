import java.util.LinkedHashMap;
import java.util.Map;

public class ECGMachine extends MedicalEquipment {
    private int leadCount;

    public ECGMachine(String equipmentId, String name, String manufacturer, String model, int leadCount) {
        super(equipmentId, name, manufacturer, model);
        this.leadCount = leadCount;
    }

    @Override
    public void startOperation() {
        this.status = EquipmentStatus.OPERATIONAL;
        System.out.println("[ECG " + equipmentId + "] Calibrating " + leadCount + "-lead telemetry sensors.");
    }

    @Override
    public void stopOperation() {
        this.status = EquipmentStatus.OUT_OF_SERVICE;
        System.out.println("[ECG " + equipmentId + "] Telemetry processing offline.");
    }

    public void recordECG() {
        System.out.println("[ECG " + equipmentId + "] Capturing 12-second electro-cardiac waveform...");
    }

    public int getLeadCount() { return leadCount; }

    @Override
    public Map<String, String> getSpecificDetails() {
        Map<String, String> d = new LinkedHashMap<>();
        d.put("Lead Count", leadCount + "-lead");
        return d;
    }
}
