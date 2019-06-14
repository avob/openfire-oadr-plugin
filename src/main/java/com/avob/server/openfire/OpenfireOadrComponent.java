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
	private static final String NAMESPACE_OADR = "http://openadr.org/openadr2";

	private static final String NODE_EVENT = "http://openadr.org/OpenADR2/EiEvent";
	private static final String RESOURCE_EVENT = "/event";

	private static final String NODE_REPORT = "http://openadr.org/OpenADR2/EiReport";
	private static final String RESOURCE_REPORT = "/report";

	private String domain;

	public OpenfireOadrComponent(String domain) {
		this.domain = domain;
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

			responseElement.addElement("item").addAttribute("jid",   domain + RESOURCE_EVENT).addAttribute("node",
					NODE_EVENT);
			
			responseElement.addElement("item").addAttribute("jid",   domain + RESOURCE_REPORT).addAttribute("node",
					NODE_REPORT);
		}
		replyPacket.setChildElement(responseElement);
		return replyPacket;
	}

	@Override
	protected void handleMessage(final Message message) {

	}

	@Override
	protected void handlePresence(final Presence presence) {
		 
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
