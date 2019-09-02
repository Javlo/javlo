<%@page import="org.javlo.servlet.IVersion"%><%@ taglib
	uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib
	prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
	
	
	<div class="row">
	
	<div class="col-md-6">
		<fieldset>
			<legend>XHMTL</legend>
			<textarea class="form-control" name="xhtml-${compid}" rows="14">${field.xhtml}</textarea>
		</fieldset>
	</div>
	<div class="col-md-6">
		<fieldset>
			<legend>SCSS</legend>
			<textarea class="form-control" name="css-${compid}" rows="14">${field.css}</textarea>
		</fieldset>
	</div>
	
	</div>
	
<!-- 	<fieldset> -->
<!-- 		<legend>Resources</legend> -->
<%-- 		<input type="file" name="file-${compid}" /> <button type="button">add</button>			 --%>
<!-- 	</fieldset> -->
