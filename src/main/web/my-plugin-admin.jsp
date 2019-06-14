<%--
<%--
  -
  - Copyright (C) 2004-2008 Jive Software. All rights reserved.
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
--%>

<%@ page import="org.jivesoftware.admin.AdminConsole"%>
<%@ page import="org.jivesoftware.openfire.Connection"%>
<%@ page import="org.jivesoftware.openfire.FlashCrossDomainHandler" %>
<%@ page import="org.jivesoftware.openfire.JMXManager" %>
<%@ page import="org.jivesoftware.openfire.XMPPServer" %>
<%@ page import="org.jivesoftware.openfire.container.AdminConsolePlugin" %>
<%@ page import="org.jivesoftware.openfire.filetransfer.proxy.FileTransferProxy" %>
<%@ page import="org.jivesoftware.openfire.http.HttpBindManager" %>
<%@ page import="org.jivesoftware.openfire.keystore.IdentityStore" %>
<%@ page import="org.jivesoftware.openfire.mediaproxy.MediaProxyService" %>
<%@ page import="org.jivesoftware.openfire.spi.ConnectionListener" %>
<%@ page import="org.jivesoftware.openfire.spi.ConnectionManagerImpl" %>
<%@ page import="org.jivesoftware.openfire.spi.ConnectionType" %>
<%@ page import="org.jivesoftware.openfire.update.Update" %>
<%@ page import="org.jivesoftware.openfire.update.UpdateManager" %>
<%@ page import="org.jivesoftware.util.HttpClientWithTimeoutFeedFetcher" %>
<%@ page import="org.jivesoftware.util.JiveGlobals" %>
<%@ page import="org.jivesoftware.util.LocaleUtils" %>
<%@ page import="org.jivesoftware.util.StringUtils" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ page import="org.jivesoftware.openfire.net.DNSUtil" %>
<%@ page import="org.xmpp.packet.JID" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%-- Define page bean for header and sidebar --%>
<jsp:useBean id="pageinfo" scope="request" class="org.jivesoftware.admin.AdminPageBean" />

<%  // Simple logout code
    if ("true".equals(request.getParameter("logout"))) {
        session.removeAttribute("jive.admin.authToken");
        response.sendRedirect("index.jsp");
        return;
    }
%>

<%-- Define Administration Bean --%>
<jsp:useBean id="webManager" class="org.jivesoftware.util.WebManager"  />
<% webManager.init(request, response, session, application, out); %>


<% // Get parameters //
    boolean serverOn = (webManager.getXMPPServer() != null);

    ConnectionManagerImpl connectionManager = ((ConnectionManagerImpl) XMPPServer.getInstance().getConnectionManager());

    // Network interface (if any) is configured for all ports on the server
    AdminConsolePlugin adminConsolePlugin =
            (AdminConsolePlugin) XMPPServer.getInstance().getPluginManager().getPlugin("admin");

    FileTransferProxy fileTransferProxy = XMPPServer.getInstance().getFileTransferProxy();
    HttpBindManager httpBindManager = HttpBindManager.getInstance();
    MediaProxyService mediaProxyService = XMPPServer.getInstance().getMediaProxyService();
    FlashCrossDomainHandler flashCrossDomainHandler = XMPPServer.getInstance().getFlashCrossDomainHandler();

    boolean rssEnabled = JiveGlobals.getBooleanProperty("rss.enabled", true);
%>

<html>
    <head>
        <title><fmt:message key="index.title"/></title>
        <meta name="pageID" content="server-settings"/>
        <meta name="helpPage" content="about_the_server.html"/>
    </head>
    <body>

<%
    UpdateManager updateManager = XMPPServer.getInstance().getUpdateManager();
    Update serverUpdate = updateManager.getServerUpdate();
    pageContext.setAttribute( "serverUpdate", serverUpdate ); %>


<style type="text/css">
.bar TD {
    padding : 0;
}
#jive-latest-activity .jive-bottom-line {
    padding-top: 10px;
    border-bottom : 1px #e8a400 solid;
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

<p>
<fmt:message key="index.title.info" />
</p>
<table border="0" width="100%">
    <td valign="top">

        <!-- <div class="jive-table"> -->
        <table border="0" cellpadding="2" cellspacing="2" width="100%" class="info-table">
        <thead>
            <tr>
                <th colspan="2" align="left" class="info-header"><fmt:message key="index.properties" /></th>
            </tr>
        </thead>
        <tbody>

            <%  if (serverOn) { %>

                 <tr>
                    <td class="c1"><fmt:message key="index.uptime" /></td>
                    <td class="c2">
                        <%
                            long now = System.currentTimeMillis();
                            long lastStarted = webManager.getXMPPServer().getServerInfo().getLastStarted().getTime();
                            long uptime = now - lastStarted;
                            String uptimeDisplay = StringUtils.getElapsedTime(uptime);
                        %>

                        <%  if (uptimeDisplay != null) { %>
                            <%= uptimeDisplay %> -- started
                        <%  } %>

                        <%= JiveGlobals.formatDateTime(webManager.getXMPPServer().getServerInfo().getLastStarted()) %>
                    </td>
                </tr>

            <%  } %>

            <tr>
                <td class="c1"><fmt:message key="index.version" /></td>
                <td class="c2">
                    <%= AdminConsole.getAppName() %>
                    <%= AdminConsole.getVersionString() %>
                </td>
            </tr>
            <tr>
                <td class="c1"><fmt:message key="index.home" /></td>
                <td class="c2">
                    <%= JiveGlobals.getHomeDirectory() %>
                </td>
            </tr>
            <tr>
                <td class="c1">
                    <fmt:message key="index.server_name" />
                </td>
                <td class="c2">
                    <% final IdentityStore identityStore = XMPPServer.getInstance().getCertificateStoreManager().getIdentityStore( ConnectionType.SOCKET_C2S ); %>
                    <% try { %>
                    <% if (!identityStore.containsDomainCertificate( "RSA" )) {%>
                    <img src="images/warning-16x16.gif" width="12" height="12" border="0" alt="<fmt:message key="index.certificate-warning" />" title="<fmt:message key="index.certificate-warning" />">&nbsp;
                    <% } %>
                    <% } catch (Exception e) { %>
                    <img src="images/error-16x16.gif" width="12" height="12" border="0" alt="<fmt:message key="index.certificate-error" />" title="<fmt:message key="index.certificate-error" />">&nbsp;
                    <% } %>
                    <c:out value="${webManager.serverInfo.XMPPDomain}"/>
                    <% try { String whatevs = JID.domainprep(webManager.getXMPPServer().getServerInfo().getXMPPDomain()); } catch (Exception e) { %>
                    <img src="images/error-16x16.gif" width="12" height="12" border="0" alt="<fmt:message key="index.domain-stringprep-error" />" title="<fmt:message key="index.domain-stringprep-error" />">&nbsp;
                    <% } %>
                </td>
            </tr>
            <tr><td>&nbsp;</td></tr>
        </tbody>
        <thead>
            <tr>
                <th colspan="2" align="left" class="info-header"><fmt:message key="index.environment" /></th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td class="c1"><fmt:message key="index.jvm" /></td>
                <td class="c2">
                    <%
                        String vmName = System.getProperty("java.vm.name");
                        if (vmName == null) {
                            vmName = "";
                        }
                        else {
                            vmName = " -- " + vmName;
                        }
                    %>
                    <%= System.getProperty("java.version") %> <%= System.getProperty("java.vendor") %><%= vmName %>
                </td>
            </tr>
            <tr>
                <td class="c1"><fmt:message key="index.app" /></td>
                <td class="c2">
                    <%= application.getServerInfo() %>
                </td>
            </tr>
            <tr>
                <td class="c1">
                    <fmt:message key="index.host_name" />
                </td>
                <td class="c2">
                    <c:out value="${webManager.serverInfo.hostname}"/>
                    <% try { String whatevs = JID.domainprep(webManager.getXMPPServer().getServerInfo().getHostname()); } catch (Exception e) { %>
                    <img src="images/error-16x16.gif" width="12" height="12" border="0" alt="<fmt:message key="index.hostname-stringprep-error" />" title="<fmt:message key="index.hostname-stringprep-error" />">&nbsp;
                    <% } %>
                    <%  // Determine if the DNS configuration for this XMPP domain needs to be evaluated.
                        final String xmppDomain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
                        final String hostname = XMPPServer.getInstance().getServerInfo().getHostname();
                        boolean dnsIssue = false;
                        if ( !xmppDomain.equalsIgnoreCase( hostname ) )
                        {
                            dnsIssue = true;
                            final List<DNSUtil.WeightedHostAddress> dnsSrvRecords = DNSUtil.srvLookup( "xmpp-client", "tcp", xmppDomain );
                            for ( final DNSUtil.WeightedHostAddress dnsSrvRecord : dnsSrvRecords )
                            {
                                if ( hostname.equalsIgnoreCase( dnsSrvRecord.getHost() ) )
                                {
                                    dnsIssue = false;
                                    break;
                                }
                            }
                        }
                        if ( dnsIssue ) {
                        %>
                        <img src="images/warning-16x16.gif" width="12" height="12" border="0">
                            <fmt:message key="index.dns-warning">
                                <fmt:param><a href='dns-check.jsp'></fmt:param>
                                <fmt:param></a></fmt:param>
                            </fmt:message>
                        <%
                        }
                    %>
                </td>
            </tr>
            <tr>
                <td class="c1"><fmt:message key="index.os" /></td>
                <td class="c2">
                    <%= System.getProperty("os.name") %> / <%= System.getProperty("os.arch") %>
                </td>
            </tr>
            <tr>
                <td class="c1"><fmt:message key="index.local" /></td>
                <td class="c2">
                    <%= JiveGlobals.getLocale() %> / <%= JiveGlobals.getTimeZone().getDisplayName(JiveGlobals.getLocale()) %>
                    (<%= (JiveGlobals.getTimeZone().getRawOffset()/1000/60/60) %> GMT)
                </td>
            </tr>
            <tr>
                <td class="c1"><fmt:message key="index.process_owner" /></td>
                <td class="c2"><%= System.getProperty("user.name") %></td>
            </tr>
            <tr>
                <td class="c1"><fmt:message key="index.memory" /></td>
                <td>
                <%    // The java runtime
                    Runtime runtime = Runtime.getRuntime();

                    double freeMemory = (double)runtime.freeMemory()/(1024*1024);
                    double maxMemory = (double)runtime.maxMemory()/(1024*1024);
                    double totalMemory = (double)runtime.totalMemory()/(1024*1024);
                    double usedMemory = totalMemory - freeMemory;
                    double percentFree = ((maxMemory - usedMemory)/maxMemory)*100.0;
                    double percentUsed = 100 - percentFree;
                    int percent = 100-(int)Math.round(percentFree);

                    DecimalFormat mbFormat = new DecimalFormat("#0.00");
                    DecimalFormat percentFormat = new DecimalFormat("#0.0");
                %>

                <table cellpadding="0" cellspacing="0" border="0" width="300">
                <tr valign="middle">
                    <td width="99%" valign="middle">
                        <div class="bar">
                        <table cellpadding="0" cellspacing="0" border="0" width="100%" style="border:1px #666 solid;">
                        <tr>
                            <%  if (percent == 0) { %>

                                <td width="100%"><img src="images/percent-bar-left.gif" width="100%" height="8" border="0" alt=""></td>

                            <%  } else { %>

                                <%  if (percent >= 90) { %>

                                    <td width="<%= percent %>%" background="images/percent-bar-used-high.gif"
                                        ><img src="images/blank.gif" width="1" height="8" border="0" alt=""></td>

                                <%  } else { %>

                                    <td width="<%= percent %>%" background="images/percent-bar-used-low.gif"
                                        ><img src="images/blank.gif" width="1" height="8" border="0" alt=""></td>

                                <%  } %>
                                <td width="<%= (100-percent) %>%" background="images/percent-bar-left.gif"
                                    ><img src="images/blank.gif" width="1" height="8" border="0" alt=""></td>
                            <%  } %>
                        </tr>
                        </table>
                        </div>
                    </td>
                    <td width="1%" nowrap>
                        <div style="padding-left:6px;" class="c2">
                        <%= mbFormat.format(usedMemory) %> MB of <%= mbFormat.format(maxMemory) %> MB (<%= percentFormat.format(percentUsed) %>%) used
                        </div>
                    </td>
                </tr>
                </table>
                </td>
            </tr>
        </tbody>
        </table>
        <!-- </div> -->
    </td>
  
</table>


</html>