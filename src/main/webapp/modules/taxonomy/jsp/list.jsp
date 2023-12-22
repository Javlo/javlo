<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><c:set var="currentNode" value="${taxonomy.taxonomyBeanMap[param.id]}" /><c:if test="${empty currentNode}">CURRENT NODE NOT FOUND : ${param.id} on ${fn:length(taxonomy.taxonomyBeanMap)} nodes. (taxonomy? ${not empty taxonomy})
</c:if><div id="item-${currentNode.id}">
<jsp:include page="item.jsp?id=${currentNode.id}" />
<ul${fn:length(currentNode.children)==0?' class="hidden"':''}>
<li><jsp:include page="newitem.jsp?id=${currentNode.id}" /></li>
<c:forEach var="node" items="${currentNode.children}">
	<li><jsp:include page="list.jsp?id=${node.id}" /></li>
</c:forEach>
</ul>
</div>