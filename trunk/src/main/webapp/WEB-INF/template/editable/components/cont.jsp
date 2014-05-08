<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><table width="100%" style="width: 100%;" valign="top">
	<tr>
		<td width="230" style="width: 230px; vertical-align: top; padding: 15px;" valign="top">
			<div style="border-top: 1px #214875 solid; border-bottom: 1px #214875 solid; padding: 15px 0;">
			<h3 style="margin: 10px 0; padding: 0;">${number.value}</h3>
			<table width="100%" style="width: 100%;">
				<tr>
					<td class="label">${rapporteur.label} : </td>
					<td class="value">${rapporteur.value}</td>
				</tr>
				<tr>
					<td class="label">${administrator.label} : </td>
					<td class="value">${administrator.value}</td>
				</tr>				
			</table>
			<br />
			<h4 style="margin: 5px 0;">${timetable.label}</h4>
			${timetable.viewXHTMLCode}
			</div>	
		</td>
		<td>
			<h2>${title.value}</h2>
			<p>
			<img width="80" style="width: 80px; float: left; margin: 0 10px 5px 0;" src="${image.previewURL}" alt="${image.label}" />
			${description.displayValue}
			</p>
		</td>
	</tr>	
</table>
