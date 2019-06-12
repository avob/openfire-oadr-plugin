package com.avob.server.openfire;

import java.util.Collections;
import java.util.Iterator;

import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.disco.ServerFeaturesProvider;
import org.jivesoftware.openfire.handler.IQHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

public class OpenfireOadrIQHandler extends IQHandler implements ServerFeaturesProvider {
	private static final Logger Log = LoggerFactory.getLogger(OpenfireOadrIQHandler.class);

	private final static IQHandlerInfo INFO = new IQHandlerInfo("services", "http://openadr.org/openadr2");

	public OpenfireOadrIQHandler() {
		super("OADR2: OpenADR vtn connector");
	}

	public Iterator<String> getFeatures() {
		return Collections.singleton(INFO.getNamespace()).iterator();
	}

	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		if (packet.isResponse()) {
			Log.debug("Silently ignoring IQ response: {}", packet);
			return null;
		}

		if (IQ.Type.set == packet.getType()) {
			Log.info("Responding with an error to an IQ request of type 'set': {}", packet);
			final IQ response = IQ.createResultIQ(packet);
			response.setError(PacketError.Condition.service_unavailable);
			return response;
		}

		final IQ response;
//		final String requestedType = packet.getChildElement().attributeValue("type");
		switch (packet.getChildElement().getName()) {
		case "services":
			response = IQ.createResultIQ(packet);
//			final Element childElement = response.setChildElement(packet.getChildElement().getName(),
//					packet.getChildElement().getNamespaceURI());
//			if (requestedType != null && !requestedType.isEmpty()) {
//				childElement.addAttribute("type", requestedType);
//			}

			break;

		default:
			Log.info(
					"Responding with an error to an IQ request for which the element name escaped by namespace is not understood: {}",
					packet);
			response = IQ.createResultIQ(packet);
			response.setError(PacketError.Condition.service_unavailable);
		}
		Log.info("Responding with {} to request {}", response.toXML(), packet.toXML());
		return response;
	}

	@Override
	public IQHandlerInfo getInfo() {
		return INFO;
	}

}
