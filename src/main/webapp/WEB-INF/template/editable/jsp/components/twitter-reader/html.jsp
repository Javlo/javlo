<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<div class="rss-reader">
<ul>
<c:forEach var="tweet" items="${tweets}" varStatus="status">
<li>
<span class="tweet.authors">${tweet.authors}</span>
<span class="tweet.message">${tweet.message}</span>
</li>

</c:forEach>
</ul>
</div>

