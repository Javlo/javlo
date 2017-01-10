<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="rss-reader">
<ul>
<c:forEach var="tweet" items="${tweets}" varStatus="status">
<li>
<span class="tweet.authors">${tweet.authors}</span>
<span class="tweet.message">${tweet.autoLinkMessage}</span>
</li>

</c:forEach>
</ul>
</div>

