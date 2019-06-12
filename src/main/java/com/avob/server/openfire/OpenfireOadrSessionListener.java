package com.avob.server.openfire;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.jivesoftware.openfire.event.SessionEventListener;
import org.jivesoftware.openfire.session.LocalSession;
import org.jivesoftware.openfire.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenfireOadrSessionListener implements SessionEventListener {

	private static final Logger Log = LoggerFactory.getLogger(OpenfireOadrSessionListener.class);

	public void sessionCreated(Session session) {
		handleSessionCreated(session);
	}

	public void sessionDestroyed(Session session) {
		// TODO Auto-generated method stub

	}

	public void anonymousSessionCreated(Session session) {
		handleSessionCreated(session);
	}

	public void anonymousSessionDestroyed(Session session) {
		// TODO Auto-generated method stub

	}

	public void resourceBound(Session session) {
		// TODO Auto-generated method stub

	}

	private void handleSessionCreated(Session session) {
		Certificate[] certs = ((LocalSession) session).getConnection().getPeerCertificates();

		Log.debug("session created");

		for (Certificate cert : certs) {

			if (cert instanceof X509Certificate) {
				final X509Certificate x509 = (X509Certificate) cert;
				try {
					String oadr20bFingerprint = OadrFingerprint.getOadr20bFingerprint(x509);
					Log.debug(oadr20bFingerprint);
				} catch (OadrFingerprintException e) {
					Log.error(e.getMessage());
				}

			}

		}
	}

}
