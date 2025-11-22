package util;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {

    private static final NumberFormat CURRENCY_INSTANCE = NumberFormat.getCurrencyInstance(new Locale("en", "US"));

    /**
     * Formats an integer price (in cents) into a currency string (e.g., $10.50).
     * @param priceInCents The price in cents.
     * @return A formatted currency string.
     */
    public static String formatCents(int priceInCents) {
        return CURRENCY_INSTANCE.format(priceInCents / 100.0);
    }
}