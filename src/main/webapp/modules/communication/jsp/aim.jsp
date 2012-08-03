<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content" id="aim-content">
	<ul class="aim-messages">
		<jsp:include page="aim_messages.jsp" />
	</ul>
	<form class="aim-form ajax" action="${info.currentURL}" method="post">
		<input type="hidden" name="webaction" value="SendAIM" />
		<input type="hidden" name="lastMessageId" value="${aimLastMessageId}" />
		<select name="receiver">
			<option value="_ALL::_ALL">All sites</option>
			<c:forEach var="site" items="${sites}">
				<optgroup label="${site}">
					<option value="${site}::_ALL">All users of ${site}</option>
					<c:forEach var="entry" items="${aimUsersBySite[site]}">
						<c:if test="${entry.key != aimCurrentUser}">
							<option style="color: ${entry.value.color};" value="${site}::${entry.value.username}">${entry.value.username}</option>
						</c:if>
					</c:forEach>
				</optgroup>
			</c:forEach>
		</select>
		<input class="label-inside label" name="message" type="text" value="Message" />
		<input type="submit" value="Send" />
	</form>
</div>
