<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><c:if test="${not empty template.backgroundColor}"
> background-color: ${template.backgroundColor};</c:if><c:if test="${not empty template.borderColor}"
> border-color: ${template.borderColor};</c:if><c:if test="${not empty template.borderWidth}"
> border-width: ${template.borderWidth}; border-style: solid;</c:if>