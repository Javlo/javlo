<%@page import="java.util.Locale"%>
<%@page import="org.javlo.context.GlobalContext"%>
<%@page import="org.javlo.service.RequestService"%>
<%@page import="java.util.Enumeration"%>
<%@page contentType="text/html"%>
<html>
	<head>
		<style type="text/css">
			table {
				width: 100%
			}
			th {
				text-align: left;
			}
		</style>
	</head>
</html>
<body>
<h1>Languages</h1>
<table>
	<tr>
		<th>lang</th>
		<th>ISO3</th>
		<th>display lang</th>
		<th>display en</th>
		<th>display fr</th>
		<th>display de</th>
	</tr>
<%for (String lg : GlobalContext.getInstance(request).getContentLanguages()) {
	Locale locale = new Locale(lg);
%>
	<tr>
		<td><%=lg%></td>
		<td><%=locale.getISO3Language()%></td>
		<td><%=locale.getDisplayLanguage()%></td>
		<td><%=locale.getDisplayLanguage(new Locale("en"))%></td>
		<td><%=locale.getDisplayLanguage(new Locale("fr"))%></td>
		<td><%=locale.getDisplayLanguage(new Locale("de"))%></td>
	</tr>
<%}%>
</table>
</body>