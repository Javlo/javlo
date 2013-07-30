<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div id="gallery" class="gallery main-template full-height">
<div id="gridview" class="thumbview">
<ul>
<c:forEach var="template" items="${templates}">
    <li class="${template.valid?'valid':'unvalid'}">
        <div class="thumb">
        	<c:if test="${empty param.previewEdit}">
            	<img src="${template.previewUrl}" alt="${template.name}" />
            </c:if>
            <c:if test="${not empty param.previewEdit}">
            
             	<c:url value="${info.currentURL}" var="selectURL">
                    	<c:param name="webaction" value="selectTemplate" />
                    	<c:param name="templateid" value="${template.name}" />
                    </c:url>  
            
            	<a href="${selectURL}">
            	<img src="${template.previewUrl}" alt="${template.name}" />
           		<div class="info">
				   <p>
                   <label>${i18n.edit['global.name']}:</label>
                   <span>${template.name}</span>
	               </p>	             
	               <p>
	                   <label>${i18n.edit['global.author']}:</label>
	                   <span>${template.authors}</span>
	               </p>
	               <p>
	                   <label>${i18n.edit['template.parent']}:</label>
	                   <span>${template.parent}</span>
	               </p>
	               <p>
	                   <label>${i18n.edit['template.creation-date']}:</label>
	                   <span>${template.creationDate}</span>
	               </p>
	            </div>
	            </a>
            </c:if>
            <c:if test="${empty param.previewEdit}">
            <div class="info">
                <p>
                    <label>${i18n.edit['global.name']}:</label>
                    <span>${template.name}</span>
                </p>
                <p>
                    <label>${i18n.edit['admin.file-source']}:</label>
                    <span><a href="${template.htmlUrl}">${template.htmlFile}</a></span>
                </p>
                <p>
                    <label>${i18n.edit['global.author']}:</label>
                    <span>${template.authors}</span>
                </p>
                <p>
                    <label>${i18n.edit['template.creation-date']}:</label>
                    <span>${template.creationDate}</span>
                </p>
                <p>
                	<a href="${template.downloadURL}">${i18n.edit['admin.download-template']}</a>                	
                </p>                
                <p class="menu">                	
                    <a href="${template.viewUrl}" class="view" title="${template.name}"></a>
                    
                    <c:url value="${info.currentURL}" var="editURL">
                    	<c:param name="webaction" value="goEditTemplate" />
                    	<c:param name="templateid" value="${template.name}" />
                    </c:url>                    
                    <a href="${editURL}" class="edit"></a>

                    <c:url value="${info.currentURL}" var="deleteURL">
                    	<c:param name="webaction" value="delete" />
                    	<c:param name="templateid" value="${template.name}" />
                    </c:url>
                    <a href="${deleteURL}" class="delete"></a>
                    
                    <c:if test="${not empty selectUrl}">
                    
                    <a href="${selectUrl}&template=${template.name}&mailing=${template.mailing}" class="select" title="select"></a>
                    </c:if>
                    
                    <c:if test="${not template.valid}">
                    <c:url value="${info.currentURL}" var="validURL">
                    	<c:param name="webaction" value="validate" />
                    	<c:param name="templateid" value="${template.name}" />
                    </c:url>
                    <a href="${validURL}" class="validate" title="validate"></a>
                    </c:if>
                    
                </p>                
            </div><!--info-->
            </c:if>
        </div><!--thumb-->
 	</li>
 	</c:forEach>
 </ul>
 </div>
 </div>
 
