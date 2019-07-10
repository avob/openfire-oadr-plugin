package com.avob.server.openfire;

public class OadrManager {

	private String reportJid;

	private String eventJid;

	private String registerPartyJid;

	private String optJid;

	private String uplinkJid;

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

	public String getOptJid() {
		return optJid;
	}

	public void setOptJid(String optJid) {
		this.optJid = optJid;
	}

	public boolean isVtnConnected() {
		return this.getEventJid() != null && this.getReportJid() != null && this.getRegisterPartyJid() != null
				&& this.getUplinkJid() != null && this.getOptJid() != null;
	}
}
