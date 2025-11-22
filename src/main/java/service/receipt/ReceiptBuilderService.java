package service.receipt;

import model.entity.CartItem;
import model.entity.Discount;
import util.CurrencyFormatter;
import service.order.TotalsCalculatorService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A service dedicated to building the final, formatted string representation of a receipt.
 */
public class ReceiptBuilderService {

    private final TotalsCalculatorService totalsCalculatorService;

    public ReceiptBuilderService(TotalsCalculatorService totalsCalculatorService) {
        this.totalsCalculatorService = totalsCalculatorService;
    }

    public String buildReceiptContent(String customerName, String employeeName, List<CartItem> cartItems, int subtotal, int discountValue, int total, String observations, String paymentMethod, int amountTendered, int change) {
        StringBuilder receipt = new StringBuilder();

        receipt.append("\tOOP Caffee\n");
        receipt.append("----------------------------------------------------\n");
        receipt.append(String.format("%-25s %s\n", "Date:", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        receipt.append(String.format("%-25s %s\n", "Served by:", employeeName));
        receipt.append(String.format("%-25s %s\n", "Customer:", customerName));
        receipt.append("----------------------------------------------------\n");

        for (CartItem item : cartItems) {
            String lineItem = String.format("%d x %s", item.getQuantity(), item.getItem().getName());
            String lineTotal = CurrencyFormatter.formatCents(item.getPriceAtTimeOfSale() * item.getQuantity());
            receipt.append(String.format("%-38s %12s\n", lineItem, lineTotal));
        }

        receipt.append("----------------------------------------------------\n");
        receipt.append(String.format("%-38s %12s\n", "Subtotal:", CurrencyFormatter.formatCents(subtotal)));

        if (discountValue > 0) {
            String discountLabel = "Discount:";
            receipt.append(String.format("%-38s %12s\n", discountLabel, "-" + CurrencyFormatter.formatCents(discountValue)));
        }

        int subtotalAfterDiscount = subtotal - discountValue;
        int tax = totalsCalculatorService.calculateTax(subtotalAfterDiscount);
        String taxText = "Tax (" + (TotalsCalculatorService.TAX_RATE_INTEGER / 100) + "%):";
        receipt.append(String.format("%-38s %12s\n", taxText, CurrencyFormatter.formatCents(tax)));

        receipt.append(String.format("%-38s %12s\n", "TOTAL:", CurrencyFormatter.formatCents(total)));
        receipt.append("----------------------------------------------------\n");

        if (observations != null && !observations.trim().isEmpty()) {
            receipt.append("Observations: ").append(observations).append("\n");
            receipt.append("----------------------------------------------------\n");
        }

        receipt.append(String.format("%-25s %s\n", "Payment Method:", paymentMethod));
        if ("Cash".equals(paymentMethod)) {
            receipt.append(String.format("%-38s %12s\n", "Amount Tendered:", CurrencyFormatter.formatCents(amountTendered)));
            receipt.append(String.format("%-38s %12s\n", "Change:", CurrencyFormatter.formatCents(change)));
        }
        receipt.append("\n\tThank you for your visit!\n");
        return receipt.toString();
    }
}