<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:forEach var="message" items="${aimMessages}">
	<li style="color: ${aimUsersBySite[message.fromSite][message.fromUser].color};">
		<div class="user" data-user="${message.fromSite}::${message.fromUser}">
			${message.fromSite} - ${message.fromUser} : 
		</div>
		<div class="message">
			<span class="to" data-user="${message.receiverSite}::${message.receiverUser}">
				${message.receiverSite} - ${message.receiverUser}
			</span>
			<span>
				${message.message}
			</span>
		</div>
</c:forEach>
<li id="aim-next-messages">
	<form class="ajax" action="${info.currentURL}" method="post">
		<input type="hidden" name="lastMessageId" value="${aimLastMessageId}" />
		<input class="ajax" type="submit" value="Check next messages" />
	</form>
	<script type="text/javascript">
	jQuery("#aim-next-messages [type=submit]").hide();
	</script>
</li>
