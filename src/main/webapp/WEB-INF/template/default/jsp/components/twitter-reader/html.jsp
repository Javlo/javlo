<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="twitter-reader">
<ul>
<c:forEach var="tweet" items="${tweets}" varStatus="status">
<li class="item-${status.index+1}">
<a href="${tweet.URL}"><span class="tweet.authors">${tweet.authors}</span></a>
<span class="tweet.message">${tweet.htmlMessage}</span>
</li>

</c:forEach>
</ul>
</div>

