<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>
<div class="smart-link-body choice-images">
<div class="row"><div class="col-sm-3">
<input class="image-field" type="hidden" name="${comp.imageInputName}" value="${comp.imageURL}" />
<c:if test="${fn:length(images)>0}">	
	
	<ul>
	<c:forEach var="image" items="${images}">
	<li>
		<figure>				
		<img src="${image.uri}" />
		<figcaption></figcaption>				
		</figure>
	</li>
	</c:forEach>
	
	</ul>
		
</c:if><c:if test="${fn:length(images)==0 && not empty comp.imageURL}">
<figure>
	<img src="${comp.imageURL}" />
	<figcaption></figcaption>	
</figure>
</c:if>
</div><div class="col-sm-9">
<div class="info">
	<div class="form-group">
		<div class="row"><div class="col-sm-3">
		<label for="${comp.imageInputName}">image</label>
		</div><div class="col-sm-9">
		<input class="form-control" type="text" name="${comp.imageInputName}" value="${comp.imageURL}" />
		</div></div>
	</div>
	<div class="form-group">
		<div class="row"><div class="col-sm-3">
		<label for="${comp.dateInputName}">date</label>
		</div><div class="col-sm-9">
		<input class="form-control" type="text" name="${comp.dateInputName}" value="${date}" />
		</div></div>
	</div>
	<div class="form-group">
		<div class="row"><div class="col-sm-3">	
		<label for="${comp.titleInputName}">title</label>
		</div><div class="col-sm-9">
		<input class="form-control" type="text" name="${comp.titleInputName}" value="${title}" />
		</div></div>
	</div>
	<div class="form-group">
		<div class="row"><div class="col-sm-3">
		<label for="${comp.descriptionInputName}">description</label>
		</div><div class="col-sm-9">
		<textarea class="form-control" name="${comp.descriptionInputName}">${description}</textarea>
		</div></div>
	</div>
</div>
</div>
</div>
</div>
<script type="text/javascript">
<!--
displayFigureSize(jQuery("#comp-${comp.id} figure"));
//-->
</script>
<c:if test="${fn:length(images)>0}">
<script type="text/javascript">
<!--
imageChoice("comp-${comp.id}");
//-->
</script>
</c:if>