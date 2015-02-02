<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="content">

<form id="create-ticket" method="post" action="${info.currentURL}">
	<input type="hidden" name="webaction" value="ticket.update" />
	<input type="hidden" name="id" value="" />
	<div class="form-group">
		<label for="priority">priority</label>
		<select id="priority" name="priority" class="form-control">
			<option value="0">none</option>
			<option value="1">low</option>
			<option value="2">middle</option>
			<option value="3">high</option>
		</select>
	</div>
	<div class="form-group">
		<label for="title">title</label>
		<input type="text" id="title" name="title" class="form-control" />
	</div>
	<div class="form-group">
		<label for="url">url</label>
		<input type="text" id="url" name="url" class="form-control" />
	</div>
	<div class="form-group">
		<label for="category">category</label>
		<select id="category" name="category" class="form-control">
			<option>usability</option>
			<option>bug</option>
			<option>features</option>
		</select>
	</div>
	<div class="form-group">
		<label for="users">users</label>
		<div class="row">
			<c:forEach var="user" items="${ticketAvailableUsers}">
				<div class="col-xs-6">
					<label>
						<input type="checkbox" name="users" value="${user.login}" /> ${user.login}
					</label>
				</div>
			</c:forEach>
		</div>
	</div>
	<div class="form-group">
		<label for="message">message</label>
		<textarea id="message" name="message" class="form-control"></textarea>
	</div>
	<button type="submit" class="btn btn-default">${i18n.view['global.ok']}</button>
</form>

</div>