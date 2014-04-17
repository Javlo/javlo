<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>
<div class="cols closer">
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
	<input type="text" id="allTitle" name="all-title" placeholder="${i18n.edit['action.change-all-title']}" onkeyup="jQuery('.file-title').each(function(){var t = jQuery(this);if (t.val().length == 0 || t.data('empty') == true) {t.data('empty',true);t.val(jQuery('#allTitle').val())}});"/>
</div><div class="line">
	<input type="text" id="allDescription" name="all-descritpion" placeholder="${i18n.edit['action.change-all-description']}" onkeyup="jQuery('.file-description').each(function(){var t = jQuery(this);if (t.val().length == 0 || t.data('empty') == true) {t.data('empty',true);t.val(jQuery('#allDescription').val())}});"/>
</div><div class="line">
	<input type="text" id="allLocation" name="all-location" placeholder="${i18n.edit['action.change-all-location']}" onkeyup="jQuery('.file-location').each(function(){var t = jQuery(this);if (t.val().length == 0 || t.data('empty') == true) {t.data('empty',true);t.val(jQuery('#allLocation').val())}});"/>
</div><div class="line">
	<input type="text" id="allDate" name="all-date" placeholder="${i18n.edit['action.change-all-date']}" onkeyup="jQuery('.file-date').each(function(){var t = jQuery(this);if (t.val().length == 0 || t.data('empty') == true) {t.data('empty',true);t.val(jQuery('#allDate').val())}});"/>
</div>
</div>

</fieldset>

</div>
<div class="two_third last">
	<jsp:include page="/modules/file/jsp/meta.jsp?close=true" />
</div>
</div>



