<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div class="content">
<form class="standard-form" id="create-ticket" method="post" action="${info.currentURL}">
<div class="row">	
	<div class="col-md-2">		
		<div id="screenshot">
			<c:if test="${not empty newTicket}"><c:url var="screenshot" value="/web-tmp/${globalContext.contextKey}_${info.page.name}.jpg" ></c:url></c:if>
			<c:if test="${empty newTicket && not empty ticket.screenshot}"><c:set var="screenshot" value="${ticket.screenshot}" /></c:if>
			<c:if test="${empty screenshot}">no screenshot</c:if>
			<c:if test="${not empty screenshot}"><a href="${screenshot}" target="_blank"><img id="screenshot-container" src="${info.ajaxLoaderURL}" alt="page screenshot" lang="en" /></a></c:if>
			<script>setTimeout(function () {
           		jQuery("#screenshot-container").attr("src", '${screenshot}');
    		}, 3000);</script>
		</div>
	</div>
	<div class="col-md-5">
		<c:if test="${not empty newTicket}">
			<div class="form-group"><input class="form-control" type="text" name="title" placeholder="title" /></div>
		</c:if><c:if test="${empty newTicket}">
			<h2>${ticket.title}</h2>
		</c:if>		
		<div class="form-group">
			<c:if test="${empty newTicket && not empty ticket.message}"><p class="message">${ticket.htmlMessage}</p></c:if>
			<c:if test="${not empty newTicket}"><textarea class="form-control" rows="4" cols="20" name="message" placeholder="message"></textarea></c:if>
		</div>
		<div class="row">
		<c:if test="${not info.editContext.lightInterface}">
			<div class="col-sm-4">
				<label for="priority">priority : </label>
				<select id="priority" name="priority"  class="form-control">
					<option value="0" ${ticket.priority == 0?'selected="selected"':''}>none</option>
					<option value="1" ${ticket.priority == 1?'selected="selected"':''}>low</option>
					<option value="2" ${ticket.priority == 2?'selected="selected"':''}>middle</option>
					<option value="3" ${ticket.priority == 3?'selected="selected"':''}>high</option>
				</select>
			</div>
		</c:if>
		<div class="col-sm-4">
			<label for="status">status : </label>
			<c:if test="${info.editContext.lightInterface}">
				<span>${ticket.status}</span>
				<input type="hidden" name="status" value="${empty ticket.status ? 'new' : ticket.status}" />
			</c:if>
			<c:if test="${not info.editContext.lightInterface}">
				<select id="status" name="status" class="form-control">
					<option ${ticket.status == 'new'?'selected="selected"':''}>new</option>
					<option ${ticket.status == 'working'?'selected="selected"':''}>working</option>
					<option value="onhold" ${ticket.status == 'onhold'?'selected="selected"':''}>on hold</option>
					<option ${ticket.status == 'rejected'?'selected="selected"':''}>rejected</option>
					<option ${ticket.status == 'done'?'selected="selected"':''}>done</option>
					<option ${ticket.status == 'archived'?'selected="selected"':''}>archived</option>
				</select>
			</c:if>
		</div>
		<c:if test="${info.editContext.lightInterface}">
			<input type="hidden" name="share" value="${empty ticket.share ? 'site' : ticket.share}" />
		</c:if>
		<c:if test="${not info.editContext.lightInterface}">
			<div class="col-sm-4">
				<label for="share">share : </label>
				<select id="share" name="share" ${ticket.debugNote ? 'disabled="disabled"' : ''} class="form-control">
					<option value="">none</option>
					<option value="site" ${ticket.share == 'site'?'selected="selected"':''}>${ticket.context}</option>
					<option value="allsites" ${ticket.share == 'allsites'?'selected="selected"':''}>all sites</option>
				    <option value="public" ${ticket.share == '"public"'?'selected="selected"':''}>public</option>
				</select>
			</div>
		</c:if>
</div>	
	</div>
	<div class="col-md-5">
		<strong><div class="authors" title="authors">${ticket.authors}</div></strong>
	    <c:if test="${not info.editContext.lightInterface}">
			<div class="line">
				<label>creation date :</label>${ticket.creationDateLabel}
			</div>
		</c:if>
		<div class="line">
			<label>last update date :</label>${ticket.lastUpdateDateLabel}
		</div>
		<c:if test="${not info.editContext.lightInterface}">
			<c:if test="${not empty ticket.category}">			
			<div class="line">
				<label>category : </label>${ticket.category}			
			</div></c:if>
		</c:if><c:if test="${not empty ticket.url}">	
			<div class="line">
				<label>url : </label><a target="_blank" href="${ticket.url}">${ticket.url}</a>
			</div>
		</c:if>
	</div>
</div>

	<input type="hidden" name="webaction" value="ticket.update" />
	<input type="hidden" name="id" value="${ticket.id}" />	
	<c:if test="${info.editContext.lightInterface}">
		<input type="hidden" name="priority" value="${empty ticket.priority ? '1' : ticket.priority}" />
	</c:if>
		
<c:if test="${not info.editContext.lightInterface}">
	<c:set var="strListSeparator" value="|||"/>
	<c:set var="knownUsers" value="${strListSeparator}"/>
	<c:set var="ticketUsers" value="${strListSeparator}"/>
	<c:forEach var="userLogin" items="${ticket.users}">
		<c:set var="ticketUsers" value="${ticketUsers}${userLogin}${strListSeparator}"/>
	</c:forEach>
	<fieldset>
		<legend>user</legend>
		<c:forEach var="user" items="${ticketAvailableUsers}">
			<c:set var="knownUsers" value="${knownUsers}${user.login}${strListSeparator}"/>
			<c:set var="userKey" value="${strListSeparator}${user.login}${strListSeparator}" />
			<label class="checkbox-inline">
				<input type="checkbox" name="users" value="${user.login}" ${fn:contains(ticketUsers, userKey)?'checked="checked"':''} /> ${user.login}
			</label>
		</c:forEach>
	</fieldset>
	<c:set var="minOne" value="false" />
	<c:set var="buffer">
		<fieldset>
			<legend>Unknown/deleted users</legend>
			<c:forEach var="userLogin" items="${ticket.users}">
				<c:set var="userKey" value="${strListSeparator}${userLogin}${strListSeparator}" />
				<c:if test="${not fn:contains(knownUsers,userKey)}">
					<label class="checkbox-inline">
						<input type="checkbox" name="users" value="${userLogin}" checked="checked" /> ${userLogin}
					</label>
					<c:set var="minOne" value="true" />
				</c:if>
			</c:forEach>
		</fieldset>
	</c:set>
	<c:if test="${minOne}"><c:out value="${buffer}" escapeXml="false" /></c:if>
</c:if>
	<c:if test="${empty newTicket}">
	<h2>comments</h2>
	<c:if test="${fn:length(ticket.comments) > 0}">
	<c:forEach var="comment" items="${ticket.comments}" varStatus="status">
	<div class="comment ${status.last?'last':''}">
		<span class="date">${comment.creationDateString}</span><span class="authors">${comment.authors} : </span>${comment.htmlMessage}		
	</div>
	</c:forEach>
	</c:if>		
	
	<div class="line">
		<label for="comment">new comments</label>
		<textarea id="comment" name="comment"></textarea>
	</div>
	</c:if>
	
	<div class="action">
		<input type="submit" name="delete" class="warning needconfirm btn btn-default" title="${i18n.edit['global.delete']}" value="${i18n.edit['global.delete']}" />
		<input type="submit" name="back" title="${i18n.edit['global.back']}" value="${i18n.edit['global.back']}" class="btn btn-default" />
		<input type="submit" name="ok" value="${i18n.edit['global.ok']}" class="btn btn-default" />		
	</div>
	
</form>

</div>