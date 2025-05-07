import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

public class DesignModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private Room room;
    private List<Furniture> furnitureList;
    private Furniture selectedFurniture;
    private String createdBy; // Store the username of the creator


    public DesignModel() {
        room = new Room(5.0f, 5.0f, 3.0f);
        furnitureList = new ArrayList<>();
        selectedFurniture = null;
    }

    public Room getRoom() { return room; }
    public List<Furniture> getFurnitureList() { return furnitureList; }
    public Furniture getSelectedFurniture() { return selectedFurniture; }

    public void addFurniture(Furniture furniture) {
        if (furniture != null) {
            furnitureList.add(furniture);
            setSelectedFurniture(furniture);
        }
    }

    public void removeFurniture(Furniture furniture) {
        if (furniture != null) {
            if (selectedFurniture == furniture) {
                setSelectedFurniture(null);
            }
            furnitureList.remove(furniture);
        }
    }

    public void setSelectedFurniture(Furniture furniture) {
        this.selectedFurniture = furniture;
    }

    public void clearDesign() {
        furnitureList.clear();
        room = new Room(5.0f, 5.0f, 3.0f);
        selectedFurniture = null;
    }

    /**
     * Get the username of the creator
     * @return The username of the creator
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Set the username of the creator
     * @param username The username of the creator
     */
    public void setCreatedBy(String username) {
        this.createdBy = username;
    }

    /**
     * Calculate the total price for all furniture in this design
     * @return The total price of all furniture
     */
    public double calculateTotalPrice() {
        return InventoryManager.calculateTotalPrice(this);
    }

    /**
     * Get a detailed price breakdown for all furniture in this design
     * @return Map with furniture types as keys and their total prices as values
     */
    public Map<String, Double> getPriceBreakdown() {
        return InventoryManager.getPriceBreakdown(this);
    }

    /**
     * Get the count of each furniture type in the design
     * @return Map with furniture types as keys and their counts as values
     */
    public Map<String, Integer> getFurnitureCounts() {
        Map<String, Integer> counts = new HashMap<>();

        if (furnitureList != null) {
            for (Furniture furniture : furnitureList) {
                String type = furniture.getType();
                counts.put(type, counts.getOrDefault(type, 0) + 1);
            }
        }

        return counts;
    }
}