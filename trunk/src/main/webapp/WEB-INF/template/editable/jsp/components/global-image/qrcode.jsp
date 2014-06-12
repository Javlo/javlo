<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><table class="qrcode" width="100%" style="width: 100%;">
	<tr>
		<td width="200">
			<img width="200" src="${previewURL}" />
		</td>
		<td width="10">&nbsp;</td>
		<td>${not empty label?label:description}</td>
		<td width="10">&nbsp;</td>
		<td width="125">
			<img width="125" src="${info.QRCodeLinkPrefix}${comp.id}.png" alt="QRCode" />
		</td>
	</tr>
</table>
