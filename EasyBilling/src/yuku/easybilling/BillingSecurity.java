// Copyright 2010 Google Inc. All Rights Reserved.

package yuku.easybilling;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Security-related methods. For a secure implementation, all of this code
 * should be implemented on a server that communicates with the application on
 * the device. For the sake of simplicity and clarity of this example, this code
 * is included here and is executed on the device. If you must verify the
 * purchases on the phone, you should obfuscate this code to make it harder for
 * an attacker to replace the code with stubs that treat all purchases as
 * verified.
 */
public class BillingSecurity {
	private static final String TAG = "BillingService";

	public static class SignedData {
		public long nonce;
		public List<Order> orders;
		
		public static class Order {
			public String notificationId;
			public String orderId;
			public String packageName;
			public String productId;
			public long purchaseTime;
			public PurchaseState purchaseState;
			public String developerPayload;
		}
	}

	/**
	 * Verifies that the data was signed with the given signature.
	 * 
	 * @param inapp_signed_data
	 *            the signed JSON string (signed, not encrypted)
	 * @param inapp_signature
	 *            the signature for the data, signed with the private key
	 */
	static boolean verifySignedData(String inapp_signed_data, String inapp_signature) {
		if (!TextUtils.isEmpty(inapp_signature) && !TextUtils.isEmpty(inapp_signed_data)) {
			return verify(getPublicKey(), inapp_signed_data, inapp_signature);
		}
		return false;
	}

	static SignedData decodeSignedData(String inapp_signed_data) {
		SignedData res = new SignedData();
		
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(inapp_signed_data);
		} catch (JSONException e) {
			Log.e(TAG, "json error decoding: " + jObject, e);
			return null;
		}
		
		res.nonce = jObject.optLong("nonce");
		
		JSONArray jTransactionsArray = jObject.optJSONArray("orders");
		if (jTransactionsArray == null) {
			res.orders = new ArrayList<SignedData.Order>(0);
		} else {
			res.orders = new ArrayList<SignedData.Order>(jTransactionsArray.length());
		}
		
		for (int i = 0; i < jTransactionsArray.length(); i++) {
			JSONObject jElement = jTransactionsArray.optJSONObject(i);
			
			SignedData.Order order = new SignedData.Order();
			order.notificationId = jElement.optString("notificationId");
			order.orderId = jElement.optString("orderId");
			order.packageName = jElement.optString("packageName");
			order.productId = jElement.optString("productId");
			order.purchaseTime = jElement.optLong("purchaseTime");
			order.purchaseState = PurchaseState.valueOf(jElement.optInt("purchaseState"));
			order.developerPayload = jElement.optString("developerPayload");
			res.orders.add(order);
		}
		
		return res;
	}

	/**
	 * Generates a PublicKey instance from a string containing the
	 * Base64-encoded public key.
	 * 
	 * @param encodedPublicKey
	 *            Base64-encoded public key
	 * @throws IllegalArgumentException
	 *             if encodedPublicKey is invalid
	 */
	public static PublicKey getPublicKey() {
		try {
			byte[] decodedKey = Base64.decode(EasyBilling.base64Key, Base64.DEFAULT);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeySpecException e) {
			Log.e(TAG, "Invalid key specification.");
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Verifies that the signature from the server matches the computed
	 * signature on the data. Returns true if the data is correctly signed.
	 * 
	 * @param publicKey
	 *            public key associated with the developer account
	 * @param inapp_signed_data
	 *            signed data from server
	 * @param inapp_signature
	 *            server signature
	 * @return true if the data and signature match
	 */
	public static boolean verify(PublicKey publicKey, String inapp_signed_data, String inapp_signature) {
		try {
			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(publicKey);
			sig.update(inapp_signed_data.getBytes());
			if (!sig.verify(Base64.decode(inapp_signature, Base64.DEFAULT))) {
				Log.e(TAG, "Signature verification failed.");
				return false;
			}
			return true;
		} catch (Exception e) {
			Log.e(TAG, "Verify got exception", e);
			return false;
		}
	}
}
