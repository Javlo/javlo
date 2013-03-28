<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<div class="link-page-combo">
<select id="select_${compid}" name="child" onchange="window.location=document.getElementById('select_${compid}').value">
<option>${title}</option>
<c:forEach items="${pages}" var="page" varStatus="status">
<option value="${page.url}">${page.title}</option>
</c:forEach>
</select>
</div>