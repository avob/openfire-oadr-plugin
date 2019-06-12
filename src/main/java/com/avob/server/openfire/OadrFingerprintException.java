package com.avob.server.openfire;

public class OadrFingerprintException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6207364955553958180L;

	public OadrFingerprintException(String message, Exception e) {
		super(message, e);
	}

	public OadrFingerprintException(String message) {
		super(message);
	}

	public OadrFingerprintException(Exception e) {
		super(e);
	}
}
