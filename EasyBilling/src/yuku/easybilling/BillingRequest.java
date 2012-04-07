package yuku.easybilling;

import android.content.Context;
import android.os.Bundle;

import yuku.easybilling.BillingResult.CheckBillingSupportedResult;
import yuku.easybilling.BillingResult.ConfirmNotificationsResult;
import yuku.easybilling.BillingResult.GetPurchaseInformationResult;
import yuku.easybilling.BillingResult.RequestPurchaseResult;
import yuku.easybilling.BillingResult.RestoreTransactionsResult;


public abstract class BillingRequest {
	public static final String TAG = BillingRequest.class.getSimpleName();
	private final Context context;
	
	public static class CheckBillingSupportedRequest extends BillingRequest {
		public CheckBillingSupportedRequest(Context context) {
			super(context);
		}

		@Override public Bundle getRequestBundle() {
			Bundle res = makeRequestBundle("CHECK_BILLING_SUPPORTED");
			return res;
		}

		@Override public CheckBillingSupportedResult parseResultBundle(Bundle resultBundle) {
			CheckBillingSupportedResult res = new CheckBillingSupportedResult();
			res.responseCode = getResponseCodeFromResultBundle(resultBundle);
			return res;
		}
	}
	
	public static class RequestPurchaseRequest extends BillingRequest {
		private final String itemId;
		private final String optionalDeveloperPayload;
		
		public RequestPurchaseRequest(Context context, String itemId, String optionalDeveloperPayload) {
			super(context);
			this.itemId = itemId;
			this.optionalDeveloperPayload = optionalDeveloperPayload;
		}
		
		@Override public Bundle getRequestBundle() {
			Bundle res = makeRequestBundle("REQUEST_PURCHASE");
			res.putString("ITEM_ID", itemId);
			if (optionalDeveloperPayload != null) res.putString("DEVELOPER_PAYLOAD", optionalDeveloperPayload);
			return res;
		}
		
		@Override public RequestPurchaseResult parseResultBundle(Bundle resultBundle) {
			RequestPurchaseResult res = new RequestPurchaseResult();
			res.responseCode = getResponseCodeFromResultBundle(resultBundle);
			res.requestId = resultBundle.getLong("REQUEST_ID");
			res.purchaseIntent = resultBundle.getParcelable("PURCHASE_INTENT");
			return res;
		}
	}
	
	public static class GetPurchaseInformationRequest extends BillingRequest {
		private final long nonce;
		private final String[] notifyIds;
		
		public GetPurchaseInformationRequest(Context context, long nonce, String[] notifyIds) {
			super(context);
			this.nonce = nonce;
			this.notifyIds = notifyIds;
		}
		
		// TODO fix android docs on type of notifyIds
		@Override public Bundle getRequestBundle() {
			Bundle res = makeRequestBundle("GET_PURCHASE_INFORMATION");
			res.putLong("NONCE", nonce);
			res.putStringArray("NOTIFY_IDS", notifyIds);
			return res;
		}
		
		@Override public GetPurchaseInformationResult parseResultBundle(Bundle resultBundle) {
			GetPurchaseInformationResult res = new GetPurchaseInformationResult();
			res.responseCode = getResponseCodeFromResultBundle(resultBundle);
			res.requestId = resultBundle.getLong("REQUEST_ID");
			return res;
		}
	}
	
	public static class ConfirmNotificationsRequest extends BillingRequest {
		private final String[] notifyIds;
		
		public ConfirmNotificationsRequest(Context context, String[] notifyIds) {
			super(context);
			this.notifyIds = notifyIds;
		}
		
		@Override public Bundle getRequestBundle() {
			Bundle res = makeRequestBundle("CONFIRM_NOTIFICATIONS");
			res.putStringArray("NOTIFY_IDS", notifyIds);
			return res;
		}
		
		@Override public ConfirmNotificationsResult parseResultBundle(Bundle resultBundle) {
			ConfirmNotificationsResult res = new ConfirmNotificationsResult();
			res.responseCode = getResponseCodeFromResultBundle(resultBundle);
			res.requestId = resultBundle.getLong("REQUEST_ID");
			return res;
		}
	}
	
	public static class RestoreTransactionsRequest extends BillingRequest {
		private final long nonce;

		public RestoreTransactionsRequest(Context context, long nonce) {
			super(context);
			this.nonce = nonce;
		}

		@Override public Bundle getRequestBundle() {
			Bundle res = makeRequestBundle("RESTORE_TRANSACTIONS");
			res.putLong("NONCE", nonce);
			return res;
		}

		@Override public RestoreTransactionsResult parseResultBundle(Bundle resultBundle) {
			RestoreTransactionsResult res = new RestoreTransactionsResult();
			res.responseCode = getResponseCodeFromResultBundle(resultBundle);
			res.requestId = resultBundle.getLong("REQUEST_ID");
			return res;
		}
	}
	
	public BillingRequest(Context context) {
		this.context = context;
	}

	public abstract Bundle getRequestBundle();
	public abstract <T extends BillingResult> BillingResult parseResultBundle(Bundle resultBundle);
	
	protected Bundle makeRequestBundle(String method) {
		Bundle request = new Bundle();
		request.putString("BILLING_REQUEST", method);
		request.putInt("API_VERSION", 1);
		request.putString("PACKAGE_NAME", context.getPackageName());
		return request;
	}
	
	public static ResponseCode getResponseCodeFromResultBundle(Bundle resultBundle) {
		return ResponseCode.valueOf(resultBundle.getInt("RESPONSE_CODE", -1));
	}
}
