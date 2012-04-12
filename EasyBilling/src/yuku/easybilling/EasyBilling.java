package yuku.easybilling;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.util.Log;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import yuku.easybilling.BillingRequest.CheckBillingSupportedRequest;
import yuku.easybilling.BillingRequest.ConfirmNotificationsRequest;
import yuku.easybilling.BillingRequest.GetPurchaseInformationRequest;
import yuku.easybilling.BillingRequest.RequestPurchaseRequest;
import yuku.easybilling.BillingRequest.RestoreTransactionsRequest;
import yuku.easybilling.BillingResult.CheckBillingSupportedResult;
import yuku.easybilling.BillingResult.ConfirmNotificationsResult;
import yuku.easybilling.BillingResult.GetPurchaseInformationResult;
import yuku.easybilling.BillingResult.RequestPurchaseResult;
import yuku.easybilling.BillingResult.RestoreTransactionsResult;
import yuku.easybilling.BillingSecurity.SignedData;

/** Static methods to call from anywhere! */
public class EasyBilling {
	public static final String TAG = EasyBilling.class.getSimpleName();
	
	static Context appContext;
	static BillingService service;
	static String base64Key;
	
	private static ResponseCode checkBillingSupportedResult; 
	
	private static WeakHashMap<EasyBillingListener, Object> listeners = new WeakHashMap<EasyBillingListener, Object>();
	private static final Object DUMMY = new Object();
	
	public interface OnBillingInitListener {
		void onBillingInitFinished();
	}
	
	public static void init(Context appContext, String base64Key) {
		init(appContext, base64Key, null);
	}
	
	public static void init(Context appContext, String base64Key, final OnBillingInitListener listener) {
		EasyBilling.appContext = appContext;
		EasyBilling.base64Key = base64Key;
		
		// for checking if billing supported
		initService();
		CheckBillingSupportedRequest request = new CheckBillingSupportedRequest(appContext);
		service.request(request, new BillingResultListener<CheckBillingSupportedResult>() {
			@Override public void onBillingResult(BillingRequestStatus status, CheckBillingSupportedResult result) {
				Log.d(TAG, "CheckBillingSupportedResult result: " + result.responseCode);
				checkBillingSupportedResult = result.responseCode;
				
				if (listener != null) {
					listener.onBillingInitFinished();
				}
			}
		});
	}

	/** Check if Billing is supported. 
	 * @return {@link ResponseCode#RESULT_OK} if yes, non-null if no, and null if no result yet.
	 */
	public static ResponseCode isBillingSupported() {
		return checkBillingSupportedResult;
	}
	
	/**
	 * This keeps track of the nonces that we generated and sent to the server.
	 * We need to keep track of these until we get back the purchase state and
	 * send a confirmation message back to Android Market. If we are killed and
	 * lose this list of nonces, it is not fatal. Android Market will send us a
	 * new "notify" message and we will re-generate a new nonce. This has to be
	 * "static" so that the {@link BillingReceiver} can check if a nonce exists.
	 */
	private static HashSet<Long> sKnownNonces = new HashSet<Long>();
	private static SecureRandom sRandom = new SecureRandom();

	/** Generates a nonce (a random number used once). */
	static long storeAndGetNonce() {
		long nonce = sRandom.nextLong();
		Log.i(TAG, "Nonce generated: " + nonce);
		sKnownNonces.add(nonce);
		return nonce;
	}

	static void removeNonce(long nonce) {
		sKnownNonces.remove(nonce);
	}

	static boolean isNonceKnown(long nonce) {
		return sKnownNonces.contains(nonce);
	}
	
	public static void addListener(EasyBillingListener listener) {
		listeners.put(listener, DUMMY);
	}
	
	public static void removeListener(EasyBillingListener listener) {
		listeners.remove(listener);
	}
	
	public static int getInventoryAmount(String productId) {
		return BillingDb.get(appContext).getInventoryAmount(productId);
	}
	
	public static Map<String, Integer> getAllInventory() {
		return BillingDb.get(appContext).getAllInventory();
	}
	
	private static void initService() {
		if (service == null) {
			service = new BillingService();
			service.setContext(appContext);
		}
	}
	
	public static BillingRequestStatus startPurchase(final Activity activity, String productId, String optionalDeveloperPayload) {
		initService();
		RequestPurchaseRequest request = new RequestPurchaseRequest(appContext, productId, optionalDeveloperPayload);
		return service.request(request, new BillingResultListener<RequestPurchaseResult>() {
			@Override public void onBillingResult(BillingRequestStatus status, RequestPurchaseResult result) {
				try {
					activity.startIntentSender(result.purchaseIntent.getIntentSender(), new Intent(), 0, 0, 0);
				} catch (SendIntentException e) {
					Log.e(TAG, "error starting activity", e);
				}
			}
		});
	}
	
	public static BillingRequestStatus startRestoreTransactions() {
		initService();
		RestoreTransactionsRequest request = new RestoreTransactionsRequest(appContext, storeAndGetNonce());
		return service.request(request, new BillingResultListener<RestoreTransactionsResult>() {
			@Override public void onBillingResult(BillingRequestStatus status, RestoreTransactionsResult result) {
				Log.d(TAG, "RestoreTransactions result: " + result.responseCode);
			}
		});
	}
	
	static void gotInAppNotify(String notification_id) {
		Log.d(TAG, "@@gotInAppNotify");
		
		String[] notification_ids = { notification_id };
		
		initService();
		GetPurchaseInformationRequest request = new GetPurchaseInformationRequest(appContext, storeAndGetNonce(), notification_ids);
		service.request(request, new BillingResultListener<GetPurchaseInformationResult>() {
			@Override public void onBillingResult(BillingRequestStatus status, GetPurchaseInformationResult result) {
				Log.d(TAG, "GetPurchaseInformation result: " + result.responseCode);
			}
		});
	}

	static void gotPurchaseStateChanged(String inapp_signed_data, String inapp_signature) {
		Log.d(TAG, "@@gotPurchaseStateChanged");
		
		boolean verified = BillingSecurity.verifySignedData(inapp_signed_data, inapp_signature);
		if (!verified) {
			Log.e(TAG, "Signature verification error");
			return;
		}
		
		SignedData signedData = BillingSecurity.decodeSignedData(inapp_signed_data);
		if (!isNonceKnown(signedData.nonce)) {
			Log.e(TAG, "Nonce is unknown: " + signedData.nonce);
			return;
		}
		
		// make a copy of current listeners
		Set<EasyBillingListener> listenerSet = new HashSet<EasyBillingListener>(listeners.keySet());
		
		List<String> notification_ids = new ArrayList<String>();
		for (SignedData.Order change: signedData.orders) {
			notification_ids.add(change.notificationId);
			BillingDb.get(appContext).updateWithChange(change, listenerSet);
		}
		
		initService();
		ConfirmNotificationsRequest request = new ConfirmNotificationsRequest(appContext, notification_ids.toArray(new String[notification_ids.size()]));
		service.request(request, new BillingResultListener<ConfirmNotificationsResult>() {
			@Override public void onBillingResult(BillingRequestStatus status, ConfirmNotificationsResult result) {
				Log.d(TAG, "ConfirmNotifications result: " + result.responseCode);
			}
		});
	}
}
