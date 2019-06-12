package com.avob.server.openfire;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public class OadrFingerprint {

	private static final int OADR_FINGER_LENGTH = 29;
	private static final String OADR20B_SHA_ALGORITHM = "SHA-256";

	public static String getOadr20bFingerprint(X509Certificate cert) throws OadrFingerprintException {
		return OadrFingerprint.truncate(getFingerprint(cert, OADR20B_SHA_ALGORITHM));
	}

	private static String getFingerprint(X509Certificate cert, String shaVersion) throws OadrFingerprintException {

		MessageDigest md;
		try {
			md = MessageDigest.getInstance(shaVersion);
		} catch (NoSuchAlgorithmException e) {
			throw new OadrFingerprintException(e);
		}

		byte[] der;
		try {
			der = cert.getEncoded();
		} catch (CertificateEncodingException e) {
			throw new OadrFingerprintException(e);
		}

		md.update(der);
		byte[] digest = md.digest();
		return hexify(digest);
	}

	private static String hexify(byte[] bytes) {

		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

		StringBuilder buf = new StringBuilder(bytes.length * 2);
		String delimiter = "";
		for (int i = 0; i < bytes.length; ++i) {
			buf.append(delimiter);
			delimiter = ":";
			buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
			buf.append(hexDigits[bytes[i] & 0x0f]);
		}

		return buf.toString();
	}

	private static String truncate(String fingerprint) throws OadrFingerprintException {
		if (fingerprint.length() == OADR_FINGER_LENGTH) {
			return fingerprint;
		} else if (fingerprint.length() > OADR_FINGER_LENGTH) {
			return fingerprint.substring(fingerprint.length() - OADR_FINGER_LENGTH);
		} else {
			// whatever is appropriate in this case
			throw new OadrFingerprintException("Oadr fingerprint can't be generated");
		}
	}

}
