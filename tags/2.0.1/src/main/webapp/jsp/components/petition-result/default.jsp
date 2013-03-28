<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="petition-result">
<c:if test="${not empty country}">
<h3>${country}</h3>
</c:if>
<ul>
<c:forEach var="sign" items="${result}">
	<li>
		<div class="header">
			<span class="name">${sign.name}
			<c:if test="${not empty sign.organization}">
				<span class="organization"> (${sign.organization}) </span>
			</c:if>
			</span>
			- <span class="date">${sign.date}</span>					
		</div>
		<div class="comment">
			${sign.comment}
		</div>
	</li>
</c:forEach>
</ul>
</div>