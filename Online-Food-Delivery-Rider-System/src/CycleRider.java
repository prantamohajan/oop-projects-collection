public class CycleRider extends Rider {
    private double maxLoadKg;

    public CycleRider(String name, String id, String phone, double maxLoadKg) {
        super(name, id, phone, "Bicycle");
        this.maxLoadKg = maxLoadKg;
    }

    public double getMaxLoadKg() { return maxLoadKg; }

    @Override
    public String getRoleDescription() {
        return "Eco-friendly Cycle Rider (Max Load: " + maxLoadKg + " kg)";
    }

    // Speed: 12 km/h avg
    @Override
    public double calculateDeliveryTime(double distanceKm) {
        return distanceKm / 12.0; 
    }
}