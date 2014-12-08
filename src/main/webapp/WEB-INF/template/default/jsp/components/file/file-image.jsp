<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"
%><c:if test="${empty imagePreview}"><jsp:include page="file.jsp"></jsp:include></c:if>
<c:if test="${not empty imagePreview}">
<div class="file pdf">
<a class="standard" href="${url}">
<figure><img src="${imagePreview}" alt="${label}" />
<c:set var="description" value="" />
<c:if test="${not empty cleanDescription}"><c:set var="description" value='<span class="description">${cleanDescription}</span>' /></c:if>
<figcaption><span class="label">${label}<span class="info">(<span class="format">${ext}</span><span class="size">${size}</span>)</span></span>${not empty cleanDescription?description:''}</figcaption>
</figure></a>
</div></c:if>