public class BikeRider extends Rider {
    private double fuelLevel;
    private double maxDistanceKm;

    public BikeRider(String name, String id, String phone, double fuelLevel, double maxDistanceKm) {
        super(name, id, phone, "Bike");
        this.fuelLevel = fuelLevel;
        this.maxDistanceKm = maxDistanceKm;
    }

    public double getFuelLevel() { 
        return fuelLevel; 
    }
    
    public double getMaxDistanceKm() { 
        return maxDistanceKm; 
    }

    @Override
    public String getRoleDescription() {
        return "Fast Motorbike Rider (Fuel: " + fuelLevel + "%, Max Dist: " + maxDistanceKm + " km)";
    }

    // Speed: 30 km/h avg
    @Override
    public double calculateDeliveryTime(double distanceKm) {
        return distanceKm / 30.0; 
    }
}