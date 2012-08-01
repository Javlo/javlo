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
			<option value="">-Please choose-</option>
			<option value="_ALL::_ALL">All sites</option>
			<c:forEach var="site" items="${contextList}">
				<c:if test="${empty site.aliasOf}">
					<optgroup label="${site.key}">
						<option value="${site.key}::_ALL">All users of ${site.key}</option>
						<c:forEach var="entry" items="${aimUsersBySite[site.key]}">
							<c:if test="${entry.key != aimCurrentUser}">
								<option style="color: ${entry.value.color};" value="${site.key}::${entry.value.username}">${entry.value.username}</option>
							</c:if>
						</c:forEach>
					</optgroup>
				</c:if>
			</c:forEach>
		</select>
		<input name="message" type="text" placeholder="Message" />
		<input type="submit" value="Send" />
	</form>
</div>
