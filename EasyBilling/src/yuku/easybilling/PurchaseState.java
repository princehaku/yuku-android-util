package yuku.easybilling;

// The possible states of an in-app purchase, as defined by Android Market.
public enum PurchaseState {
    // Responses to requestPurchase or restoreTransactions.
    PURCHASED,   // User was charged for the order.
    CANCELED,    // The charge failed on the server.
    REFUNDED;    // User received a refund for the order.

    // Converts from an ordinal value to the PurchaseState
    public static PurchaseState valueOf(int index) {
        PurchaseState[] values = PurchaseState.values();
        if (index < 0 || index >= values.length) {
            return CANCELED;
        }
        return values[index];
    }
}