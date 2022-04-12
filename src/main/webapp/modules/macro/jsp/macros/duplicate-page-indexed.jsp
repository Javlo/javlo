<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><form method="post" action="${info.currentURL}" class="standard-form">
	<fieldset>
		<legend>Duplicate page</legend>
		<input type="hidden" name="webaction" value="duplicate-page-indexed.duplicate" />
		<input type="hidden" name="module" value="content" />
		<input type="hidden" name="page" value="${info.pageName}" />

		<div class="row">
			<div class="col-xs-5">
				<input class="form-control" type="number" min="1" max="998" name="from" value="1" />
			</div>
			<div class="col-xs-2">
				<svg style="display: block; margin: 3px auto 0 auto;"xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-arrow-right" viewBox="0 0 16 16">
					<path fill-rule="evenodd" d="M1 8a.5.5 0 0 1 .5-.5h11.793l-3.147-3.146a.5.5 0 0 1 .708-.708l4 4a.5.5 0 0 1 0 .708l-4 4a.5.5 0 0 1-.708-.708L13.293 8.5H1.5A.5.5 0 0 1 1 8z" />
				</svg>
			</div>
			<div class="col-xs-5">
				<input class="form-control" type="number" min="2" max="999" name="to" value="" />
			</div>
		</div>

		<div class="action">
			<input class="btn btn-primary pull-right" type="submit" value="duplicate" />
		</div>

	</fieldset>
</form>


