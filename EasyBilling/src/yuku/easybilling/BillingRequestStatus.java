package yuku.easybilling;

public enum BillingRequestStatus {
	/** Got return value false when trying to bind to Google Play billing service */
	BIND_ERROR, 
	/** Got RemoteException when calling remote service method */
	REMOTE_EXCEPTION,
	/** Return value delivered immediately */
	IMMEDIATE,
	/** Return value delivered via listener because we need to bind to Google Play billing service first */
	DELAYED,
}
