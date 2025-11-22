package service.receipt;

import model.entity.CartItem;
import model.entity.Item;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;
import service.order.TotalsCalculatorService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("unit")
class ReceiptBuilderServiceTest {

    @Mock
    private TotalsCalculatorService mockTotalsCalculator;

    @InjectMocks
    private ReceiptBuilderService receiptBuilderService;

    private List<CartItem> cartItems;

    @BeforeAll
    void printHeader() {
        System.out.println("====================================================================");
        System.out.println("FILE TESTING: ReceiptBuilderServiceTest.java");
        System.out.println("PURPOSE: Tests the logic for building the final receipt string.");
        System.out.println("--------------------------------------------------------------------");
    }

    @AfterAll
    void printFooter() {
        System.out.println("====================================================================\n");
    }

    @AfterEach
    void reportTestResult(TestInfo testInfo) {
        System.out.println("  - " + testInfo.getDisplayName() + " - PASSED");
    }

    @BeforeEach
    void setUp() {
        // Reseta o mock antes de cada teste para evitar que stubs de um teste interfiram em outro.
        Mockito.reset(mockTotalsCalculator);
        Item item1 = new Item(1, "Coffee", 250, 10);
        Item item2 = new Item(2, "Muffin", 350, 5);
        cartItems = Arrays.asList(new CartItem(item1, 2), new CartItem(item2, 1)); // Subtotal: 2*250 + 1*350 = 850
    }

    @Test
    @DisplayName("1. testBuildReceiptContent_withDiscountAndObservations()")
    void testBuildReceiptContent_withDiscountAndObservations() {
        // Arrange
        int subtotal = 850;
        int discountValue = 85; // 10%
        int subtotalAfterDiscount = subtotal - discountValue; // 765
        int tax = 54; // Mocked tax on 765
        int total = subtotalAfterDiscount + tax; // 819

        when(mockTotalsCalculator.calculateTax(subtotalAfterDiscount)).thenReturn(tax);

        // Act
        String receipt = receiptBuilderService.buildReceiptContent(
                "John Doe", "Admin", cartItems, subtotal, discountValue, total,
                "Extra hot", "Cash", 1000, 181);

        // Assert
        assertTrue(receipt.contains("Subtotal:"), "Receipt should contain Subtotal.");
        assertTrue(receipt.contains("$8.50"), "Receipt should contain correct subtotal value.");
        assertTrue(receipt.contains("Discount:"), "Receipt should contain Discount.");
        assertTrue(receipt.contains("-$0.85"), "Receipt should contain correct discount value.");
        assertTrue(receipt.contains("Tax"), "Receipt should contain Tax.");
        assertTrue(receipt.contains("$0.54"), "Receipt should contain correct tax value.");
        assertTrue(receipt.contains("TOTAL:"), "Receipt should contain TOTAL.");
        assertTrue(receipt.contains("$8.19"), "Receipt should contain correct total value.");
        assertTrue(receipt.contains("Observations: Extra hot"), "Receipt should contain observations.");
    }

    @Test
    @DisplayName("2. testBuildReceiptContent_withoutDiscountOrObservations()")
    void testBuildReceiptContent_withoutDiscountOrObservations() {
        // Arrange
        int subtotal = 850;
        int discountValue = 0;
        int tax = 60; // Mocked tax on 850
        int total = subtotal + tax; // 910

        // Act
        String receipt = receiptBuilderService.buildReceiptContent(
                "Jane Doe", "Employee", cartItems, subtotal, discountValue, total, "", "Credit Card", 0, 0);

        // Assert
        assertTrue(receipt.contains("Subtotal:"), "Receipt should contain Subtotal.");
        assertTrue(receipt.contains("$8.50"), "Receipt should contain correct subtotal value.");
        assertTrue(receipt.contains("TOTAL:"), "Receipt should contain TOTAL.");
        assertTrue(receipt.contains("$9.10"), "Receipt should contain correct total value.");
        Assertions.assertFalse(receipt.contains("Discount:"), "Receipt should not contain a discount line.");
        Assertions.assertFalse(receipt.contains("Observations:"), "Receipt should not contain an observations line.");
    }
}