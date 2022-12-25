<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:if test="${info.admin || not empty contentContext.currentTemplate.templateData[param.name]}"><div class="row data-line">
	<div class="col-xs-6 col-6">
	<label for="${param.name}">${param.name}</label>
	</div><div class="col-xs-${info.admin?'4':'6'} col-${info.admin?'4':'6'}">	
		<input placeholder="${i18n.edit['global.default']}" class="form-control ${param.style}" type="text" name="${param.name}" value="${color}" ${param.style == "color"?'size="7" maxlength="7" ':''} onchange="this.style.backgroundColor=this.value" />
	</div><c:if test="${info.admin}"><div class="col-xs-2 col-2 ref-context">
	<i class="bi bi-key"></i> ${contentContext.currentTemplate.templateData[param.name]}
	</div></c:if>
</div></c:if>