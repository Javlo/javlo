<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<li class="${template.valid?'valid':'unvalid'}${info.page.templateId == template.name?' active':''}">
		<c:if test="${not empty selectUrl}"><a href="${selectUrl}&template=${template.name}" title="select ${template.name}"></c:if>
        <div class="thumb">
        	<c:if test="${empty param.previewEdit}">
            	<img src="${template.previewUrl}" alt="${template.name}" />
            </c:if>
            <c:if test="${not empty param.previewEdit}">
            
             	<c:url value="${info.currentURL}" var="selectURL" context="/">
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
                <c:if test="${empty selectUrl}"><p>
                    <label>${i18n.edit['admin.file-source']}:</label>
                     <span><a href="${template.htmlUrl}">${template.htmlFile}</a></span>
                </p></c:if>
                <p>
                    <label>${i18n.edit['global.author']}:</label>
                    <span>${template.authors}</span>
                </p>
                <p>
                    <label>${i18n.edit['template.creation-date']}:</label>
                    <span>${template.creationDate}</span>
                </p>
                 <c:if test="${empty selectUrl}"><p>
                	<a href="${template.downloadURL}">${i18n.edit['admin.download-template']}</a>                	
                </p></c:if>  
                 <c:if test="${empty selectUrl}">              
                <p class="menu">                	
                    <a href="${template.viewUrl}" class="view" title="${template.name}"></a>
                    
                    <c:url value="${info.currentURL}" var="editURL" context="/">
                    	<c:param name="webaction" value="goEditTemplate" />
                    	<c:param name="templateid" value="${template.name}" />
                    </c:url>                    
                    <a href="${editURL}" class="edit"></a>

                    <c:url value="${info.currentURL}" var="deleteURL" context="/">
                    	<c:param name="webaction" value="delete" />
                    	<c:param name="id" value="${template.name}" />
                    </c:url>
                    <a href="${deleteURL}" class="delete needconfirm"></a>
                    
                    <c:if test="${not empty selectUrl}">
                    
                    <a href="${selectUrl}&template=${template.name}" class="select" title="select"></a>
                    </c:if>
                    
                    <a href="${info.currentURL}?webaction=commitChildren&templateid=${template.name}" class="push" title="commit"></a>
                    
                    <c:if test="${not template.valid}">
                    <c:url value="${info.currentURL}" var="validURL" context="/">
                    	<c:param name="webaction" value="validate" />
                    	<c:param name="id" value="${template.name}" />
                    </c:url>
                    <a href="${validURL}" class="validate" title="validate"></a>
                    </c:if>
                    
                </p>     
                </c:if>           
            </div><!--info-->          
            </c:if>
        </div><!--thumb-->
        <c:if test="${not empty selectUrl}"></a></c:if>
 	</li>