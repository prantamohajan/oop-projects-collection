import java.util.LinkedHashMap;
import java.util.Map;

public class PatientMonitor extends MedicalEquipment {
    private int heartRate;
    private String bloodPressure;

    public PatientMonitor(String equipmentId, String name, String manufacturer, String model) {
        super(equipmentId, name, manufacturer, model);
        this.heartRate = 75;
        this.bloodPressure = "120/80";
    }

    @Override
    public void startOperation() {
        this.status = EquipmentStatus.OPERATIONAL;
        System.out.println("[PatientMonitor " + equipmentId + "] Real-time vital sign tracking started.");
    }

    @Override
    public void stopOperation() {
        this.status = EquipmentStatus.OUT_OF_SERVICE;
        System.out.println("[PatientMonitor " + equipmentId + "] Vital sign tracking stopped.");
    }

    public void displayVitals() {
        System.out.println("[PatientMonitor " + equipmentId + "] Heart Rate: " + heartRate + " bpm | BP: " + bloodPressure);
    }

    public void updateVitals(int hr, String bp) {
        if (hr < 20 || hr > 250) {
            throw new IllegalArgumentException("Heart rate must be between 20 and 250 bpm.");
        }
        if (bp == null || !bp.matches("\\d{2,3}/\\d{2,3}")) {
            throw new IllegalArgumentException("Blood pressure must look like 120/80.");
        }
        this.heartRate = hr;
        this.bloodPressure = bp;
    }

    public int getHeartRate() { return heartRate; }

    public String getBloodPressure() { return bloodPressure; }

    @Override
    public Map<String, String> getSpecificDetails() {
        Map<String, String> d = new LinkedHashMap<>();
        d.put("Heart Rate", heartRate + " bpm");
        d.put("Blood Pressure", bloodPressure + " mmHg");
        return d;
    }
}
