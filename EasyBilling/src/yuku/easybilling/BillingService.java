package yuku.easybilling;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import com.android.vending.billing.IMarketBillingService;

public class BillingService extends Service implements ServiceConnection {
	private static final String TAG = BillingService.class.getSimpleName();

	/** The service connection to the remote MarketBillingService. */
	private IMarketBillingService remoteService;
	private List<Pair<BillingRequest, BillingResultListener<? extends BillingResult>>> pendingRequests = new ArrayList<Pair<BillingRequest, BillingResultListener<? extends BillingResult>>>();

	@Override public void onCreate() {
		super.onCreate();
		Log.i(TAG, "@@onCreate");
	}

	public void setContext(Context context) {
		attachBaseContext(context);
	}

	@Override public IBinder onBind(Intent intent) {
		return null;
	}

	@Override public void onServiceConnected(ComponentName name, IBinder service) {
		Log.i(TAG, "Remote service connected.");
		remoteService = IMarketBillingService.Stub.asInterface(service);
		
		// run pending requests if any
		runPendingRequests();
	}
	
	@Override public void onServiceDisconnected(ComponentName name) {
		Log.d(TAG, "Remote service disconnected");
		remoteService = null;
	}

	@SuppressWarnings("unchecked") private void runPendingRequests() {
		Log.d(TAG, "@@runPendingRequests");
		
		while (true) {
			if (pendingRequests.size() == 0) {
				Log.d(TAG, "No more pendingRequests");
				break;
			} else {
				Log.d(TAG, "We have " + pendingRequests.size() + " pendingRequests");
			}
			
			if (remoteService == null) {
				Log.d(TAG, "runPendingRequests stopped because remoteService is disconnected");
				break;
			}
			
			// remoteService is available now!
			Pair<BillingRequest, BillingResultListener<? extends BillingResult>> entry = null;
			synchronized (pendingRequests) {
				if (pendingRequests.size() > 0) entry = pendingRequests.remove(0);
			}
			
			if (entry == null) {
				Log.d(TAG, "No more pendingRequests");
				break;
			}
			
			@SuppressWarnings("rawtypes") BillingResultListener listener = entry.second;
			try {
				BillingRequest request = entry.first;
				Bundle requestBundle = request.getRequestBundle();
				Bundle resultBundle = remoteService.sendBillingRequest(requestBundle);
				BillingResult result = request.parseResultBundle(resultBundle);
				if (listener == null) {
					Log.d(TAG, "Listener is null, we ignore the result (OK)");
				} else {
					listener.onBillingResult(BillingRequestStatus.DELAYED, result);
				}
			} catch (RemoteException e) {
				if (listener == null) {
					Log.d(TAG, "Listener is null, we ignore the result (RemoteException)");
				} else {
					listener.onBillingResult(BillingRequestStatus.REMOTE_EXCEPTION, null);
				}
			}
		}
	}

	public <T extends BillingResult> BillingRequestStatus request(BillingRequest request, BillingResultListener<T> resultListener) {
		BillingRequestStatus res = requestImpl(request, resultListener);
		Log.d(TAG, "request method result for " + request.getClass().getSimpleName() + ": " + res);
		return res;
	}

	@SuppressWarnings("unchecked") private <T extends BillingResult> BillingRequestStatus requestImpl(BillingRequest request, BillingResultListener<T> resultListener) {
		if (remoteService != null) { // we are connected to remote service.
			try {
				Bundle requestBundle = request.getRequestBundle();
				Bundle resultBundle = remoteService.sendBillingRequest(requestBundle);
				resultListener.onBillingResult(BillingRequestStatus.IMMEDIATE, (T) request.parseResultBundle(resultBundle));
				return BillingRequestStatus.IMMEDIATE;
			} catch (RemoteException e) {
				Log.e(TAG, "BillingService#request", e);
				return BillingRequestStatus.REMOTE_EXCEPTION;
			}
		} else {
			boolean bindResult = bindService(new Intent("com.android.vending.billing.MarketBillingService.BIND"), this, Context.BIND_AUTO_CREATE);
			if (bindResult) {
				synchronized (pendingRequests) {
					pendingRequests.add(new Pair<BillingRequest, BillingResultListener<?>>(request, resultListener));
				}
				return BillingRequestStatus.DELAYED;
			} else {
				return BillingRequestStatus.BIND_ERROR;
			}
		}
	}
}
