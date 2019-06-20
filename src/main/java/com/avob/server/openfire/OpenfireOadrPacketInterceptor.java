package com.avob.server.openfire;

import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class OpenfireOadrPacketInterceptor implements PacketInterceptor {

	private static final Logger Log = LoggerFactory.getLogger(OpenfireOadrPacketInterceptor.class);

	private static final String EVENT_SERVICE = "EiEvent";

	private static final String REPORT_SERVICE = "EiReport";

	private static final String REGISTERPARTY_SERVICE = "EiRegisterParty";

	private OadrManager oadrManager;
	private String fullXmppDomain;
	private PacketRouter packetRouter;

	public OpenfireOadrPacketInterceptor(OadrManager oadrManager, String fullXmppDomain) {
		this.oadrManager = oadrManager;
		this.fullXmppDomain = fullXmppDomain;

		packetRouter = XMPPServer.getInstance().getPacketRouter();
	}

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {

//		Log.info(packet.toXML());
		if (processed) {
			return;
		}

		if (incoming) {

//			if (!oadrManager.isVtnConnected()) {
//				throw new PacketRejectedException("Traffic is inhibated because VTN is not connected");
//			}
			if (packet instanceof IQ) {

				IQ iq = (IQ) packet;

				if (iq.getChildElement() != null && iq.getChildElement().element("resource") != null) {
					String resource = iq.getChildElement().element("resource").getText();
					Log.info("vtn client: " + resource + " connected with jid: " + iq.getFrom().toString());
					String jid = iq.getFrom().getResource() + "@" + iq.getFrom().getDomain() + "/"
							+ iq.getFrom().getResource();
					switch (resource) {
					case REGISTERPARTY_SERVICE:

						Log.info("Set registerParty Jid: " + jid);
						oadrManager.setRegisterPartyJid(jid);
						break;

					case EVENT_SERVICE:
						Log.info("Set event Jid: " + jid);
						oadrManager.setEventJid(jid);
						break;

					case REPORT_SERVICE:
						Log.info("Set report Jid: " + jid);
						oadrManager.setReportJid(jid);
						break;
					}
				}
			} else if (packet instanceof Message) {

//				if (!packet.getTo().toString().contains(fullXmppDomain)
//						&& !packet.getFrom().toString().contains(fullXmppDomain)) {
//					Log.warn("from: " + packet.getFrom().toString() + " / to: " + packet.getTo().toString()
//							+ " packet has been inhibated");
//					throw new PacketRejectedException("Oadr plugin only allow VTN <-> VEN traffic");
//				}

//				if (packet.getTo() != null) {
//					String to = packet.getTo().toString();
//
//					JID vtnId = null;
//
//					if (to.equals(OpenfireOadrComponent.LOCALPART_EVENT + "@" + fullXmppDomain)) {
//
//						vtnId = oadrManager.getEventJid();
//
//					} else if (to.equals(OpenfireOadrComponent.LOCALPART_REGISTERPARTY + "@" + fullXmppDomain)) {
//
//						Log.info("Route to registerPartyService: " + oadrManager.getRegisterPartyJid().toString());
//						vtnId = oadrManager.getRegisterPartyJid();
//
//					} else if (to.equals(OpenfireOadrComponent.LOCALPART_REPORT + "@" + fullXmppDomain)) {
//
//						vtnId = oadrManager.getReportJid();
//
//					}
//
//					if (vtnId != null) {
//						Packet createCopy = packet.createCopy();
//						createCopy.setTo(vtnId);
//						packetRouter.route(createCopy);
//
//					}
//
//				}

//				

			}

		}

	}

}
