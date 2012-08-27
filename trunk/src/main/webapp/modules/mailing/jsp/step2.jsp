<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="content">
	<form action="${info.currentURL}" method="post">
		<input type="hidden" name="webaction" value="wizard" />
		<input type="hidden" name="box" value="sendwizard" />
		<div>
			STEP 2
		</div>
		<input type="submit" name="previous" value="Previous" />
		<input type="submit" name="next" value="Next" />
	</form>
</div>
