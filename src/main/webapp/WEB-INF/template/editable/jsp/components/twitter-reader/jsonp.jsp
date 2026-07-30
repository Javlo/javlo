<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><%
/*
 * The callback name is written straight into javascript, so escaping it would
 * be meaningless : only a plain identifier is accepted, anything else falls
 * back on a fixed name.
 */
String callbackParam = request.getParameter("callback");
if (callbackParam == null || !callbackParam.matches("[A-Za-z_$][A-Za-z0-9_$.]{0,63}")) {
	callbackParam = "callback";
}
request.setAttribute("safeCallback", callbackParam);
%>${safeCallback}(${json});
