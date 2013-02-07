<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">
<form class="standard-form" id="create-ticket" method="post" action="${info.currentURL}">
		<input type="hidden" name="webaction" value="ticket.update" />
		<input type="hidden" name="id" value="${ticket.id}" />
		<div class="line">
			<label for="priority">priority : </label>
			<select id="priority" name="priority">
				<option value="1" ${ticket.priority == 1?'selected="selected"':''}>status</option>
				<option value="2" ${ticket.priority == 2?'selected="selected"':''}>middle</option>
				<option value="3" ${ticket.priority == 3?'selected="selected"':''}>high</option>
			</select>
		</div>
		<div class="line">
			<label for="status">status : </label>
			<select id="status" name="status">
				<option ${ticket.status == 'new'?'selected="selected"':''}>new</option>
				<option ${ticket.status == 'working'?'selected="selected"':''}>working</option>
				<option ${ticket.status == 'on old'?'selected="selected"':''}>on old</option>
				<option ${ticket.status == 'refuse'?'selected="selected"':''}>refuse</option>
				<option ${ticket.status == 'archived'?'selected="selected"':''}>archived</option>
			</select>
		</div>
		<div class="line">
			<label>authors :</label>${ticket.authors}
		</div>	
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
		<c:if test="${not empty ticket.url}">
		<div class="line">
			<label>url : </label><a href="${ticket.url}">${ticket.url}</a>			
		</div>
		</c:if>
					
		<div class="line">
			<label>message</label>
			<div class="message">${ticket.message}</div>			
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
	
	<fieldset>
		<legend>new comments</legend>
		<textarea name="comment"></textarea>
		<div class="action">
		<input type="submit" name="back" value="${i18n.edit['global.back']}" />
		<input type="submit" value="${i18n.edit['global.ok']}" />		
		</div>
	</fieldset>
</form>

</div>