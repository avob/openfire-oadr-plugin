package com.avob.server.openfire;

import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.LocalSession;
import org.jivesoftware.openfire.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
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

				if (iq.getElement().element("bind") != null) {
					if (iq.getChildElement().element("resource") != null) {
						// VTN can setup a connection per Oadr Service (+1 for uplinks) by providing
						// resource in IQ bind payload
						// resource MUST be either EiEvent, EiReport, EiRegisterParty, uplink
						String resource = iq.getChildElement().element("resource").getText();
						Log.info("vtn client: " + resource + " connected with jid: " + iq.getFrom().toString());
						String jid = iq.getFrom().getResource() + "@" + iq.getFrom().getDomain() + "/"
								+ iq.getFrom().getResource();
						switch (resource) {
						case OpenfireOadrComponent.REGISTERPARTY_SERVICE:

							Log.info("Set registerParty Jid: " + jid);
							oadrManager.setRegisterPartyJid(jid);
							break;

						case OpenfireOadrComponent.EVENT_SERVICE:
							Log.info("Set event Jid: " + jid);
							oadrManager.setEventJid(jid);
							break;

						case OpenfireOadrComponent.REPORT_SERVICE:
							Log.info("Set report Jid: " + jid);
							oadrManager.setReportJid(jid);
							break;

						case OpenfireOadrComponent.OPT_SERVICE:
							Log.info("Set opt Jid: " + jid);
							oadrManager.setOptJid(jid);
							break;

						case OpenfireOadrComponent.UPLINK_SERVICE:
							Log.info("Set uplink Jid: " + jid);
							oadrManager.setUplinkJid(jid);
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

				if (message.getFrom() != null && message.getFrom().equals(session.getAddress())) {
					String resource = message.getFrom().getResource();
					String fromJid = localSession
							.getSessionData(OpenfireOadrSessionListener.FINGERPRINT_SESSION_DATA_KEY) + "@"
							+ fullXmppDomain + "/" + resource;
					message.setFrom(fromJid);
				}

			}

		}

	}

}
