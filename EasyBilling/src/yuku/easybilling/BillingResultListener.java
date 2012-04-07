package yuku.easybilling;

public interface BillingResultListener<T extends BillingResult> {
	void onBillingResult(BillingRequestStatus status, T result);
}
