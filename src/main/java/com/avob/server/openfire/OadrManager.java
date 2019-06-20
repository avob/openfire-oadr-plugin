package com.avob.server.openfire;

import java.util.HashMap;
import java.util.Map;

public class OadrManager {

	private String reportJid;

	private String eventJid;

	private String registerPartyJid;

	private String uplinkJid;

	private Map<String, String> venJidToFingerprint = new HashMap<>();

	public String getReportJid() {
		return reportJid;
	}

	public void setReportJid(String reportJid) {
		this.reportJid = reportJid;
	}

	public String getEventJid() {
		return eventJid;
	}

	public void setEventJid(String eventJid) {
		this.eventJid = eventJid;
	}

	public String getRegisterPartyJid() {
		return registerPartyJid;
	}

	public void setRegisterPartyJid(String registerPartyJid) {
		this.registerPartyJid = registerPartyJid;
	}

	public String getUplinkJid() {
		return uplinkJid;
	}

	public void setUplinkJid(String uplinkJid) {
		this.uplinkJid = uplinkJid;
	}

	public void addVen(String jid, String fingerprint) {
		venJidToFingerprint.put(jid, fingerprint);
	}

	public void removeVen(String jid) {
		venJidToFingerprint.remove(jid);
	}

	public String getVenFingerprint(String jid) {
		return venJidToFingerprint.get(jid);
	}

}
