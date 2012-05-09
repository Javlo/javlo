<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div id="gallery" class="gallery main-template">
<div id="gridview" class="thumbview">
<ul>
<c:forEach var="template" items="${templates}">
    <li>
        <div class="thumb">
            <img src="${template.previewUrl}" alt="${template.template.name}" />
            <div class="info">
                <p>
                    <label>${i18n.edit['global.name']}:</label>
                    <span>${template.template.name}</span>
                </p>
                <p>
                    <label>${i18n.edit['admin.file-source']}:</label>
                    <span><a href="${template.htmlUrl}">${template.htmlFile}</a></span>
                </p>
                <p>
                    <label>${i18n.edit['global.author']}:</label>
                    <span>${template.template.authors}</span>
                </p>
                <p>
                    <label>${i18n.edit['template.creation-date']}:</label>
                    <span>${template.creationDate}</span>
                </p>
                <p>
                	<a href="${template.downloadUrl}">${i18n.edit['admin.download-template']}</a>
                </p>
                <p class="menu">
                    <a href="${template.viewUrl}" class="view" title="${template.template.name}"></a>
                    <a href="${info.currentURL}?webaction=goEditTemplate&name=${template.template.name}&mailing=${template.template.mailing}" class="edit"></a>
                    <a href="${info.currentURL}?webaction=deleteTemplate&name=${template.template.name}&mailing=${template.template.mailing}" class="delete"></a>
                    <c:if test="${not empty selectUrl}">
                    <a href="${selectUrl}&template=${template.template.name}&mailing=${template.template.mailing}" class="select" title="select"></a>
                    </c:if>
                </p>
            </div><!--info-->
        </div><!--thumb-->
 	</li>
 	</c:forEach>
 </ul>
 </div>
 </div>
 
