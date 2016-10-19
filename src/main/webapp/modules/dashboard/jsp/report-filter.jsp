<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="report-filter" class="content">
<div class="form-group">
<button class="btn btn-default" onclick="window.print();">print report</button>
</div>
<fieldset>
<legend>Report filter</legend>
<form action="${info.currentURL}" method="post">
<input type="hidden" name="webaction"  value="dashboard.filter" />
<div class="form-group">
	<label for="start-date">Start date (report only on page create from this date)</label>
	<input type="text" name="start-date" id="start-date" class="datepicker-past" value="${reportFilter.startDateLabel}" />
</div>
<input type="submit" class="btn btn-default pull-right" />
</form>
</fieldset>
</div>