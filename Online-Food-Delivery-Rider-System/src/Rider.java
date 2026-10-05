public abstract class Rider extends Person implements Assignable {
    private String vehicleType;
    private boolean isAvailable;
    private double rating;
    private Order currentOrder;

    public Rider(String name, String id, String phone, String vehicleType) {
        super(name, id, phone);
        this.vehicleType = vehicleType;
        this.isAvailable = true;
        this.rating = 5.0;
        this.currentOrder = null;
    }

    public String getVehicleType() { return vehicleType; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public Order getCurrentOrder() { return currentOrder; }

    // Interface realization
    @Override
    public void assignOrder(Order o) {
        this.currentOrder = o;
        this.isAvailable = false;
        o.setStatus("Assigned");
        System.out.println("-> " + getName() + " has been assigned to Order: " + o.getOrderId());
    }

    @Override
    public void completeOrder() {
        if (currentOrder != null) {
            currentOrder.setStatus("Delivered");
            System.out.println("-> Order " + currentOrder.getOrderId() + " marked Delivered by " + getName());
            this.currentOrder = null;
            this.isAvailable = true;
        } else {
            System.out.println("-> No active order to complete for " + getName());
        }
    }

    // Polymorphic method to be overridden by children
    public abstract double calculateDeliveryTime(double distanceKm);
}