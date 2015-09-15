<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"
%>p {
	font-size: ${empty info.template.style.finalTextSize?'12px':info.template.style.finalTextSize};
	color: green;
}