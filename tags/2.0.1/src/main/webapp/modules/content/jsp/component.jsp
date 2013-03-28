<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="close-component">
<a class="close" href="${info.currentURL}?webaction=displayComponentsList">x</a>
</div>
<div class="content component-list"> <!-- components -->
<div class="one_half">
<c:set var="closeAccordion" value="" />
<c:set var="titleCount" value="0" />
<c:forEach var="comp" items="${components}">
<c:if test="${comp.metaTitle}">${closeAccordion}
<c:set var="titleCount" value="${titleCount+1}" />
<c:if test="${titleCount == 4}">
	</div><div class="one_half">	
</c:if><h4 style="color: #${comp.hexColor}">${i18n.edit[comp.value]}</h4>
<div><ul><c:set var="closeAccordion" value="</ul></div>"
/></c:if><c:if test="${!comp.metaTitle}"
><li${comp.selected?' class="selected"':''}><a class="ajax" href="${info.currentURL}?webaction=changeComponent&type=${comp.type}">${comp.label}</a></li>
</c:if></c:forEach>
${closeAccordion}
</div>
</div> <!-- /components -->
