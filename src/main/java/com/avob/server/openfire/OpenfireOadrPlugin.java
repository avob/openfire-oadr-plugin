package com.avob.server.openfire;

import java.io.File;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.DefaultAuthProvider;
import org.jivesoftware.openfire.auth.HybridAuthProvider;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.event.SessionEventDispatcher;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.keystore.IdentityStore;
import org.jivesoftware.openfire.keystore.TrustStore;
import org.jivesoftware.openfire.spi.ConnectionType;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;

public class OpenfireOadrPlugin implements Plugin {

	public static final String OPENFIRE_OADR_VTN_ID_SYSTEM_PROPERTY = "xmpp.oadr.vtnId";
	public static final String OPENFIRE_OADR_VTN_AUTH_ENDPOINT_SYSTEM_PROPERTY = "xmpp.oadr.vtnAuthEndpoint";

	private static final String XMPP_SUBDOMAIN = "xmpp";

	private static final Logger Log = LoggerFactory.getLogger(OpenfireOadrPlugin.class);

	private OpenfireOadrSessionListener openfireOadrSessionListener;
	private OpenfireOadrComponent component;
	private InterceptorManager interceptorManager;
	private OpenfireOadrPacketInterceptor packetInterceptor;
	private OadrManager oadrManager = new OadrManager();

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		XMPPServer server = XMPPServer.getInstance();

		String xmppDomain = server.getServerInfo().getXMPPDomain();
		IdentityStore identityStore = server.getCertificateStoreManager().getIdentityStore(ConnectionType.SOCKET_C2S);
		TrustStore trustStore = server.getCertificateStoreManager().getTrustStore(ConnectionType.SOCKET_C2S);

		// init session listener

		KeyStore ks = identityStore.getStore();
		KeyStore ts = trustStore.getStore();
		try {
			SSLContext ctx = SSLContext.getInstance("TLSv1.2");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, identityStore.getConfiguration().getPassword());
			tmf.init(ts);
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			openfireOadrSessionListener = new OpenfireOadrSessionListener(ctx.getSocketFactory(), oadrManager);
		} catch (NoSuchAlgorithmException e) {
			Log.error(e.getMessage());
		} catch (UnrecoverableKeyException e) {
			Log.error(e.getMessage());
		} catch (KeyStoreException e) {
			Log.error(e.getMessage());
		} catch (KeyManagementException e) {
			Log.error(e.getMessage());
		}

		if (openfireOadrSessionListener != null) {
			Log.info("SessionListener loaded");
			SessionEventDispatcher.addListener(openfireOadrSessionListener);
		} else {
			Log.error("SessionListener can't be loaded");
		}

		ComponentManager componentManager = ComponentManagerFactory.getComponentManager();
		try {
			component = new OpenfireOadrComponent(oadrManager, XMPP_SUBDOMAIN + "." + xmppDomain);
			componentManager.addComponent(XMPP_SUBDOMAIN, component);
		} catch (ComponentException e) {
			Log.error(e.getMessage());
		}

		OpenfireOadrAuthProvider oadrAuthProvider = new OpenfireOadrAuthProvider();
		JiveGlobals.setProperty("provider.auth.className", HybridAuthProvider.class.getName());
		JiveGlobals.setProperty("hybridAuthProvider.primaryProvider.className", DefaultAuthProvider.class.getName());
		JiveGlobals.setProperty("hybridAuthProvider.secondaryProvider.className",
				oadrAuthProvider.getClass().getName());

		String fullXmppDomain = XMPP_SUBDOMAIN + "." + xmppDomain;
		interceptorManager = InterceptorManager.getInstance();
		packetInterceptor = new OpenfireOadrPacketInterceptor(oadrManager, fullXmppDomain);
		interceptorManager.addInterceptor(packetInterceptor);

	}

	@Override
	public void destroyPlugin() {
		SessionEventDispatcher.removeListener(openfireOadrSessionListener);
		ComponentManager componentManager = ComponentManagerFactory.getComponentManager();
		try {
			componentManager.removeComponent(XMPP_SUBDOMAIN);
		} catch (ComponentException e) {
			Log.error(e.getMessage());
		}
		interceptorManager.removeInterceptor(packetInterceptor);

		packetInterceptor = null;
		openfireOadrSessionListener = null;
		component = null;
	}

}
