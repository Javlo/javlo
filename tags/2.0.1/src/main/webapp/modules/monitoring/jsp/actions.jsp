<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="log-js-only">
	<div class="special last">
		<div class="form_default">
			<input id="log-filter" type="text" placeholder="${i18n.edit['monitoring.log.filter']}..." />
		</div>
	</div>
	<a id="log-auto-refresh" class="action-button play-pause active" href="#">
		<span>${i18n.edit['monitoring.log.auto-refresh']}</span>
	</a>
	<a id="log-refresh" class="action-button refresh" href="#">
		<span>${i18n.edit['monitoring.log.refresh']}</span>
	</a>
</div>
<div class="clear">&nbsp;</div>

