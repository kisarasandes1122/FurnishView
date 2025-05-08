import java.io.*;
import java.util.*;

/**
 * Manages the furniture inventory and pricing information.
 * Provides methods to add, update, and retrieve furniture prices.
 */
public class InventoryManager {
    private static final String INVENTORY_FILE = "./inventory.dat";
    private static Map<String, FurniturePrice> furniturePrices;

    static {
        // Initialize with default prices if no inventory exists
        furniturePrices = loadInventory();
        if (furniturePrices.isEmpty()) {
            initializeDefaultPrices();
            saveInventory();
        }
    }

    /**
     * Initialize default prices for all furniture types
     */
    private static void initializeDefaultPrices() {
        // Basic furniture
        furniturePrices.put("Chair", new FurniturePrice("Chair", 25000.00, 25000.00));
        furniturePrices.put("Sofa", new FurniturePrice("Sofa", 85000.00, 15000.00));
        furniturePrices.put("Dining Table", new FurniturePrice("Dining Table", 45000.00, 12000.00));
        furniturePrices.put("Side Table", new FurniturePrice("Side Table", 12500.00, 15000.00));
        furniturePrices.put("Bed", new FurniturePrice("Bed", 65000.00, 10000.00));
        furniturePrices.put("Bookshelf", new FurniturePrice("Bookshelf", 35000.00, 8000.00));

        // Seating
        furniturePrices.put("Armchair", new FurniturePrice("Armchair", 32000.00, 20000.00));
        furniturePrices.put("Dining Chair", new FurniturePrice("Dining Chair", 18000.00, 22000.00));
        furniturePrices.put("Office Chair", new FurniturePrice("Office Chair", 22000.00, 18000.00));
        furniturePrices.put("Stool", new FurniturePrice("Stool", 8500.00, 15000.00));
        furniturePrices.put("Bench", new FurniturePrice("Bench", 19999.00, 6000.00));
        furniturePrices.put("Recliner", new FurniturePrice("Recliner", 75000.00, 12500.00));
        furniturePrices.put("Ottoman", new FurniturePrice("Ottoman", 15000.00, 18000.00));

        // Tables
        furniturePrices.put("Coffee Table", new FurniturePrice("Coffee Table", 28000.00, 12000.00));
        furniturePrices.put("Desk", new FurniturePrice("Desk", 42000.00, 9500.00));
        furniturePrices.put("Console Table", new FurniturePrice("Console Table", 32500.00, 11000.00));
        furniturePrices.put("End Table", new FurniturePrice("End Table", 8999.00, 9500.00));

        // Storage
        furniturePrices.put("Wardrobe", new FurniturePrice("Wardrobe", 95000.00, 7500.00));
        furniturePrices.put("Dresser", new FurniturePrice("Dresser", 55000.00, 9000.00));
        furniturePrices.put("Filing Cabinet", new FurniturePrice("Filing Cabinet", 19999.00, 6000.00));
        furniturePrices.put("TV Stand", new FurniturePrice("TV Stand", 24999.00, 7000.00));
        furniturePrices.put("Chest of Drawers", new FurniturePrice("Chest of Drawers", 48000.00, 8500.00));

        // Beds
        furniturePrices.put("Twin Bed", new FurniturePrice("Twin Bed", 48000.00, 6000.00));
        furniturePrices.put("Queen Bed", new FurniturePrice("Queen Bed", 85000.00, 5500.00));
        furniturePrices.put("King Bed", new FurniturePrice("King Bed", 110000.00, 5000.00));
        furniturePrices.put("Bunk Bed", new FurniturePrice("Bunk Bed", 78000.00, 7500.00));
        furniturePrices.put("Murphy Bed", new FurniturePrice("Murphy Bed", 125000.00, 8000.00));

        // Miscellaneous
        furniturePrices.put("Headboard", new FurniturePrice("Headboard", 22000.00, 6500.00));
        furniturePrices.put("Crib", new FurniturePrice("Crib", 29999.00, 7000.00));
        furniturePrices.put("Chaise Lounge", new FurniturePrice("Chaise Lounge", 49999.00, 5000.00));
        furniturePrices.put("Futon", new FurniturePrice("Futon", 39999.00, 4500.00));
    }

    /**
     * Get the price for a specific furniture type
     * @param furnitureType The type of furniture
     * @return The FurniturePrice object, or null if not found
     */
    public static FurniturePrice getFurniturePrice(String furnitureType) {
        return furniturePrices.get(furnitureType);
    }

    /**
     * Calculate the price for a specific furniture item
     * @param furniture The furniture object
     * @return The calculated price, or 0.0 if price info not found
     */
    public static double calculateFurniturePrice(Furniture furniture) {
        if (furniture == null) return 0.0;

        FurniturePrice priceInfo = furniturePrices.get(furniture.getType());
        if (priceInfo == null) return 0.0;

        return priceInfo.calculatePrice(furniture.getWidth(), furniture.getDepth(), furniture.getHeight());
    }

    /**
     * Calculate the total price for all furniture in a design
     * @param designModel The design model containing furniture
     * @return The total price
     */
    public static double calculateTotalPrice(DesignModel designModel) {
        if (designModel == null || designModel.getFurnitureList() == null) return 0.0;

        double totalPrice = 0.0;
        for (Furniture furniture : designModel.getFurnitureList()) {
            totalPrice += calculateFurniturePrice(furniture);
        }

        return totalPrice;
    }

    /**
     * Get a detailed price breakdown for all furniture in a design
     * @param designModel The design model
     * @return Map with furniture types as keys and their total prices as values
     */
    public static Map<String, Double> getPriceBreakdown(DesignModel designModel) {
        Map<String, Double> breakdown = new HashMap<>();

        if (designModel == null || designModel.getFurnitureList() == null) {
            return breakdown;
        }

        for (Furniture furniture : designModel.getFurnitureList()) {
            String type = furniture.getType();
            double price = calculateFurniturePrice(furniture);

            // Add to existing price if the furniture type already exists
            if (breakdown.containsKey(type)) {
                breakdown.put(type, breakdown.get(type) + price);
            } else {
                breakdown.put(type, price);
            }
        }

        return breakdown;
    }

    /**
     * Update or add a furniture price entry
     * @param furnitureType The type of furniture
     * @param basePrice The base price
     * @param pricePerUnitVolume The price per unit volume
     * @return true if successful, false otherwise
     */
    public static boolean updateFurniturePrice(String furnitureType, double basePrice, double pricePerUnitVolume) {
        if (furnitureType == null || furnitureType.isEmpty()) return false;

        FurniturePrice priceInfo = new FurniturePrice(furnitureType, basePrice, pricePerUnitVolume);
        furniturePrices.put(furnitureType, priceInfo);

        return saveInventory();
    }

    /**
     * Get all furniture prices
     * @return Map of all furniture prices
     */
    public static Map<String, FurniturePrice> getAllFurniturePrices() {
        return new HashMap<>(furniturePrices);
    }

    /**
     * Save the inventory data to file
     * @return true if successful, false otherwise
     */
    private static boolean saveInventory() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(INVENTORY_FILE))) {
            oos.writeObject(furniturePrices);
            return true;
        } catch (Exception e) {
            System.err.println("Error saving inventory: " + e.getMessage());
            return false;
        }
    }

    /**
     * Load the inventory data from file
     * @return Map containing furniture prices
     */
    @SuppressWarnings("unchecked")
    private static Map<String, FurniturePrice> loadInventory() {
        File file = new File(INVENTORY_FILE);

        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (Map<String, FurniturePrice>) ois.readObject();
            } catch (Exception e) {
                System.err.println("Error loading inventory: " + e.getMessage());
            }
        }

        return new HashMap<>();
    }
}