import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class DesignModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private Room room;
    private List<Furniture> furnitureList;
    private Furniture selectedFurniture;

    public DesignModel() {
        room = new Room(5.0f, 5.0f, 3.0f); // Default rectangular room
        furnitureList = new ArrayList<>();
        selectedFurniture = null;
    }

    public Room getRoom() { return room; }
    public List<Furniture> getFurnitureList() { return furnitureList; }
    public Furniture getSelectedFurniture() { return selectedFurniture; }

    public void addFurniture(Furniture furniture) {
        if (furniture != null) {
            furnitureList.add(furniture);
            setSelectedFurniture(furniture); // Automatically select newly added furniture
        }
    }

    public void removeFurniture(Furniture furniture) {
        if (furniture != null) {
            if (selectedFurniture == furniture) {
                setSelectedFurniture(null); // Deselect if the removed item was selected
            }
            furnitureList.remove(furniture);
        }
    }

    public void setSelectedFurniture(Furniture furniture) {
        this.selectedFurniture = furniture;
        // If selecting null, ensure no item in the list thinks it's selected (though this shouldn't happen)
        // No, direct comparison is sufficient.
    }

    public void clearDesign() {
        furnitureList.clear();
        room = new Room(5.0f, 5.0f, 3.0f); // Reset to default room
        selectedFurniture = null;
    }

}