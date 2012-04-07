package yuku.easybilling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class BillingReceiver extends BroadcastReceiver {
	private static final String TAG = BillingReceiver.class.getSimpleName();

	@Override public void onReceive(Context context, Intent intent) {
		{ // debug
			Log.d(TAG, "Intent passed to BillingReceiver: ");
			Log.d(TAG, "  action: " + intent.getAction());
			Log.d(TAG, "  data uri: " + intent.getData());
			Log.d(TAG, "  component name: " + intent.getComponent());
			Log.d(TAG, "  flags: 0x" + Integer.toHexString(intent.getFlags()));
			Log.d(TAG, "  mime: " + intent.getType());
			Bundle extras = intent.getExtras();
			Log.d(TAG, "  extras: " + (extras == null? "null": (extras.size() + " entries")));
			if (extras != null) {
				for (String key: extras.keySet()) {
					Log.d(TAG, "    " + key + " = " + extras.get(key));
				}
			}
		}
		
		String action = intent.getAction();

		if ("com.android.vending.billing.RESPONSE_CODE".equals(action)) {
			long request_id = intent.getLongExtra("request_id", -1);
			ResponseCode response_code = ResponseCode.valueOf(intent.getIntExtra("response_code", -1));
			Log.d(TAG, "Got RESPONSE_CODE with requestId: " + request_id + " responseCode: " + response_code);
			
		} else if ("com.android.vending.billing.IN_APP_NOTIFY".equals(action)) {
			String notification_id = intent.getStringExtra("notification_id");
			EasyBilling.gotInAppNotify(notification_id);
			
		} else if ("com.android.vending.billing.PURCHASE_STATE_CHANGED".equals(action)) {
			String inapp_signed_data = intent.getStringExtra("inapp_signed_data");
			String inapp_signature = intent.getStringExtra("inapp_signature");
			EasyBilling.gotPurchaseStateChanged(inapp_signed_data, inapp_signature);
			
		} else {
			Log.e(TAG, "unexpected action: " + action);
		}
	}
}