<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div class="row data-line">	
		<div class="col-xs-4">
		<label for="${param.name}">${param.name}</label>
		</div><div class="col-xs-7">	
			<input class="form-control ${param.style}" type="text" name="${param.name}" value="${param.value}" ${param.style == "color"?'size="7" maxlength="7" ':''}/>
		</div><div class="col-xs-1 check">
		<c:if test="${not empty contentContext.currentTemplate.templateData[param.name]}">
		<i class="fa fa-check" aria-hidden="true" title="define in current template : ${contentContext.currentTemplate.name}" lang="en"></i>
		</c:if>
		</div>
	</div>