package yuku.easybilling;

import android.app.PendingIntent;

class BillingResult {
	public static final String TAG = BillingResult.class.getSimpleName();
	
	public ResponseCode responseCode;
	
	public static class CheckBillingSupportedResult extends BillingResult {
	}
	
	public static class RequestPurchaseResult extends BillingResult {
		public long requestId;
		public PendingIntent purchaseIntent;
	}
	
	public static class GetPurchaseInformationResult extends BillingResult {
		public long requestId;
	}
	
	public static class ConfirmNotificationsResult extends BillingResult {
		public long requestId;
	}
	
	public static class RestoreTransactionsResult extends BillingResult {
		public long requestId;
	}
}
