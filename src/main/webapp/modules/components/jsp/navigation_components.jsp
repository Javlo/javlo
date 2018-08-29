<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<ul class="navigation">
<c:forEach var="link" items="${globalContext.externComponents}">	
<c:url var="navURL" value="${info.currentURL}" context="/"><c:param name="component" value="${link.name}" /></c:url>
<li ${param.component eq link.name?'class="current"':''}><a href="${navURL}">${link.name}</a></li>
</c:forEach>
<li class="add-component">
		<form id="form-add-component" action="${info.currentURL}" method="post">		
			<div class="row"><div class="col-xs-8">
			<input name="webaction" value="components.addcomponent" type="hidden">
			<input class="form-control" name="component" placeholder="add component..." type="text">
			</div><div class="col-xs-4">
				<input class="btn btn-default btn-xs" value="ok" type="submit">
			</div>			
			</div>
		</form>
	</li>	
</ul>