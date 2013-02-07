<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="content">

<form class="standard-form" id="create-ticket" method="post" action="${info.currentURL}">
	<input type="hidden" name="webaction" value="ticket.update" />
	<input type="hidden" name="id" value="${ticket.id}" />
	<div class="line">
		<label for="priority">priority</label>
		<select id="priority" name="priority">
			<option value="1">low</option>
			<option value="2">middle</option>
			<option value="3">high</option>
		</select>
	</div>
	<div class="line">
		<label for="title">title</label>
		<input type="text" id="title" name="title" />
	</div>
	<div class="line">
		<label for="url">url</label>
		<input type="text" id="url" name="url" />
	</div>		
	<div class="line">
		<label for="category">category</label>
		<select id="category" name="category">
			<option>usability</option>
			<option>bug</option>
			<option>features</option>				
		</select>
	</div>		
	<div class="line">
		<label for="message">message</label>
		<textarea id="message" name="message"></textarea>
	</div>
	<div class="action">
		<input type="submit" value="${i18n.view['global.ok']}" />
	</div>	
</form>

</div>