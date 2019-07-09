package com.avob.server.openfire;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.jetty.http.HttpStatus;
import org.jivesoftware.openfire.event.SessionEventListener;
import org.jivesoftware.openfire.session.LocalSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author bzanni
 *
 */
public class OpenfireOadrSessionListener implements SessionEventListener {

	public static final String ROLE_VEN = "ROLE_VEN";

	public static final String ROLE_VTN = "ROLE_VTN";

	public static final String ROLE_SESSION_DATA_KEY = "ROLE";

	public static final String FINGERPRINT_SESSION_DATA_KEY = "fingerprint";

	private static final Logger Log = LoggerFactory.getLogger(OpenfireOadrSessionListener.class);

	private SSLSocketFactory socketFactory;
	private OadrManager oadrManager;

	public OpenfireOadrSessionListener(SSLSocketFactory socketFactory, OadrManager oadrManager) {
		this.socketFactory = socketFactory;
		this.oadrManager = oadrManager;
	}

	public void sessionCreated(Session session) {
		Log.info("sessionCreated");
		handleSessionCreated(session);
	}

	public void sessionDestroyed(Session session) {
		Log.info("sessionDestroyed");
		handleSessionDestroyed(session);

	}

	public void anonymousSessionCreated(Session session) {
		Log.info("anonymousSessionCreated");
		handleSessionCreated(session);
	}

	public void anonymousSessionDestroyed(Session session) {
		Log.info("anonymousSessionDestroyed");
		handleSessionDestroyed(session);

	}

	public void resourceBound(Session session) {
//		handleSessionCreated(session);
	}

	private void handleSessionDestroyed(Session session) {
		if (oadrManager.getEventJid() != null && oadrManager.getEventJid().equals(session.getAddress())) {
			oadrManager.setEventJid(null);
			String vtnId = JiveGlobals.getProperty(OpenfireOadrPlugin.OPENFIRE_OADR_VTN_ID_SYSTEM_PROPERTY);
			Log.info("vtn: " + vtnId + " event client disconnected");
		}
		if (oadrManager.getRegisterPartyJid() != null
				&& oadrManager.getRegisterPartyJid().equals(session.getAddress())) {
			oadrManager.setRegisterPartyJid(null);
			String vtnId = JiveGlobals.getProperty(OpenfireOadrPlugin.OPENFIRE_OADR_VTN_ID_SYSTEM_PROPERTY);
			Log.info("vtn: " + vtnId + " registerParty client disconnected");
		}
		if (oadrManager.getReportJid() != null && oadrManager.getReportJid().equals(session.getAddress())) {
			oadrManager.setReportJid(null);
			String vtnId = JiveGlobals.getProperty(OpenfireOadrPlugin.OPENFIRE_OADR_VTN_ID_SYSTEM_PROPERTY);
			Log.info("vtn: " + vtnId + " report client disconnected");
		}
		if (oadrManager.getUplinkJid() != null && oadrManager.getUplinkJid().equals(session.getAddress())) {
			oadrManager.setUplinkJid(null);
			String vtnId = JiveGlobals.getProperty(OpenfireOadrPlugin.OPENFIRE_OADR_VTN_ID_SYSTEM_PROPERTY);
			Log.info("vtn: " + vtnId + " uplink client disconnected");
		}

	}

	private void handleSessionCreated(Session session) {

		if (!(session instanceof LocalSession)) {
			return;
		}

		LocalSession localSession = (LocalSession) session;

		Certificate[] certs = localSession.getConnection().getPeerCertificates();

		String vtnId = JiveGlobals.getProperty(OpenfireOadrPlugin.OPENFIRE_OADR_VTN_ID_SYSTEM_PROPERTY);
		if (vtnId != null) {
			for (Certificate cert : certs) {
				if (cert instanceof X509Certificate) {
					final X509Certificate x509 = (X509Certificate) cert;
					try {
						String oadr20bFingerprint = OadrFingerprint.getOadr20bFingerprint(x509);
						if (vtnId.equals(oadr20bFingerprint)) {
							Log.info("vtn: " + oadr20bFingerprint + " client connected with jid: "
									+ session.getAddress().toFullJID());

							localSession.setSessionData(ROLE_SESSION_DATA_KEY, ROLE_VTN);

							return;
						} else {

							boolean validateUserRole = validateUserRole(oadr20bFingerprint);
							if (validateUserRole) {
								Log.info("ven: " + oadr20bFingerprint + " client connected with jid: "
										+ session.getAddress().toFullJID());

								localSession.setSessionData(FINGERPRINT_SESSION_DATA_KEY, oadr20bFingerprint);
								localSession.setSessionData(ROLE_SESSION_DATA_KEY, ROLE_VEN);

								return;
							}
						}

					} catch (OadrFingerprintException e) {
						Log.error(e.getMessage());
					} catch (Exception e) {
						Log.error(e.getMessage());
					}

				}
			}
		}
		Log.warn("Closing session with unknown client: " + session.getAddress().toFullJID());
		session.close();

	}

	// HTTP POST request
	private boolean validateUserRole(String username) throws Exception {

		String vtnAuthEnddpoint = JiveGlobals
				.getProperty(OpenfireOadrPlugin.OPENFIRE_OADR_VTN_AUTH_ENDPOINT_SYSTEM_PROPERTY);

		if (vtnAuthEnddpoint != null) {
			String url = vtnAuthEnddpoint + "/" + username;
			Log.info(url);
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			con.setSSLSocketFactory(socketFactory);
			// add reuqest header
			con.setRequestMethod("POST");

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();

			if (responseCode == HttpStatus.OK_200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				// print result
				String roles = response.toString();
				Log.info("username: " + username + " has role: " + roles);
				if (roles.indexOf(ROLE_VEN) > -1) {
					return true;
				}
			}

		}
		return false;
	}

}
