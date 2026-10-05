import java.util.ArrayList;

public class Main {
    // Method Overloading 1: Search by ID (Polymorphism)
    public static Rider searchRider(ArrayList<Rider> riders, String id) {
        for (Rider r : riders) {
            if (r.getId().equalsIgnoreCase(id)) return r;
        }
        return null;
    }

    // Method Overloading 2: Search first available by Vehicle Type (Polymorphism)
    public static Rider searchRider(ArrayList<Rider> riders, String vehicleType, boolean onlyAvailable) {
        for (Rider r : riders) {
            if (r.getVehicleType().equalsIgnoreCase(vehicleType) && (!onlyAvailable || r.isAvailable())) {
                return r;
            }
        }
        return null;
    }

    // Exception handling logic
    public static Rider findAvailableRider(ArrayList<Rider> riders) throws NoRiderAvailableException {
        for (Rider r : riders) {
            if (r.isAvailable()) {
                return r;
            }
        }
        throw new NoRiderAvailableException("All riders are currently busy on delivery!");
    }

    public static void main(String[] args) {
        System.out.println("=== 1. Core Classes & Polymorphism Testing ===");
        ArrayList<Rider> riders = new ArrayList<>();
        riders.add(new BikeRider("Rahim", "R1", "01700000001", 80.0, 15.0));
        riders.add(new CycleRider("Karim", "R2", "01700000002", 10.0));

        for (Rider r : riders) {
            System.out.println(r.getName() + " (" + r.getRoleDescription() + ")");
            System.out.printf("Estimated time for 10 km: %.2f hours\n", r.calculateDeliveryTime(10));
        }
        System.out.println("\n=== 2. Order Assignment & Interface Testing ===");
        Order order1 = new Order("ORD-101", "Tanvir", "KFC", "GEC Circle");
        Order order2 = new Order("ORD-102", "Sadia", "Pizza Hut", "2 No Gate");
        Order order3 = new Order("ORD-103", "Anik", "Handi", "Agrabad");

        try {
            // Assign Order 1
            Rider assigned1 = findAvailableRider(riders);
            assigned1.assignOrder(order1);

            // Assign Order 2
            Rider assigned2 = findAvailableRider(riders);
            assigned2.assignOrder(order2);

            // Try assigning Order 3 (Should trigger Custom Exception)
            System.out.println("\nAttempting to assign Order 3 when everyone is busy...");
            Rider assigned3 = findAvailableRider(riders);
            assigned3.assignOrder(order3);

        } catch (NoRiderAvailableException e) {
            System.out.println(">>> Exception Caught: " + e.getMessage());
        }

        System.out.println("\n=== 3. Completing Order & Freeing Rider ===");
        Rider rahim = searchRider(riders, "R1");
        if (rahim != null) {
            rahim.completeOrder();
        }

        System.out.println("\n=== 4. Re-attempting Assignment after Freeing Rider ===");
        try {
            Rider freeRider = findAvailableRider(riders);
            freeRider.assignOrder(order3);
            System.out.println(order3);
        } catch (NoRiderAvailableException e) {
            System.out.println(">>> Exception Caught: " + e.getMessage());
        }
    }
}