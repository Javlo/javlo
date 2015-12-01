<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><h2>Choose component type</h2>

<form method="post" action="${info.currentURL}" class="standard-form">
<div class="form-group">
<fieldset>
<input type="hidden" name="webaction" value="macro-delete-component.delete" />
<button type="button" class="btn btn-default pull-right" onclick="jQuery('.types').each(function() {jQuery(this).prop('checked', !jQuery(this).prop('checked'));}); return false;">inverse</button>
<div class="row">
<c:forEach var="comp" items="${components}">
	<div class="col-sm-3">
		<label><input class="types" name="types" value="${comp.type}" type="checkbox" /> ${comp.type}</label>
	</div>	
</c:forEach>
</div>
</fieldset>
</div>
<div class="form-group">
	<div class="row">
	<div class="col-sm-2"><label for="content">contains</label></div>
	<div class="col-sm-10"><input class="form-control" id="content" name="content" /></div>
	</div>
</div>
<div class="form-group">
	<div class="row">
	<div class="col-sm-12"><label><input name="allpages" type="checkbox" /> all pages.</label></div>
	</div>
</div>

<div class="form-group">
<button type="submit" class="btn btn-primary pull-right">Delete</button>
</div>

</form>





