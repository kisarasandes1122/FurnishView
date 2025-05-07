import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
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

}