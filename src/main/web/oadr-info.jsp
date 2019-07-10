
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="org.jivesoftware.util.JiveGlobals"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>


<html>
<head>
<title><fmt:message key="oadr.info.title" /></title>
<meta name="pageID" content="oadr-info" />
</head>
<body>

	<style type="text/css">
.bar TD {
	padding: 0;
}

#jive-latest-activity .jive-bottom-line {
	padding-top: 10px;
	border-bottom: 1px #e8a400 solid;
}

#jive-latest-activity {
	border: 1px #E8A400 solid;
	background-color: #FFFBE2;
	font-family: Lucida Grande, Arial, Helvetica, sans-serif;
	font-size: 9pt;
	padding: 0 10px 10px 10px;
	margin-bottom: 10px;
	min-height: 280px;
	-moz-border-radius: 4px;
	width: 95%;
	margin-right: 20px;
}

#jive-latest-activity h4 {
	font-size: 13pt;
	margin: 15px 0 4px 0;
}

#jive-latest-activity h5 {
	font-size: 9pt;
	font-weight: normal;
	margin: 15px 0 5px 5px;
	padding: 0;
}

#jive-latest-activity .jive-blog-date {
	font-size: 8pt;
	white-space: nowrap;
}

#jive-latest-activity .jive-feed-icon {
	float: right;
	padding-top: 10px;
}

.info-header {
	background-color: #eee;
	font-size: 10pt;
}

.info-table {
	margin-right: 12px;
}

.info-table .c1 {
	text-align: right;
	vertical-align: top;
	color: #666;
	font-weight: bold;
	font-size: 9pt;
	white-space: nowrap;
}

.info-table .c2 {
	font-size: 9pt;
	width: 90%;
}
</style>

<%@ page import="org.jivesoftware.openfire.XMPPServer" %>
<%@ page import="com.avob.server.openfire.OpenfireOadrPlugin" %>
com.avob.server.openfire<% // Get parameters //

    // Network interface (if any) is configured for all ports on the server
    OpenfireOadrPlugin openfireOadrPlugin =
            (OpenfireOadrPlugin) XMPPServer.getInstance().getPluginManager().getPlugin("OpenfireOadrPlugin");



    boolean rssEnabled = JiveGlobals.getBooleanProperty("rss.enabled", true);
%>
	<p>
		<fmt:message key="oadr.info.message" />
	</p>
	<table border="0" width="100%">
		<td valign="top">

			<table border="0" cellpadding="2" cellspacing="2" width="100%"
				class="info-table">
				<thead>
					<tr>
						<th colspan="2" align="left" class="info-header"><fmt:message
								key="oadr.info.properties" /></th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td class="c1"><fmt:message key="oadr.info.vtnId" /></td>
						<td class="c2">
						<% final String vtnId = JiveGlobals.getProperty("xmpp.oadr.vtnId"); %>
						<% if(vtnId == null) { %>
						<fmt:message key="oadr.info.vtnIDSystemPropertyRequired" />
						<img src="images/error-16x16.gif" width="12" height="12" border="0" alt="<fmt:message key="oadr.info.vtnIDSystemPropertyRequired" />" title="<fmt:message key="oadr.info.vtnIDSystemPropertyRequired" />">&nbsp;
						<% } else { %>
							<%= vtnId %>
						<% } %>

						</td>
					</tr>
					<tr>
						<td class="c1"><fmt:message key="oadr.info.vtnAuthEndpoint" /></td>
						<td class="c2">
						<% final String vtnAuthEndpoint = JiveGlobals.getProperty("xmpp.oadr.vtnAuthEndpoint"); %>
						<% if(vtnAuthEndpoint == null) { %>
						<fmt:message key="oadr.info.vtnAuthEndpointSystemPropertyRequired" />
						<img src="images/error-16x16.gif" width="12" height="12" border="0" alt="<fmt:message key="vtnAuthEndpointSystemPropertyRequired" />" title="<fmt:message key="vtnAuthEndpointSystemPropertyRequired" />">&nbsp;
						<% } else { %>
							<%= vtnAuthEndpoint %>
						<% } %>

						</td>
					</tr>
					<tr>
						<td class="c1"><fmt:message key="oadr.info.vtnClientState" /></td>
						<td class="c2">
						<% boolean vtnClientConnected = openfireOadrPlugin.getOadrManager().isVtnConnected(); %>
						<%= vtnClientConnected %>
						</td>
					</tr>
					<tr>
						<td>&nbsp;</td>
					</tr>
				</tbody>


			</table>
		</td>

	</table>
</html>