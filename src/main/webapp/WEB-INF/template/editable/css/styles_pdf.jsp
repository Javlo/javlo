<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:if test="${not empty template.backgroundColor}"
> background-color: ${template.backgroundColor};</c:if><c:if test="${not empty template.borderColor}"
> border-color: ${template.borderColor};</c:if><c:if test="${not empty template.borderWidth}"
> border-width: ${template.borderWidth}; border-style: solid;</c:if>