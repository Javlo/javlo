<%@page import="java.util.Enumeration"%>
<%@page contentType="text/html"%>

<h1>HttpServletRequest return of principal methods</h1>

<code>request.getAuthType()</code> : <%=request.getAuthType()%><br /><br />

<code>request.getContextPath() </code> : <%=request.getContextPath()%><br /><br />

<code>request.getPathInfo()</code> : <%=request.getPathInfo()%><br /><br />

<code>request.getPathTranslated() </code> : <%=request.getPathTranslated() %><br /><br />

<code>request.getQueryString() </code> : <%=request.getQueryString() %><br /><br />

<code>request.getRequestURI()  </code> : <%=request.getRequestURI()  %><br /><br />

<code>request.getRequestURL()  </code> : <%=request.getRequestURL()  %><br /><br />

<code>request.getServletPath()  </code> : <%=request.getServletPath()  %><br /><br />

<code>request.getRemoteHost()  </code> : <%=request.getRemoteHost()  %><br /><br />

<code>request.getRemoteAddr()  </code> : <%=request.getRemoteAddr() %><br /><br />

<code>request.getServerName()  </code> : <%=request.getServerName()  %><br /><br />

<code>request.getServerPort()  </code> : <%=request.getServerPort()  %><br /><br />

<code>request.getProtocol()  </code> : <%=request.getProtocol()  %><br /><br />

<code>request.getLocalName()  </code> : <%=request.getLocalName() %><br /><br />

<h2>Header :</h2>

<ul>
<%
Enumeration headers = request.getHeaderNames();
while (headers.hasMoreElements()) {
	String headerKey = ""+headers.nextElement();
	%><li><%=headerKey%> : <%=((Enumeration)request.getHeaders(headerKey)).nextElement()%></li>	
<%}%>
</ul>
