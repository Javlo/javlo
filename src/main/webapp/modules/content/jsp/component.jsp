<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="accordion"> <!-- components -->
<c:set var="closeAccordion" value="" />
<c:forEach var="comp" items="${components}">
<c:if test="${comp.metaTitle}">${closeAccordion}
<h4><a href="#">${i18n.edit[comp.value]}</a></h4>
<div><ul>
<c:set var="closeAccordion" value="</ul></div>"
/></c:if>
<c:if test="${not comp.metaTitle}"
><li><a class="ajax" href="${info.currentURL}?webaction=changeComponent&type=${comp.type}">${comp.label}</a></li>
</c:if></c:forEach>
${closeAccordion}
</div> <!-- /components -->
