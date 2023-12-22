<%@ taglib prefix="c" uri="jakarta.tags.core"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib uri="jakarta.tags.functions" prefix="fn" %><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%>
<table class="footer-wrapper" width="100%" style="width: 100%; border-collapse: collapse; text-align: center;">
<tr><td background-color="#F3F3F3" style="background-color: #F3F3F3">
<c:if test="${not empty follow.value}">
<table class="footer-bloc follow" width="100%" style="width: 100%; border-collapse: collapse; text-align: center;">
	<tr><td height="12px" style="font-size: 0; height: 10px">&nbsp;</td></tr>
	<tr><td style="text-align: center; font-weight: bold;">${follow.viewXHTMLCode}</td></tr>
	<tr><td height="12px" style="font-size: 0; height: 10px">&nbsp;</td></tr>
</table>
</c:if>
<table class="footer-bloc links" width="100%" style="width: 100%; border-collapse: collapse; text-align: center;">	  
	<tr class="links">
		<c:if test="${not empty fb.value}"><td style="text-align: center" class="facebook"><a href="${fb.URL}"><img style="display: inline; width: 60px;" width="60" alt="${fb.title}" src="${info.rootTemplateURL}/img/facebook.png" /></a></td></c:if>
		<c:if test="${not empty twitter.value}"><td style="text-align: center" class="twitter"><a href="${twitter.URL}"><img style="display: inline; width: 60px;" width="60" alt="${twitter.title}" src="${info.rootTemplateURL}/img/twitter.png" /></a></td></c:if>
		<c:if test="${not empty flickr.value}"><td style="text-align: center" class="flickr"><a href="${flickr.URL}"><img style="display: inline; width: 60px;" width="60" alt="${flickr.title}" src="${info.rootTemplateURL}/img/flickr.png" /></a></td></c:if>
	</tr>
	<tr><td height="12px" style="font-size: 0; height: 10px">&nbsp;</td></tr>
</table>
<c:if test="${not empty text.value}">
<table class="footer-bloc text" width="100%" style="width: 100%; border-collapse: collapse; text-align: center;">	
	<tr><td style="text-align: center;">${text.viewXHTMLCode}</td></tr>	
</table>
</c:if>
</td></tr></table>