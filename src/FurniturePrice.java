import java.io.Serializable;

/**
 * Stores pricing information for furniture items.
 * This class is used for inventory management and price calculation.
 */
public class FurniturePrice implements Serializable {
    private static final long serialVersionUID = 1L;

    private String furnitureType;
    private double basePrice;
    private double pricePerUnitVolume;
    private String currency = "USD";

    /**
     * Creates a new furniture price entry
     * @param furnitureType The type of furniture (e.g., "Chair", "Sofa")
     * @param basePrice The base price of the furniture
     * @param pricePerUnitVolume Additional price per unit volume (width × depth × height)
     */
    public FurniturePrice(String furnitureType, double basePrice, double pricePerUnitVolume) {
        this.furnitureType = furnitureType;
        this.basePrice = basePrice;
        this.pricePerUnitVolume = pricePerUnitVolume;
    }

    /**
     * Calculate the price for a specific furniture item based on its dimensions
     * @param width The width of the furniture
     * @param depth The depth of the furniture
     * @param height The height of the furniture
     * @return The calculated price
     */
    public double calculatePrice(float width, float depth, float height) {
        double volume = width * depth * height;
        return basePrice + (pricePerUnitVolume * volume);
    }

    // Getters and setters
    public String getFurnitureType() {
        return furnitureType;
    }

    public void setFurnitureType(String furnitureType) {
        this.furnitureType = furnitureType;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getPricePerUnitVolume() {
        return pricePerUnitVolume;
    }

    public void setPricePerUnitVolume(double pricePerUnitVolume) {
        this.pricePerUnitVolume = pricePerUnitVolume;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}