<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
 %><c:set var="titleCount" value="0" />
<div class="component-list"> <!-- components -->
<div class="one_half">
<c:forEach var="comp" items="${components}">
<c:if test="${comp.metaTitle}">
<c:set var="titleCount" value="${titleCount+1}" />
<c:if test="${titleCount == 4}">
	</div><div class="one_half">	
</c:if><h4 style="color: #${comp.hexColor}">${i18n.edit[comp.value]}</h4>
</c:if><c:if test="${!comp.metaTitle}"
><div class="component${comp.selected?' selected':''}" data-type="${comp.type}"><span>${comp.label}</span></div>
</c:if></c:forEach>
</div>
<div style="clear: both;"><span></span></div>
</div> <!-- /components -->
