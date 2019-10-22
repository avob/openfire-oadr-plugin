package com.avob.server.openfire;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.AbstractComponent;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;

/**
 * This component declares a subdomain where VEN/VTN clients are supposed to
 * connect
 * 
 * It handles OADR Disco Info and Disco Items payloads
 * 
 * It routes OADR VEN traffic to VTN using OadrManager jids
 * 
 * It forbid VEN to VEN traffic
 * 
 * @author bzanni
 *
 */
public class OpenfireOadrComponent extends AbstractComponent {
	private static final Logger Log = LoggerFactory.getLogger(OpenfireOadrComponent.class);

	public static final String EVENT_SERVICE = "EiEvent";

	public static final String REPORT_SERVICE = "EiReport";

	public static final String REGISTERPARTY_SERVICE = "EiRegisterParty";

	public static final String OPT_SERVICE = "EiOpt";

	public static final String UPLINK_SERVICE = "uplink";

	public static final String VEN_CLIENT_RESOURCE = "client";

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
		// create oadr disco items for each VTN service
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
		if (message.getTo() != null && message.getFrom() != null) {
			String to = message.getTo().toString();

			String vtnServiceJid = null;

			// check if message 'to' jid match with one of declared VTN service jid
			if (to.equals(OpenfireOadrComponent.LOCALPART_EVENT + "@" + domain)) {

				vtnServiceJid = oadrManager.getEventJid();

			} else if (to.equals(OpenfireOadrComponent.LOCALPART_REGISTERPARTY + "@" + domain)) {

				vtnServiceJid = oadrManager.getRegisterPartyJid();

			} else if (to.equals(OpenfireOadrComponent.LOCALPART_REPORT + "@" + domain)) {

				vtnServiceJid = oadrManager.getReportJid();

			} else if (to.equals(OpenfireOadrComponent.LOCALPART_OPT + "@" + domain)) {

				vtnServiceJid = oadrManager.getOptJid();

			}

			// if so, change message 'to' field
			if (vtnServiceJid != null) {
				Message createCopy = message.createCopy();
				createCopy.setTo(vtnServiceJid);

				this.send(createCopy);

			}

		}
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
