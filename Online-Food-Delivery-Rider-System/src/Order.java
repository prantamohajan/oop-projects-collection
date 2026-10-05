public class Order {
    private String orderId;
    private String customerName;
    private String restaurant;
    private String address;
    private String status; // "Pending", "Assigned", "Delivered"

    public Order(String orderId, String customerName, String restaurant, String address) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.restaurant = restaurant;
        this.address = address;
        this.status = "Pending";
    }

    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getRestaurant() { return restaurant; }
    public String getAddress() { return address; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Order #" + orderId + " | Customer: " + customerName + " | Restaurant: " + restaurant + " | Status: " + status;
    }
}