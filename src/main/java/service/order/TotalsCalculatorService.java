package service.order;

import model.entity.CartItem;
import java.util.Objects;

import java.util.List;

/**
 * A dedicated service for performing all order-related calculations.
 * This includes subtotal, taxes, and final totals, ensuring that business
 * logic for calculations is centralized.
 */
public class TotalsCalculatorService {

    // Tax rate is defined as an integer to avoid floating-point arithmetic.
    // 700 represents a 7% tax rate (700 / 10000).
    // This allows for precise calculations when dealing with cents.
    public static final int TAX_RATE_INTEGER = 700;

    /**
     * A simple Data Transfer Object (DTO) to hold all calculated total values.
     * This allows the service to return all computed values at once.
     */
    public static class OrderTotals {
        private final int subtotal;
        private final int discount;
        private final int tax;
        private final int total;

        public OrderTotals(int subtotal, int discount, int tax, int total) {
            this.subtotal = subtotal;
            this.discount = discount;
            this.tax = tax;
            this.total = total;
        }

        public int getSubtotal() { return subtotal; }
        public int getDiscount() { return discount; }
        public int getTax() { return tax; }
        public int getTotal() { return total; }
    }

    /**
     * Centralized method to calculate all order totals.
     * @param cartItems The list of items in the cart.
     * @param discountValue The pre-calculated discount amount.
     * @return An {@link OrderTotals} object containing all calculated values.
     */
    public OrderTotals calculateAllTotals(List<CartItem> cartItems, int discountValue) {
        int subtotal = calculateSubtotal(cartItems);
        int total = calculateFinalTotal(subtotal, discountValue);
        int tax = calculateTax(subtotal - discountValue);
        return new OrderTotals(subtotal, discountValue, tax, total);
    }

    /**
     * Calculates the subtotal of all items in the cart.
     * @param cartItems The list of items in the cart.
     * @return The calculated subtotal in cents.
     */
    public int calculateSubtotal(List<CartItem> cartItems) {
        return cartItems.stream()

        .mapToInt(item -> item.getPriceAtTimeOfSale() * item.getQuantity())
                .sum();
    }

    /**
     * Calculates the tax amount based on the subtotal after discounts.
     * @param subtotalAfterDiscount The subtotal minus any applicable discounts.
     * @return The calculated tax amount in cents, rounded to the nearest cent.
     */
    public int calculateTax(int subtotalAfterDiscount) {
        // Perform tax calculation using integer arithmetic to prevent floating-point inaccuracies.
        // The result is divided by 10000 to scale it back correctly.
        // Adding 5000 before division effectively rounds the result to the nearest cent.
        return (subtotalAfterDiscount * TAX_RATE_INTEGER + 5000) / 10000;
    }

    /**
     * Calculates the final total of the order.
     * @param subtotal The initial subtotal.
     * @param discountValue The value of the discount applied.
     * @return The final total amount in cents.
     */
    public int calculateFinalTotal(int subtotal, int discountValue) {
        int subtotalAfterDiscount = subtotal - discountValue;
        int tax = calculateTax(subtotalAfterDiscount);
        return subtotalAfterDiscount + tax;
    }
}