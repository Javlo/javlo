<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:forEach var="message" items="${comm.messages}">
	<li style="color: ${comm.sitesByKey[message.fromSite].usersByName[message.fromUser].color};">
		<span class="user" data-user="${message.fromSite}::${message.fromUser}">
			${message.fromUser}</span><c:if 
				test="${comm.allSites and not message.fromAllSites}"
					><span class="user site" data-user="${message.fromSite}::_ALL"
					>@${message.fromSite}</span></c:if>
		<span class="message">
			<c:if test="${message.toAllSites}">
				<span class="to announce">
					(${i18n.edit['communication.announce']})
				</span>
			</c:if>
			<c:if test="${not message.toAllUsers}">
				<span class="to" data-user="${message.receiverSite}::${message.receiverUser}">
					&gt; ${message.receiverUser}
				</span>
			</c:if> :
			<span>${message.message}</span>
		</span>
</c:forEach>
<li id="cim-next-messages">
	<form class="ajax" action="${info.currentURL}" method="post">
		<input type="hidden" name="lastMessageId" value="${comm.lastMessageId}" />
		<input class="ajax" type="submit" value="Check next messages" />
	</form>
	<script type="text/javascript">
	jQuery("#cim-next-messages [type=submit]").hide();
	</script>
</li>
