package controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import model.entity.Category;
import model.entity.CartItem;
import model.entity.Discount;
import model.entity.Item;
import util.CurrencyFormatter;
import service.auth.SessionManager;
import service.cart.Cart;
import service.categories.CategoryList;
import service.discount.DiscountCalculationService;
import service.discount.DiscountService;
import service.menu.Catalog;
import service.order.TotalsCalculatorService;

import java.util.List;

/**
 * Main controller for the Caffee Kiosk application.
 * Manages UI updates and delegates actions to appropriate services and handlers.
 */
public class CaffeeController {

    //<editor-fold desc="FXML Injected Fields">
    @FXML private VBox categoryPanel;
    @FXML private TableView<Item> itemsTable;
    @FXML private TableColumn<Item, String> itemNameColumn;
    @FXML private TableColumn<Item, Integer> itemPriceColumn;
    @FXML private TableColumn<Item, Integer> itemInventoryColumn;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button addToCartButton;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> cartItemNameColumn;
    @FXML private TableColumn<CartItem, Integer> cartItemQtyColumn;
    @FXML private TableColumn<CartItem, String> cartItemPriceColumn;
    @FXML private TableColumn<CartItem, String> cartItemTotalColumn;
    @FXML public TextArea observationsTextArea;
    @FXML public ComboBox<Discount> discountComboBox;
    @FXML public TextField otherDiscountField;
    @FXML public CheckBox otherDiscountPercentageCheckBox;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private Label employeeLabel;
    @FXML private Label percentageLabel;
    //</editor-fold>

    //<editor-fold desc="Services and Controllers">
    private final CategoryList categoryListService;
    private final Catalog menuCatalogService;
    private final Cart cartService;
    private final DiscountService discountService;
    private final DiscountCalculationService discountCalculationService;
    private final TotalsCalculatorService totalsCalculatorService;
    private final HistoryController historyController;
    private CheckoutHandler checkoutHandler;
    private Runnable onLogoutListener;
    private int lastSelectedCategoryId = -1;
    //</editor-fold>

    public CaffeeController(CategoryList categoryListService, Catalog menuCatalogService, Cart cartService, DiscountService discountService, DiscountCalculationService discountCalculationService, TotalsCalculatorService totalsCalculatorService, HistoryController historyController) {
        this.categoryListService = categoryListService;
        this.menuCatalogService = menuCatalogService;
        this.cartService = cartService;
        this.discountService = discountService;
        this.discountCalculationService = discountCalculationService;
        this.totalsCalculatorService = totalsCalculatorService;
        this.historyController = historyController;
    }

    @FXML
    public void initialize() {
        setupTables();
        addListeners();
        Platform.runLater(() -> {
            percentageLabel.setText("%");
            loadInitialData();
        });
    }

    private void setupTables() {
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        itemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        itemInventoryColumn.setCellValueFactory(new PropertyValueFactory<>("inventory"));
        itemPriceColumn.setCellFactory(col -> new CurrencyFormattingCell<>());

        cartItemNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getName()));
        cartItemQtyColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartItemPriceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(CurrencyFormatter.formatCents(cellData.getValue().getPriceAtTimeOfSale())));
        cartItemTotalColumn.setCellValueFactory(cellData -> {
            int total = cellData.getValue().getPriceAtTimeOfSale() * cellData.getValue().getQuantity();
            return new SimpleStringProperty(CurrencyFormatter.formatCents(total));
        });
    }

    private void addListeners() {
        itemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isOutOfStock = newSelection == null || newSelection.getInventory() <= 0;
            addToCartButton.setDisable(isOutOfStock);
        });

        discountComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isOther = newSelection != null && "Other".equalsIgnoreCase(newSelection.getName());
            otherDiscountField.setDisable(!isOther);
            otherDiscountPercentageCheckBox.setDisable(!isOther);
            if (!isOther) {
                otherDiscountField.clear();
                otherDiscountPercentageCheckBox.setSelected(false);
            }
            updateTotals();
        });

        otherDiscountField.textProperty().addListener((obs, o, n) -> updateTotals());
        otherDiscountPercentageCheckBox.selectedProperty().addListener((obs, o, n) -> updateTotals());
    }

    private void loadInitialData() {
        loadCategories();
        loadDiscounts();
        refreshCart();
    }

    public void start() {
        employeeLabel.setText("Employee: " + SessionManager.getInstance().getLoggedInEmployeeName());
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryListService.getAllCategories();
            categoryPanel.getChildren().remove(1, categoryPanel.getChildren().size());
            for (Category category : categories) {
                Button categoryButton = new Button(category.getDescription());
                categoryButton.setPrefWidth(130);
                categoryButton.setOnAction(e -> handleCategorySelection(category.getId()));
                categoryPanel.getChildren().add(categoryButton);
            }
        } catch (Exception e) {
            showError("Error loading categories: " + e.getMessage());
        }
    }

    private void loadDiscounts() {
        try {
            List<Discount> discounts = discountService.getActiveDiscounts();
            discountComboBox.setItems(FXCollections.observableArrayList(discounts));
            discountComboBox.getItems().add(0, new Discount(0, "None", 0, false, true));
            discountComboBox.getItems().add(new Discount(-1, "Other", 0, false, true));
            discountComboBox.getSelectionModel().selectFirst();
        } catch (Exception e) {
            showError("Error loading discounts: " + e.getMessage());
        }
    }

    public void handleCategorySelection(int categoryId) {
        this.lastSelectedCategoryId = categoryId;
        try {
            List<Item> items = menuCatalogService.getItemsByCategory(categoryId);
            itemsTable.setItems(FXCollections.observableArrayList(items));
        } catch (Exception ex) {
            showError("Error loading items: " + ex.getMessage());
        }
    }

    @FXML
    private void handleAddItemToCart() {
        Item selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;
        int quantity = quantitySpinner.getValue();
        cartService.addItem(selectedItem, quantity);
        refreshCart();
        itemsTable.getSelectionModel().clearSelection();
        quantitySpinner.getValueFactory().setValue(1);
    }

    @FXML
    private void handleRemoveItem() {
        CartItem selectedCartItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedCartItem == null) return;
        cartService.removeItem(selectedCartItem);
        refreshCart();
    }

    @FXML
    private void handleClearCart() {
        cartService.clearCart();
        refreshCart();
    }

    @FXML
    private void handleCheckout() {
        checkoutHandler.startCheckout();
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clearSession();
        cartService.clearCart();
        if (onLogoutListener != null) {
            onLogoutListener.run();
        }
    }

    @FXML
    private void handleShowHistory() {
        if (historyController != null) {
            historyController.showHistory();
        }
    }

    public void refreshCart() {
        cartTable.setItems(FXCollections.observableArrayList(cartService.getCartItems()));
        updateTotals();
    }

    /**
     * Clears all transactional data from the UI and data services after a checkout is complete.
     */
    public void clearCartAndResetUI() {
        cartService.clearCart();
        observationsTextArea.clear();
        discountComboBox.getSelectionModel().selectFirst(); // Reset to "None"
        refreshCart(); // Update UI to show empty cart and reset totals

        // Refresh the item list to show updated inventory
        if (lastSelectedCategoryId != -1) {
            handleCategorySelection(lastSelectedCategoryId);
        }
    }

    private void updateTotals() {
        int subtotal = totalsCalculatorService.calculateSubtotal(cartService.getCartItems());
        int discount = discountCalculationService.calculateDiscount(subtotal, discountComboBox.getSelectionModel().getSelectedItem(), otherDiscountField.getText(), otherDiscountPercentageCheckBox.isSelected());

        TotalsCalculatorService.OrderTotals totals = totalsCalculatorService.calculateAllTotals(cartService.getCartItems(), discount);

        subtotalLabel.setText("Subtotal: " + CurrencyFormatter.formatCents(totals.getSubtotal()));
        discountLabel.setText("Discount: -" + CurrencyFormatter.formatCents(totals.getDiscount()));
        
        taxLabel.setText("Tax (" + (TotalsCalculatorService.TAX_RATE_INTEGER / 100) + "%): " + CurrencyFormatter.formatCents(totals.getTax()));
        totalLabel.setText("Total: " + CurrencyFormatter.formatCents(totals.getTotal()));
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.setHeaderText(null);
            alert.showAndWait();
        });
    }

    //<editor-fold desc="Getters and Setters">
    public int getLastSelectedCategoryId() {
        return lastSelectedCategoryId;
    }

    public ComboBox<Discount> getDiscountComboBox() {
        return discountComboBox;
    }

    public TextField getOtherDiscountField() {
        return otherDiscountField;
    }

    public CheckBox getOtherDiscountPercentageCheckBox() {
        return otherDiscountPercentageCheckBox;
    }

    public TextArea getObservationsTextArea() {
        return observationsTextArea;
    }

    public void setOnLogoutListener(Runnable onLogoutListener) {
        this.onLogoutListener = onLogoutListener;
    }

    public void setCheckoutHandler(CheckoutHandler checkoutHandler) {
        this.checkoutHandler = checkoutHandler;
    }
    //</editor-fold>

    private static class CurrencyFormattingCell<T> extends TableCell<T, Integer> {
        @Override
        protected void updateItem(Integer price, boolean empty) {
            super.updateItem(price, empty);
            if (empty || price == null) {
                setText(null);
            } else {
                setText(CurrencyFormatter.formatCents(price));
            }
        }
    }
}