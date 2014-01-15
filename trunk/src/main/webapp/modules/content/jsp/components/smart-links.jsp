<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="smart-link-body">
<div class="picture choice-images">

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
		
</c:if>	
<c:if test="${fn:length(images)==0 && not empty comp.imageURL}">
<figure>
	<img src="${comp.imageURL}" />
	<figcaption></figcaption>
</figure>
<script type="text/javascript">
<!--
displayFigureSize(jQuery("#comp-${comp.id} figure"));
//-->
</script>
</c:if>
</div>
<div class="info">
	<div class="line">
		<label for="${comp.imageInputName}">image</label>
		<input type="text" name="${comp.imageInputName}" value="${comp.imageURL}" />
	</div>
	<div class="line">
		<label for="${comp.dateInputName}">date</label>
		<input type="text" name="${comp.dateInputName}" value="${date}" />
	</div>
	<div class="line">
		<label for="${comp.titleInputName}">title</label>
		<input type="text" name="${comp.titleInputName}" value="${title}" />
	</div>
	<div class="line">
		<label for="${comp.descriptionInputName}">description</label>
		<textarea name="${comp.descriptionInputName}">${description}</textarea>
	</div>
</div>
</div>
<c:if test="${fn:length(images)>0}">
<script type="text/javascript">
<!--
imageChoice("comp-${comp.id}");
//-->
</script>
</c:if>
