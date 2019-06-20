package com.avob.server.openfire;

import org.jivesoftware.openfire.auth.AuthProvider;
import org.jivesoftware.openfire.auth.ConnectionException;
import org.jivesoftware.openfire.auth.InternalUnauthenticatedException;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenfireOadrAuthProvider implements AuthProvider {

	private static final Logger Log = LoggerFactory.getLogger(OpenfireOadrAuthProvider.class);

	private static final String EVENT_SERVICE = "EiEvent";

	private static final String REPORT_SERVICE = "EiReport";

	private static final String REGISTERPARTY_SERVICE = "EiRegisterParty";

	private static final String UPLINK_SERVICE = "uplink";

	@Override
	public void authenticate(String username, String password)
			throws UnauthorizedException, ConnectionException, InternalUnauthenticatedException {

		if (!EVENT_SERVICE.equals(username) && !REPORT_SERVICE.equals(username)
				&& !REGISTERPARTY_SERVICE.equals(username) && !UPLINK_SERVICE.equals(username)) {
			Log.info("Unauthorized user: " + username);
			throw new UnauthorizedException();
		}

		Log.info("Authorized user: " + username);
		return;

	}

	@Override
	public String getPassword(String username) throws UserNotFoundException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPassword(String username, String password)
			throws UserNotFoundException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean supportsPasswordRetrieval() {
		return false;
	}

	@Override
	public boolean isScramSupported() {
		return false;
	}

	@Override
	public String getSalt(String username) throws UnsupportedOperationException, UserNotFoundException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getIterations(String username) throws UnsupportedOperationException, UserNotFoundException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getServerKey(String username) throws UnsupportedOperationException, UserNotFoundException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getStoredKey(String username) throws UnsupportedOperationException, UserNotFoundException {
		throw new UnsupportedOperationException();
	}

}
