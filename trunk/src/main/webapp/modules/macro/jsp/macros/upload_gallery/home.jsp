<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="cols">
<div class="one_third">

<form method="post" action="${info.currentURL}" class="standard-form" enctype="multipart/form-data">
<input type="hidden" name="webaction" value="macro-upload-gallery.upload" />
<fieldset>
<legend>Image importation</legend>

<div class="line">
	<c:set var="selectedMonth" value="${empty selectedMonth ? (info.currentMonth + 1) : selectedMonth}" />
	<label for="month">month</label>
	<select id="month" name="month">
		<c:forEach var="m" items="${info.months}" varStatus="status">
			<option value="${status.count}" ${selectedMonth eq status.count ? 'selected="selected"' : ''}>${m}</option>
		</c:forEach>
	</select>
</div>
<div class="line">
	<c:set var="selectedYear" value="${empty selectedYear ? info.currentYear : selectedYear}" />
	<label for="year">year</label>
	<input id="year" name="year" type="number" value="${selectedYear}" />
</div>
<div class="line">
	<label for="file">files</label>
	<input id="file" name="file" type="file" multiple="multiple" />
</div>
<div class="action">
	<input type="submit" value="import" />
</div>

</fieldset>
</form>

<fieldset>
<legend>Default metadata</legend>

<div class="form-list">
	<div class="line">
		<label for="title-${file.id}">${i18n.edit["field.title"]}</label>
		<input class="file-title" type="text" id="title-${file.id}" name="title-${file.id}" value="${file.title}" />
	</div>
	<div class="line">
		<label for="description-${file.id}">${i18n.edit["field.description"]}</label>
		<textarea class="file-description" id="description-${file.id}" name="description-${file.id}" rows="5" cols="10">${file.description}</textarea>
	</div>
	<div class="line">
		<label for="location-${file.id}">${i18n.edit["field.location"]}</label>
		<input class="file-location" type="text" id="location-${file.id}" name="location-${file.id}" value="${file.location}" />
	</div>
	<div class="line">
		<label for="date-${file.id}">${i18n.edit["field.date"]}</label>
		<input class="file-date" type="text" id="date-${file.id}" name="date-${file.id}" value="${file.manualDate}" />
	</div>
</div>

</fieldset>

</div>
<div class="two_third last">
	<jsp:include page="/modules/file/jsp/meta.jsp" />
</div>
</div>



