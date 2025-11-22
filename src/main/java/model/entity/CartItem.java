package model.entity;

/**
 * Represents an item within the shopping cart, tracking the item itself and the quantity.
 */
public class CartItem {
    private final Item item;
    private int quantity;
    private final int priceAtTimeOfSale;

    public CartItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
        // "Freeze" the price of the item at the moment it's added to the cart.
        this.priceAtTimeOfSale = item.getPrice();
    }

    public Item getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity for this cart item.
     * @param quantity The new quantity.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPriceAtTimeOfSale() {
        return priceAtTimeOfSale;
    }
}