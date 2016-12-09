<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><li class="${template.valid?'valid':'unvalid'}${info.page.templateId == template.name?' active':''}">
		<c:if test="${not empty selectUrl}"><a href="${selectUrl}&template=${template.name}" title="select ${template.name}" class="select-template"></c:if>
        <div class="thumb">
        	<c:if test="${empty param.previewEdit}">
            	<img src="${template.previewUrl}" alt="${template.name}" />
            </c:if>
            <c:if test="${not empty param.previewEdit}">
            
             	<c:url value="${info.currentURL}" var="selectURL" context="/">
                    	<c:param name="webaction" value="template.selectTemplate" />
                    	<c:param name="templateid" value="${template.name}" />
                </c:url>  
            
            	<a href="${selectURL}" class="select-template">
            	<img src="${template.previewUrl}" alt="${template.name}" />
           		<div class="info">
				   <p>
                   <label>${i18n.edit['global.name']}:</label>
                   <span>${template.name}</span>
	               </p>	               
	               <p>
	                   <label>${i18n.edit['template.parent']}:</label>
	                   <span>${template.parent}</span>
	               </p>
	               <p>
	                   <label>${i18n.edit['global.version']}:</label>
	                   <span>${template.version}</span>
	               </p>
	               <p>
	                   <label>${i18n.edit['template.creation-date']}:</label>
	                   <span>${template.creationDate}</span>
	               </p>
	               <p>
	                   <label>${i18n.edit['template.count-renderers']}:</label>
	                   <span>${fn:length(template.renderers)}</span>
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
                <c:if test="${not empty template.parent}"><p>
                    <label>${i18n.edit['template.parent']}:</label>
	                   <span>${template.parent}</span>
                </p></c:if>
                <p>
                    <label>${i18n.edit['global.version']}:</label>
                    <span>${template.version}</span>
                </p>
                <p>
                    <label>${i18n.edit['template.creation-date']}:</label>
                    <span>${template.creationDate}</span>
                </p>
                <p>
	                   <label>${i18n.edit['template.count-renderers']}:</label>
	                   <span>${fn:length(template.renderers)}</span>
	               </p>
                 <c:if test="${empty selectUrl}"><p>
                	<a href="${template.downloadURL}">${i18n.edit['admin.download-template']}</a>                	
                </p></c:if>  
                 <c:if test="${empty selectUrl}">              
                <p class="menu">   
                
                	<c:url value="${info.currentModuleURL}/jsp/page_list.jsp" var="pageListURL" context="/">
						<c:param name="name" value="${template.name}" />
						<c:param name="webaction" value="template.pageTemplate" />						
					</c:url>
                             	
                    <c:if test="${globalContext.master}"><a href="${pageListURL}" class="view popup" title="${template.name}"></a></c:if>
                    
                    <c:url value="${info.currentURL}" var="editURL" context="/">
                    	<c:param name="webaction" value="template.goEditTemplate" />
                    	<c:param name="templateid" value="${template.name}" />
                    </c:url>                    
                    <a href="${editURL}" class="edit"></a>

                    <c:url value="${info.currentURL}" var="deleteURL" context="/">
                    	<c:param name="webaction" value="template.delete" />
                    	<c:param name="id" value="${template.name}" />
                    </c:url>
                    <a href="${deleteURL}" class="delete needconfirm"></a>
                    
                    <c:if test="${not empty selectUrl}">
                    
                    <a href="${selectUrl}&template=${template.name}" class="select" title="select"></a>
                    </c:if>
                    
                    <a href="${info.currentURL}?webaction=template.commitChildren&templateid=${template.name}" class="push" title="commit"></a>
                    
                    <c:if test="${not template.valid}">
                    <c:url value="${info.currentURL}" var="validURL" context="/">
                    	<c:param name="webaction" value="template.validate" />
                    	<c:param name="id" value="${template.name}" />
                    </c:url>
                    <a href="${validURL}" class="validate" title="validate"></a>
                    </c:if>
                    
                </p>     
                </c:if>           
            </div><!--info-->          
            </c:if>
        <span title="${template.valid?'valid':'not valid'}" class="valid-info glyphicon glyphicon-${template.valid?'ok-sign':'exclamation-sign'}"></span>
        </div><!--thumb-->
        <c:if test="${not empty selectUrl}"></a></c:if>
        
 	</li>