<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content" id="cim-content">
	<ul class="cim-messages">
		<jsp:include page="cim_messages.jsp" />
	</ul>
	<form class="cim-form ajax" action="${info.currentURL}" method="post">
		<input type="hidden" name="webaction" value="SendIM" />
		<input type="hidden" name="lastMessageId" value="${comm.lastMessageId}" />
		<select name="receiver">
			<c:if test="${not comm.allSites}">
				<c:set var="site" value="${comm.sitesByKey[comm.currentSite]}" />

				<option value="${site.key}::_ALL">All users</option>
				<c:forEach var="user" items="${site.users}">
					<c:if test="${user.username != comm.currentUser}">
						<option style="color: ${user.color};" value="${site.key}::${user.username}">${user.username}</option>
					</c:if>
				</c:forEach>

			</c:if>
			<c:if test="${comm.allSites}">
				<option value="_ALL::_ALL">${i18n.edit['communication.all-sites']}</option>
				<c:forEach var="site" items="${comm.sites}">
					<c:if test="${!empty site.users}">
						<option value="${site.key}::_ALL" class="cim-site">${site.label}</option>
						<c:forEach var="user" items="${site.users}">
							<c:if test="${user.username != comm.currentUser}">
								<option style="color: ${user.color};" value="${site.key}::${user.username}" class="cim-site-user">${user.username}</option>
							</c:if>
						</c:forEach>
					</c:if>
				</c:forEach>
			</c:if>
		</select>
		<input class="label-inside label" name="message" type="text" value="Message" />
		<input type="submit" value="Send" />
	</form>
</div>
