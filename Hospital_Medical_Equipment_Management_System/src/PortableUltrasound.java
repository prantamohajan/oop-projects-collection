import java.util.LinkedHashMap;
import java.util.Map;

public class PortableUltrasound extends MedicalEquipment {
    private String probeType;

    public PortableUltrasound(String equipmentId, String name, String manufacturer, String model, String probeType) {
        super(equipmentId, name, manufacturer, model);
        this.probeType = probeType;
    }

    @Override
    public void startOperation() {
        this.status = EquipmentStatus.OPERATIONAL;
        System.out.println("[Ultrasound " + equipmentId + "] Initialized imaging transceiver with " + probeType + " probe.");
    }

    @Override
    public void stopOperation() {
        this.status = EquipmentStatus.OUT_OF_SERVICE;
        System.out.println("[Ultrasound " + equipmentId + "] Ultrasound imaging terminated.");
    }

    public void captureImage() {
        System.out.println("[Ultrasound " + equipmentId + "] Acoustic frame captured and rendered to display buffer.");
    }

    public String getProbeType() { return probeType; }

    @Override
    public Map<String, String> getSpecificDetails() {
        Map<String, String> d = new LinkedHashMap<>();
        d.put("Probe Type", probeType);
        return d;
    }
}
