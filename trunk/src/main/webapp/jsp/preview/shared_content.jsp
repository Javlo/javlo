<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<input class="filter" type="text" placeholder="${i18n.edit['global.filter']}" onkeyup="filter(this.value, '#preview_command .shared-content .content-wrapper');" />
<form id="shared-content-form" class="js-submit" action="${info.currentURL}" method="post">
	<div>
		<input type="hidden" name="webaction" value="shared-content.choose" />
		<select name="provider">
			<option class="placeholder" value="">${i18n.edit["preview.choose-provider"]}</option>
			<c:forEach var="provider" items="${sharedContentProviders}">
				<option ${sharedContentContext.provider eq provider.name?'selected="selected"':''}>${provider.name}</option>
			</c:forEach>
		</select>
		<c:if test="${fn:length(sharedContentCategories)>0}">
		<select name="category">
			<c:forEach var="category" items="${sharedContentCategories}">
				<option ${sharedContentContext.category eq category.key?'selected="selected"':''} value="${category.key}">${category.value}</option>
			</c:forEach>
		</select>		
		</c:if>
		<input type="submit" />
	</div>
</form>
<c:if test="${not empty sharedContent}">
<div class="content shared-content">
<c:forEach var="content" items="${sharedContent}">
<div class="content-wrapper ${not empty content.description?'width-description':'without-description'}">
<div class="content" data-shared="${content.id}">
	<h4><span>${content.title}</span></h4>
	<figure><img src="${content.imageURL}" /></figure>
	<c:if test="${not empty content.description}">
		<div class="description">${content.description}</div>
	</c:if>	
</div>
</div>
</c:forEach>
</div>
</c:if>
