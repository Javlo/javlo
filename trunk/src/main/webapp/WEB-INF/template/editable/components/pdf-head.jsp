<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><table width="100%" style="width: 100%;">
	<tr>
		<td class="title" width="33%" style="width: 33%; text-align: left;">${title.value}</td>
		<td class="pdf" width="34%" style="width: 34%; text-align: center;"><a href="${info.PDFURL}">${pdf.value}</a></td>
		<td class="number" width="33%" style="width: 33%; text-align: right;">${number.value}</td>
	</tr>		
</table>
