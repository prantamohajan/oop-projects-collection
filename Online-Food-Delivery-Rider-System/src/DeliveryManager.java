import java.util.ArrayList;
import java.util.HashMap;

public class DeliveryManager {
    private HashMap<String, Rider> riderMap;
    private ArrayList<Order> orderList;

    public DeliveryManager() {
        riderMap = new HashMap<>();
        orderList = new ArrayList<>();
    }

    public void addRider(Rider rider) {
        riderMap.put(rider.getId(), rider);
    }

    public void addOrder(Order order) {
        orderList.add(order);
    }

    public HashMap<String, Rider> getRiderMap() {
        return riderMap;
    }

    public ArrayList<Order> getOrderList() {
        return orderList;
    }

    // মেথড ওভারলোডিং ১: আইডি দিয়ে রাইডার সার্চ
    public Rider searchRider(String id) {
        return riderMap.get(id);
    }

    // মেথড ওভারলোডিং ২: বাহনের ধরন ও অ্যাভেইলেবিলিটি দিয়ে সার্চ
    public Rider searchRider(String vehicleType, boolean onlyAvailable) {
        for (Rider r : riderMap.values()) {
            if (r.getVehicleType().equalsIgnoreCase(vehicleType)) {
                if (!onlyAvailable || r.isAvailable()) {
                    return r;
                }
            }
        }
        return null;
    }

    // দূরত্ব এবং বাহন ভিত্তিক স্মার্ট ডিসপ্যাচ লজিক
    public Rider assignRider(Order order, double distanceKm, String preference) throws NoRiderAvailableException {
        // ১. যদি ইউজার সরাসরি বাইক চেয়ে থাকে
        if ("Bike".equalsIgnoreCase(preference)) {
            Rider b = searchRider("Bike", true);
            if (b != null) {
                b.assignOrder(order);
                return b;
            }
            throw new NoRiderAvailableException("No Bike Rider available for Order #" + order.getOrderId());
        }

        // ২. যদি ইউজার সরাসরি সাইকেল চেয়ে থাকে
        if ("Cycle".equalsIgnoreCase(preference)) {
            if (distanceKm > 5.0) {
                throw new NoRiderAvailableException(
                        "Order distance (" + distanceKm + " km) is too far for a Cycle Rider! (Max 5 km)");
            }
            Rider c = searchRider("Bicycle", true);
            if (c != null) {
                c.assignOrder(order);
                return c;
            }
            throw new NoRiderAvailableException("No Cycle Rider available for Order #" + order.getOrderId());
        }

        // ৩. যদি Preference = "Any" থাকে (স্মার্ট ডিসট্যান্স বেসড সিলেকশন)
        if (distanceKm <= 5.0) {
            // কম দূরত্বে আগে সাইকেল রাইডারকে অগ্রাধিকার দেওয়া হবে
            Rider c = searchRider("Bicycle", true);
            if (c != null) {
                c.assignOrder(order);
                return c;
            }
            // সাইকেল খালি না থাকলে বাইক দেওয়া হবে
            Rider b = searchRider("Bike", true);
            if (b != null) {
                b.assignOrder(order);
                return b;
            }
        } else {
            // ৫ কিমির বেশি হলে সরাসরি বাইক রাইডার দেওয়া হবে
            Rider b = searchRider("Bike", true);
            if (b != null) {
                b.assignOrder(order);
                return b;
            }
        }

        throw new NoRiderAvailableException(
                "No suitable riders available to deliver Order #" + order.getOrderId() + " (" + distanceKm + " km)");
    }

    public boolean completeOrder(String riderId) {
        Rider rider = riderMap.get(riderId);
        if (rider != null && !rider.isAvailable()) {
            rider.completeOrder();
            return true;
        }
        return false;
    }

    public boolean removeOrder(String orderId) {
        return orderList.removeIf(o -> o.getOrderId().equals(orderId) && "Delivered".equalsIgnoreCase(o.getStatus()));
    }
}