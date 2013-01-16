<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<form class="standard-form" id="form-search" method="post" action="${info.currentURL}">
	<input type="hidden" name="webaction" value="search.search" />

	<div class="line">
		<label for="q">${properties['field.keyword']} :</label>
		<input id="q" type="text" id="q" name="q" value="${searchResult.query}" />
	</div>
	<fieldset>
	<legend>${properties['group.categories']}</legend>
	
	<c:forEach items="${paramValues}" var="comps">
	<c:forEach var="comp" items="${comps.value}">
		<c:if test="${comp eq  'bank'}">
			<c:set var="bank" value="true" />
		</c:if>
		<c:if test="${comp eq  'juridique'}">
			<c:set var="juridique" value="true" />
		</c:if>
		<c:if test="${comp eq  'technical'}">
			<c:set var="technical" value="true" />
		</c:if>
	</c:forEach>
	</c:forEach> 
	
	<div class="inline">
		<input type="checkbox" id="bank" name="comps" value="general" ${not empty bank?'checked="checked"':''}/><label for="bank">${properties['category.general']}</label>
	</div><div class="inline">
		<input type="checkbox" id="bank" name="comps" value="bank" ${not empty bank?'checked="checked"':''}/><label for="bank">${properties['category.bank']}</label>
	</div><div class="inline">
		<input type="checkbox" id="juridique" name="comps" value="juridique" ${not empty juridique?'checked="checked"':''}/><label for="juridique">${properties['category.law']}</label>
	</div><div class="inline">
		<input type="checkbox" id="technical" name="comps" value="technical" ${not empty technical?'checked="checked"':''}/><label for="technical">${properties['category.technic']}</label>		
	</div>
	</fieldset>
	<div class="action">
		<input type="submit" />
	</div>	
</form>


<!-- <ul id="search-result">	
<c:forEach items="${searchList}" var="result"  varStatus="status">
<li class="search-block">
<a href="${result.url}" class="title">${result.title}</a>
<span class="date">${result.dateString}</span>
<span class="description">${result.description}</span>
</li>
</c:forEach>
</ul>-->

<ul id="search-result">	
<c:forEach items="${searchList}" var="result">
<c:if test="${not empty result.components}">
<c:forEach items="${result.components}" var="comp">
<li class="search-block ${comp.type}">
${comp.xhtml}
</li>
</c:forEach>
</c:if>
<c:if test="${empty result.components}">
<li class="search-block content">
<span class="title"><a href="${result.url}">${result.title}</a></span>
<span class="date">${result.dateString}</span>
<span class="description">${result.description}</span>
</li>
</c:if>
</c:forEach>
</ul>