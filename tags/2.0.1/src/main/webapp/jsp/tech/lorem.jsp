<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@page import="org.javlo.helper.LoremIpsumGenerator"
%><%@page contentType="text/html"
%><c:if test="${not empty param.form}"><%=LoremIpsumGenerator.getParagraph(Integer.parseInt(request.getParameter("words")), request.getParameter("start") != null, request.getParameter("ponctuation") != null)%></c:if><c:if test="${empty param.form}">
<html>
<head>
	<title>Javlo - Lorem ipsum Generator.</title>
</head>
<body>
<form>
<fieldset>
<legend>config lorem ipsum generator</legend>
	<div class="line">
		<label for="words">word count</label>
		<input type="text" id="words" name="words" value="${param.words}" />
	</div>
	<div class="line">
		<input type="checkbox" id="start" name="start" ${not empty param.start?'checked="checked"':''} />
		<label for="start">start with lorem...</label>
	</div>
	<div class="line">
		<input type="checkbox" id="ponctuation" name="ponctuation" ${not empty param.ponctuation?'checked="checked"':''} />
		<label for="ponctuation">ponctuation</label>		
	</div>
	<div class="line">
		<input type="checkbox" id="form" name="form" />
		<label for="form">text only</label>		
	</div>
	<div class="action">
		<input type="submit" />
	</div>
</fieldset>
</form>
<c:if test="${not empty param.words}">
<p><%=LoremIpsumGenerator.getParagraph(Integer.parseInt(request.getParameter("words")), request.getParameter("start") != null, request.getParameter("ponctuation") != null)%></p>
</c:if>
</body>
</html>
</c:if>