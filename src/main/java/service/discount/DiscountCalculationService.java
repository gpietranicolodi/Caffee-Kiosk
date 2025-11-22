package service.discount;

import model.entity.Discount;

/**
 * A service class dedicated to the business logic of calculating discounts.
 * This decouples the calculation logic from the controller.
 */
public class DiscountCalculationService {

    /**
     * Calculates the total discount amount based on the selected discount type and subtotal.
     *
     * @param subtotal The order subtotal before any discounts.
     * @param selectedDiscount The {@link Discount} object selected by the user.
     * @param otherAmountStr The manually entered discount amount as a string.
     * @param isOtherPercentage True if the manual amount is a percentage, false if it is a fixed value.
     * @return The calculated discount value in cents.
     */
    public int calculateDiscount(int subtotal, Discount selectedDiscount, String otherAmountStr, boolean isOtherPercentage) {
        int discountValue = 0;

        if (selectedDiscount == null || "None".equalsIgnoreCase(selectedDiscount.getName())) {
            return 0;
        }

        // Handle manually entered ("Other") discounts
        if ("Other".equalsIgnoreCase(selectedDiscount.getName())) {
            if (otherAmountStr != null && !otherAmountStr.trim().isEmpty()) {
                try {
                    // Parse as double to handle decimal inputs (e.g., "2.50")
                    double otherAmount = Double.parseDouble(otherAmountStr);
                    if (isOtherPercentage) {
                        discountValue = (int) Math.round((subtotal * otherAmount) / 100.0);
                    } else {
                        // O valor já está em centavos, então apenas arredonde.
                        discountValue = (int) Math.round(otherAmount);
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid numbers, discount remains 0
                }
            }
        } else { // Handle pre-defined discounts from the database
            if (selectedDiscount.isPercentage()) {
                discountValue = (subtotal * selectedDiscount.getAmount()) / 100;
            } else {
                discountValue = selectedDiscount.getAmount();
            }
        }

        // Ensure the discount value is not negative and does not exceed the subtotal.
        // This prevents invalid order totals.
        return Math.max(0, Math.min(discountValue, subtotal));
    }
}