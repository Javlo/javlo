<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div class="content">
	<form class="ajax standard-form" action="${info.currentURL}" method="post">
		<div>
			<input type="hidden" name="webaction" value="wizard" />
			<input type="hidden" name="box" value="sendwizard" />
		</div>
		<div>
			${confirmMessage}
		</div>
		<ul>
			<c:forEach var="email" items="${mailing.allRecipients}">
				<li><c:out value="${email}" escapeXml="true" /></li>
			</c:forEach>
		</ul>
		<div>
			<input type="submit" name="previous" value="Previous" />
			<input type="submit" name="send" value="Send" />
		</div>
	</form>
</div>
