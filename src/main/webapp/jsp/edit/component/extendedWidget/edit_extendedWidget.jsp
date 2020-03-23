<%@page import="org.javlo.servlet.IVersion"%><%@ taglib
	uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib
	prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

	<fieldset>
		<legend>SCSS</legend>
		<textarea class="form-control text-editor" data-mode="text/x-scss" data-ext="css"  name="css-${compid}" rows="14">${field.css}</textarea>
	</fieldset>

	<fieldset>
		<legend>XHMTL</legend>
		<textarea class="form-control text-editor" data-mode="text/html" data-ext="html"  name="xhtml-${compid}" rows="14">${fn:escapeXml(field.xhtml)}</textarea>
	</fieldset>

<!-- 	<fieldset> -->
<!-- 		<legend>Resources</legend> -->
<%-- 		<input type="file" name="file-${compid}" /> <button type="button">add</button>			 --%>
<!-- 	</fieldset> -->
