package com.avob.server.openfire;

import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.LocalSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class OpenfireOadrPacketInterceptor implements PacketInterceptor {

	private static final Logger Log = LoggerFactory.getLogger(OpenfireOadrPacketInterceptor.class);

	private OadrManager oadrManager;
	private String fullXmppDomain;

	public OpenfireOadrPacketInterceptor(OadrManager oadrManager, String fullXmppDomain) {
		this.oadrManager = oadrManager;
		this.fullXmppDomain = fullXmppDomain;
	}

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {

//		Log.info(packet.toXML());
		if (processed) {
			return;
		}

		if (incoming) {

			if (!(session instanceof LocalSession)) {
				return;
			}

			LocalSession localSession = (LocalSession) session;

//			if (!oadrManager.isVtnConnected()) {
//				throw new PacketRejectedException("Traffic is inhibated because VTN is not connected");
//			}

			if (packet instanceof IQ) {

				IQ iq = (IQ) packet;

				if (
//						isVtn(localSession) && 
				iq.getElement().element("bind") != null) {
					Log.info("Intercept VTN iq");
//					Log.info(iq.toString());

					if (iq.getChildElement().element("resource") != null) {
						// VTN can setup a connection per Oadr Service (+1 for uplinks) by providing
						// resource in IQ bind payload
						// resource MUST be either EiEvent, EiReport, EiRegisterParty, uplink
						String resource = iq.getChildElement().element("resource").getText();

						String jid = iq.getFrom().getResource() + "@" + iq.getFrom().getDomain() + "/"
								+ iq.getFrom().getResource();
						switch (resource) {
						case OpenfireOadrComponent.REGISTERPARTY_SERVICE:
							Log.info("vtn client: " + resource + " connected with jid: " + iq.getFrom().toString());
							Log.info("Set registerParty Jid: " + jid);
							oadrManager.setRegisterPartyJid(jid);
							break;

						case OpenfireOadrComponent.EVENT_SERVICE:
							Log.info("vtn client: " + resource + " connected with jid: " + iq.getFrom().toString());
							Log.info("Set event Jid: " + jid);
							oadrManager.setEventJid(jid);
							break;

						case OpenfireOadrComponent.REPORT_SERVICE:
							Log.info("vtn client: " + resource + " connected with jid: " + iq.getFrom().toString());
							Log.info("Set report Jid: " + jid);
							oadrManager.setReportJid(jid);
							break;

						case OpenfireOadrComponent.OPT_SERVICE:
							Log.info("vtn client: " + resource + " connected with jid: " + iq.getFrom().toString());
							Log.info("Set opt Jid: " + jid);
							oadrManager.setOptJid(jid);
							break;

						case OpenfireOadrComponent.UPLINK_SERVICE:
							Log.info("vtn client: " + resource + " connected with jid: " + iq.getFrom().toString());
							Log.info("Set uplink Jid: " + jid);
							oadrManager.setUplinkJid(jid);

							break;

						case OpenfireOadrComponent.VEN_CLIENT_RESOURCE:
							Log.info("ven client: " + resource + " connected with jid: " + iq.getFrom().toString());
							break;
						}
					} else {
						// VTN can setup an unique connection by omiting re
						Log.info("vtn client connected with jid: " + iq.getFrom().toString());
						String jid = iq.getFrom().toString();
						oadrManager.setRegisterPartyJid(jid);
						oadrManager.setEventJid(jid);
						oadrManager.setReportJid(jid);
						oadrManager.setOptJid(jid);
						oadrManager.setUplinkJid(jid);

					}

				}
			} else if (packet instanceof Message) {

				Message message = (Message) packet;

				Log.info("Intercept ---------");
				Log.info(message.toXML());

				if (message.getFrom() != null && message.getTo() != null) {
					if (message.getFrom().toString().equals(oadrManager.getUplinkJid())) {

						Log.info("Intercept VTN msg from: " + message.getFrom().toString() + " to: "
								+ message.getTo().toString());

						String old = message.getFrom().toString();
						String vtnId = JiveGlobals.getProperty(OpenfireOadrPlugin.OPENFIRE_OADR_VTN_ID_SYSTEM_PROPERTY);

						if (vtnId != null) {
							String domain = message.getFrom().getDomain();
							String fromJid = vtnId + "@" + domain + "/uplink";
							message.setFrom(fromJid);

							Log.info("Change msg 'from' from: " + old + "to: " + fromJid);

						}

					} else if (message.getTo().toString().contains(fullXmppDomain)) {
						Log.info("Intercept VEN msg from: " + message.getFrom().toString() + " to: "
								+ message.getTo().toString());

						if (message.getFrom().equals(session.getAddress())) {

							JID old = message.getFrom();
							String domain = message.getFrom().getDomain();
							String resource = message.getFrom().getResource();
							String fromJid = localSession
									.getSessionData(OpenfireOadrSessionListener.FINGERPRINT_SESSION_DATA_KEY) + "@"
									+ domain + "/" + resource;
							message.setFrom(fromJid);

							Log.info("Change msg 'from' from: " + old.toString() + "to: " + fromJid);

						}
					}

				}

			}

		}

	}

}
