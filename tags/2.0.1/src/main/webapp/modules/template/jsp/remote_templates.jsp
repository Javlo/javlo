<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div id="gallery" class="gallery remote-template full-height">
<div id="gridview" class="thumbview">
<ul>
<c:set var="max" value="24" />
<c:if test="${not empty param.viewAll}">
	<c:set var="max" value="9999" />	
</c:if>
<c:forEach var="template" items="${templateFactory.templates}" begin="1" end="${max}">	
    <li>
        <div class="thumb">
            &nbsp;<img src="${template.imageURL}" alt="${template.name}" />&nbsp;
            <div class="info">
                <p>
                    <label>${i18n.edit['global.name']}:</label>
                    <span>${template.name}</span>
                </p>
                <p>
                    <label>${i18n.edit['template.creation-date']}:</label>
                    <span>${template.creationDate}</span>
                </p>
                <p>
                	<a href="${template.downloadURL}">${i18n.edit['admin.download-template']}</a>
                </p>
                <p class="menu">
                    <a href="${template.URL}" class="preview" title="${template.name}" target="_blank"></a>
                    <a href="${info.currentURL}?webaction=import&name=${template.name}&list=${param.list}" class="import" title="import ${template.name}"></a>
                </p>
            </div><!--info-->
        </div><!--thumb-->
 	</li> 	
 	</c:forEach>
 </ul>
 </div>
 </div>
 
