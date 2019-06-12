package com.avob.server.openfire;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.event.SessionEventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenfireOadrPlugin implements Plugin {
	private static final Logger Log = LoggerFactory.getLogger(OpenfireOadrIQHandler.class);

	private OpenfireOadrIQHandler oadrIQHandler;
	private OpenfireOadrSessionListener openfireOadrSessionListener;

	private List<String> getToBeRemovedFeature() throws IOException, URISyntaxException {
		File file = new File(getClass().getClassLoader().getResource("to_be_removed_feature.txt").getFile());
		List<String> features = new ArrayList<>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				// read next line
				line = reader.readLine();
				features.add(line.trim());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return features;
	}

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		XMPPServer server = XMPPServer.getInstance();

		// init session listener
		openfireOadrSessionListener = new OpenfireOadrSessionListener();
		SessionEventDispatcher.addListener(openfireOadrSessionListener);

		// init iq handler
		oadrIQHandler = new OpenfireOadrIQHandler();
		IQRouter iqRouter = server.getIQRouter();
		iqRouter.addHandler(oadrIQHandler);
		Log.debug("http://jabber.org/protocol/rsm");
		server.getIQDiscoInfoHandler().removeServerFeature("http://jabber.org/protocol/rsm");
		server.getIQPEPHandler().stop();
		server.getIQRegisterHandler().stop();
		server.getPubSubModule().stop();
		try {
			for (String feature : getToBeRemovedFeature()) {
				Log.debug(feature.trim());
				server.getIQDiscoInfoHandler().removeServerFeature(feature.trim());
			}
		} catch (IOException e) {
			Log.error(e.getMessage());
		} catch (URISyntaxException e) {
			Log.error(e.getMessage());
		}

		for (final Iterator<String> it = oadrIQHandler.getFeatures(); it.hasNext();) {
			XMPPServer.getInstance().getIQDiscoInfoHandler().addServerFeature(it.next());
		}

	}

	@Override
	public void destroyPlugin() {
		SessionEventDispatcher.removeListener(openfireOadrSessionListener);
		for (final Iterator<String> it = oadrIQHandler.getFeatures(); it.hasNext();) {
			XMPPServer.getInstance().getIQDiscoInfoHandler().removeServerFeature(it.next());
		}

		oadrIQHandler = null;
		openfireOadrSessionListener = null;
	}

}
