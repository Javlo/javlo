<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">
<form class="standard-form" id="create-ticket" method="post" action="${info.currentURL}">
<div class="col-container">
<div class="one_half">
		<input type="hidden" name="webaction" value="ticket.update" />
		<input type="hidden" name="id" value="${ticket.id}" />
		<div class="line">
			<label>authors :</label>${ticket.authors}
		</div>
		<div class="line">
			<label for="priority">priority : </label>
			<select id="priority" name="priority">
				<option value="0" ${ticket.priority == 0?'selected="selected"':''}>none</option>
				<option value="1" ${ticket.priority == 1?'selected="selected"':''}>low</option>
				<option value="2" ${ticket.priority == 2?'selected="selected"':''}>middle</option>
				<option value="3" ${ticket.priority == 3?'selected="selected"':''}>high</option>
			</select>
		</div>
		<div class="line">
			<label for="status">status : </label>
			<select id="status" name="status">
				<option ${ticket.status == 'new'?'selected="selected"':''}>new</option>
				<option ${ticket.status == 'working'?'selected="selected"':''}>working</option>
				<option value="onhold" ${ticket.status == 'onhold'?'selected="selected"':''}>on hold</option>
				<option ${ticket.status == 'refuse'?'selected="selected"':''}>refuse</option>
				<option ${ticket.status == 'done'?'selected="selected"':''}>done</option>
				<option ${ticket.status == 'archived'?'selected="selected"':''}>archived</option>
			</select>
		</div>
		<div class="line">
			<label for="share">share : </label>
			<select id="share" name="share" ${ticket.debugNote ? 'disabled="disabled"' : ''}>
				<option value="">none</option>
				<option value="site" ${ticket.share == 'site'?'selected="selected"':''}>${ticket.context}</option>
				<option value="allsites" ${ticket.share == 'allsites'?'selected="selected"':''}>all sites</option>
			    <option value="public" ${ticket.share == '"public"'?'selected="selected"':''}>public</option>
			</select>
		</div>
</div><div class="one_half">			
		<div class="line">
			<label>creation date :</label>${ticket.creationDateLabel}
		</div>
		<div class="line">
			<label>last update date :</label>${ticket.lastUpdateDateLabel}
		</div>
		<div class="line">
			<label>title :</label>${ticket.title}
		</div>
		<div class="line">
			<label>category : </label>${ticket.category}			
		</div>
</div></div>
		<fieldset>
			<legend>user</legend>
			<c:forEach var="user" items="${ticketUsers}">
				<label class="checkbox-inline">
					<input type="checkbox" name="users" value="${user.login}" ${fn:contains(ticket.users,user.login)?'checked="checked"':''} /> ${user.login}
				</label>
			</c:forEach>
		</fieldset>
		<div class="frame">
		
		<c:if test="${not empty ticket.url}">	
		
		<div class="line">
			<label>url : </label><a href="${ticket.url}">${ticket.url}</a>			
		</div>
		</c:if>
					
		<div class="line">
			<label>message</label>
			<div class="message">${ticket.message}</div>			
		</div>
		
		</div>
		
	<h2>comments</h2>
	<c:if test="${fn:length(ticket.comments) > 0}">
	<c:forEach var="comment" items="${ticket.comments}">
	<fieldset>
		<legend>${comment.authors}</legend>
		${comment.message}
	</fieldset>
	</c:forEach>
	</c:if>		
	
	<div class="line">
		<label for="comment">new comments</label>
		<textarea id="comment" name="comment"></textarea>
	</div>
	
	<div class="action">
		<input type="submit" name="delete" class="warning needconfirm" title="${i18n.edit['global.delete']}" value="${i18n.edit['global.delete']}" />
		<input type="submit" name="back" title="${i18n.edit['global.back']}" value="${i18n.edit['global.back']}" />
		<input type="submit" name="ok" value="${i18n.edit['global.ok']}" />		
	</div>
	
</form>

</div>