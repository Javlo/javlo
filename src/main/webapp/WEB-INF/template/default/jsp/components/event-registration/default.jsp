<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="event-regisstration">
	<form action="${info.currentURL}" method="post">
		<div class="form-group">
			<input type="hidden" name="webaction" value="event-registration.confirm" />
			<button class="btn btn-primary" type="submit" name="confirm">Confirm</button>
			<button class="btn btn-default" type="submit" name="confirm">Cancel</button>
		</div>
	</form>
</div>