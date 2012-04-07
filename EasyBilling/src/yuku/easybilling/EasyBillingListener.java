package yuku.easybilling;

public interface EasyBillingListener {
	void onInventoryAmountChange(String productId, PurchaseState purchaseState, int oldAmount, int newAmount);
}
