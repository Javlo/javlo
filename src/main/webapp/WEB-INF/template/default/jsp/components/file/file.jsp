<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><%@taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<div class="file pdf">
<c:set var="title" value="" />
<c:if test="${not empty cleanDescription}"><c:set var="title" value=' title="${cleanDescription}"' /></c:if>
<a class="standard" href="${url}"${title}>${label}<span class="info"> (<span class="format">${ext}</span> <span class="size">${size}</span>)</span></a>
</div>