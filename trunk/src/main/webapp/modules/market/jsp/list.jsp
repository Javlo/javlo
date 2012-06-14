<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div id="gallery" class="gallery remote-resource full-height">
<div id="gridview" class="thumbview">
<ul>
<c:set var="max" value="24" />
<c:if test="${not empty param.viewAll}">
	<c:set var="max" value="9999" />	
</c:if>
<c:forEach var="resource" items="${resources}" begin="1" end="${max}">	
    <li>
        <div class="thumb">
            &nbsp;<img src="${resource.imageURL}" alt="${resource.name}" />&nbsp;
            <div class="info">
                <p>
                    <label>${i18n.edit['global.name']}:</label>
                    <span>${resource.name}</span>
                </p>
                <p>
                    <label>${i18n.edit['global.date']}:</label>
                    <span>${resource.dateAsString}</span>
                </p>
                <p>
                	<a href="${resource.downloadURL}">${i18n.edit['resource.download']}</a>
                </p>
                <p class="menu">
                    <a href="${remote.URL}" class="preview" title="${resource.name}" target="_blank"></a>
                    <a href="${info.currentURL}?webaction=importPage&id=${resource.id}" class="import" title="import ${resource.name}"></a>
                </p>
            </div><!--info-->
        </div><!--thumb-->
 	</li> 	
 	</c:forEach>
 </ul>
 </div>
 </div>
 
