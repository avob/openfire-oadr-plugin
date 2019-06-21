package com.avob.server.openfire;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.AbstractComponent;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Presence;

public class OpenfireOadrComponent extends AbstractComponent {
	private static final Logger Log = LoggerFactory.getLogger(OpenfireOadrComponent.class);

	public static final String EVENT_SERVICE = "EiEvent";

	public static final String REPORT_SERVICE = "EiReport";

	public static final String REGISTERPARTY_SERVICE = "EiRegisterParty";

	public static final String OPT_SERVICE = "EiOpt";

	public static final String UPLINK_SERVICE = "uplink";

	private static final String NAMESPACE_OADR = "http://openadr.org/openadr2";

	private static final String NODE_EVENT = "http://openadr.org/OpenADR2/" + EVENT_SERVICE;
	public static final String LOCALPART_EVENT = "event";

	private static final String NODE_REPORT = "http://openadr.org/OpenADR2/" + REPORT_SERVICE;
	public static final String LOCALPART_REPORT = "report";

	private static final String NODE_REGISTERPARTY = "http://openadr.org/OpenADR2/" + REGISTERPARTY_SERVICE;
	public static final String LOCALPART_REGISTERPARTY = "registerparty";

	private static final String NODE_OPT = "http://openadr.org/OpenADR2/" + OPT_SERVICE;
	public static final String LOCALPART_OPT = "opt";

	private String domain;
	private OadrManager oadrManager;

	public OpenfireOadrComponent(OadrManager oadrManager, String domain) {
		this.domain = domain;
		this.oadrManager = oadrManager;

	}

	@Override
	protected IQ handleDiscoInfo(IQ iq) {
		final IQ replyPacket = IQ.createResultIQ(iq);
		final Element responseElement = replyPacket.setChildElement("query", NAMESPACE_DISCO_INFO);

		// features
		responseElement.addElement("feature").addAttribute("var", NAMESPACE_DISCO_INFO);
		responseElement.addElement("feature").addAttribute("var", NAMESPACE_OADR);

		replyPacket.setChildElement(responseElement);
		return replyPacket;

	}

	@Override
	protected IQ handleDiscoItems(IQ iq) {
		final IQ replyPacket = IQ.createResultIQ(iq);
		final Element responseElement = replyPacket.setChildElement("query", NAMESPACE_DISCO_ITEMS);
		Element childElement = iq.getChildElement();
		String node = childElement.attributeValue("node");
		String serviceNode = NAMESPACE_OADR + "#services";
		if (serviceNode.equals(node)) {

			responseElement.addElement("item").addAttribute("jid", LOCALPART_EVENT + "@" + domain).addAttribute("node",
					NODE_EVENT);

			responseElement.addElement("item").addAttribute("jid", LOCALPART_REPORT + "@" + domain).addAttribute("node",
					NODE_REPORT);

			responseElement.addElement("item").addAttribute("jid", LOCALPART_REGISTERPARTY + "@" + domain)
					.addAttribute("node", NODE_REGISTERPARTY);

			responseElement.addElement("item").addAttribute("jid", LOCALPART_OPT + "@" + domain).addAttribute("node",
					NODE_OPT);
		}
		replyPacket.setChildElement(responseElement);
		return replyPacket;
	}

	@Override
	protected void handleMessage(final Message message) {
		Log.info("Message");
		Log.info(message.toXML());

		if (message.getTo() != null && message.getFrom() != null) {
			String to = message.getTo().toString();

			String vtnId = null;

			if (to.equals(OpenfireOadrComponent.LOCALPART_EVENT + "@" + domain)) {

				vtnId = oadrManager.getEventJid();

			} else if (to.equals(OpenfireOadrComponent.LOCALPART_REGISTERPARTY + "@" + domain)) {

				vtnId = oadrManager.getRegisterPartyJid();

			} else if (to.equals(OpenfireOadrComponent.LOCALPART_REPORT + "@" + domain)) {

				vtnId = oadrManager.getReportJid();

			} else if (to.equals(OpenfireOadrComponent.LOCALPART_OPT + "@" + domain)) {

				vtnId = oadrManager.getOptJid();

			}

			if (vtnId != null) {
				Message createCopy = message.createCopy();
				createCopy.setTo(vtnId);

				this.send(createCopy);

			}

		}
	}

	@Override
	protected void handlePresence(final Presence presence) {
		Log.info("Presence");
		Log.info(presence.toXML());
	}

	@Override
	protected void handleIQResult(final IQ iq) {

		Log.info("IQ");
		Log.info(iq.toXML());
	}

	@Override
	public void start() {
		super.start();
		Log.info("Oadr component started");
	}

	@Override
	public String getDescription() {
		return "OpenADR VTN Connector plugin";
	}

	@Override
	public String getName() {
		return "OadrVTNConnector";
	}

}
