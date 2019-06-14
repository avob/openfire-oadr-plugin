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

public class OpenfireOadrSessionListener implements SessionEventListener {

	private static final Logger Log = LoggerFactory.getLogger(OpenfireOadrSessionListener.class);

	private SSLSocketFactory socketFactory;

	public OpenfireOadrSessionListener(SSLSocketFactory socketFactory) {
		this.socketFactory = socketFactory;
	}

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
//		AuthPro
	}

	private void handleSessionCreated(Session session) {
		Certificate[] certs = ((LocalSession) session).getConnection().getPeerCertificates();
//		if (session.getStatus() == Session.STATUS_AUTHENTICATED) {
//			return;
//		}
		String vtnId = JiveGlobals.getProperty(OpenfireOadrPlugin.OPENFIRE_OADR_VTN_ID_SYSTEM_PROPERTY);
		if (vtnId != null) {
			for (Certificate cert : certs) {
				if (cert instanceof X509Certificate) {
					final X509Certificate x509 = (X509Certificate) cert;
					try {
						String oadr20bFingerprint = OadrFingerprint.getOadr20bFingerprint(x509);
						if (vtnId.equals(oadr20bFingerprint)) {
							Log.info("vtn: " + oadr20bFingerprint + " connected with jid: "
									+ session.getAddress().toFullJID());
							return;
						} else {
							Log.info("potential ven: " + oadr20bFingerprint);

							boolean validateUserRole = validateUserRole(oadr20bFingerprint);
							if (validateUserRole) {
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
				if (roles.indexOf("ROLE_VEN") > -1) {
					return true;
				}
			}

		}
		return false;
	}

}
