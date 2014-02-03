<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div class="line">
	<label for="${param.name}">${param.name}</label>
	<input class="color" type="text" name="${param.name}" value="${param.value}" size="7" maxlength="7" />
	<c:if test="${not empty param.value}">
		<span style="margin-left: 12px; vertical-align: text-top; border: 1px #777777 solid; display: inline-block; width: 26px; height: 26px; background-color: ${param.value}">&nbsp;</span>
	</c:if>
</div>
