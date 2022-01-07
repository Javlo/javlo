<%@page import="org.javlo.template.TemplateData"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><fieldset>
<legend>${i18n.edit['admin.title.graphic-charter']}</legend>

<input type="hidden" name="graphic-charter" value="true" />

<div class="row">
<div class="col-md-4">

<c:set var="color" value="${currentContext.templateData.background}" scope="request" />
<jsp:include page="template_data.jsp?name=background&style=color" />

<c:set var="color" value="${currentContext.templateData.backgroundMenu}" scope="request" />
<jsp:include page="template_data.jsp?name=backgroundMenu&style=color" />

<c:set var="color" value="${currentContext.templateData.backgroundActive}" scope="request" />
<jsp:include page="template_data.jsp?name=backgroundActive&style=color" />

<c:set var="color" value="${currentContext.templateData.foreground}" scope="request" />
<jsp:include page="template_data.jsp?name=foreground&style=color" />

<c:set var="color" value="${currentContext.templateData.border}" scope="request" />
<jsp:include page="template_data.jsp?name=border&style=color" />

<c:set var="color" value="${currentContext.templateData.text}" scope="request" />
<jsp:include page="template_data.jsp?name=text&style=color" />

<c:set var="color" value="${currentContext.templateData.title}" scope="request" />
<jsp:include page="template_data.jsp?name=title&style=color" />

<c:set var="color" value="${currentContext.templateData.link}" scope="request" />
<jsp:include page="template_data.jsp?name=link&style=color" />

<c:set var="color" value="${currentContext.templateData.componentBackground}" scope="request" />
<jsp:include page="template_data.jsp?name=componentBackground&style=color" />

<c:set var="color" value="${currentContext.templateData.special}" scope="request" />
<jsp:include page="template_data.jsp?name=special&style=color" />

<c:if test="${param.colorMessages}">
<h2>Message</h2>
<jsp:include page="template_data.jsp?name=messagePrimary&value=${currentContext.templateData.messagePrimary}&style=color" />
<jsp:include page="template_data.jsp?name=messageSecondary&value=${currentContext.templateData.messageSecondary}&style=color" />
<jsp:include page="template_data.jsp?name=messageSuccess&value=${currentContext.templateData.messageSuccess}&style=color" />
<jsp:include page="template_data.jsp?name=messageDanger&value=${currentContext.templateData.messageDanger}&style=color" />
<jsp:include page="template_data.jsp?name=messageWarning&value=${currentContext.templateData.messageWarning}&style=color" />
<jsp:include page="template_data.jsp?name=messageInfo&value=${currentContext.templateData.messageInfo}&style=color" />
</c:if>

<div class="row">	
	<div class="col-xs-4">
	<label>color list</label>
	</div><div class="col-xs-8">	
		<div class="row">
		<c:forEach begin="0" end="<%=TemplateData.COLOR_LIST_SIZE-1%>" varStatus="status">
		<div class="col-xs-2"><input class="form-control color" type="text" name="colorList${status.index}" value="${currentContext.templateData.colorList[status.index]}" /></div>
		</c:forEach>
		</div>
	</div>	
</div>

</div><div class="col-md-4">
<div class="form-group">
	<label for="logo">logo : </label>
	<input class="form-control" type="file" name="logo" id="logo" />
</div>
<c:if test="${not empty logoPreview}">
	<c:url var="delURL" value="${info.currentURL}" context="/">
		<c:param name="webaction" value="admin.removelogo" />
	</c:url>
	<div class="delete-link"><a href="${delURL}" title="remove logo"><i class="fa fa-times" aria-hidden="true"></i></a></div>
	<img alt="logo" src="${logoPreview}" />
</c:if>
</div><div class="col-md-4">
<h2>Layout</h2>
<div class="checkbox">	
	<label><input type="checkbox" name="loginMenu" ${currentContext.templateData.loginMenu?'checked="checked"':""} />
	${i18n.edit['admin.template.login-menu']}</label>
</div>
<div class="checkbox">	
	<label><input type="checkbox" name="fixMenu" ${currentContext.templateData.fixMenu?'checked="checked"':""} />
	${i18n.edit['admin.template.fix-menu']}</label>
</div>
<div class="checkbox">	
	<label><input type="checkbox" name="largeMenu" ${currentContext.templateData.largeMenu?'checked="checked"':""} />
	${i18n.edit['admin.template.large-menu']}</label>
</div>
<div class="checkbox">	
	<label><input type="checkbox" name="dropdownMenu" ${currentContext.templateData.dropdownMenu?'checked="checked"':""} />
	${i18n.edit['admin.template.dropdown-menu']}</label>
</div>
<div class="checkbox">	
	<label><input type="checkbox" name="searchMenu" id="searchMenu" ${currentContext.templateData.searchMenu?'checked="checked"':""} onchange="document.getElementById('jssearchMenu').checked=false;" />
	${i18n.edit['admin.template.search-menu']}</label>
</div>
<div class="checkbox">	
	<label><input type="checkbox" name="jssearchMenu" id="jssearchMenu" ${currentContext.templateData.jssearchMenu?'checked="checked"':""} onchange="document.getElementById('searchMenu').checked=false;" />
	${i18n.edit['admin.template.jssearch-menu']}</label>
</div>
<div class="checkbox">	
	<label><input type="checkbox" name="large" id="large-content" ${currentContext.templateData.large?'checked="checked"':""} onchange="document.getElementById('small-content').checked=false;" />
	${i18n.edit['admin.template.large']}</label>
</div>
<div class="checkbox">	
	<label><input type="checkbox" name="small" id="small-content" ${currentContext.templateData.small?'checked="checked"':""} onchange="document.getElementById('large-content').checked=false;" />
	${i18n.edit['admin.template.small']}</label>
</div>
<div class="checkbox">	
	<label><input type="checkbox" name="fixSidebar" ${currentContext.templateData.fixSidebar?'checked="checked"':""} />
	${i18n.edit['admin.template.fix-sidebar']}</label>
</div>
</div></div>

<script>
	var fontRef = new Array();
	<c:forEach var="font" items="${fonts}">fontRef['${font}'] = '${fontsMap[font]}';
	</c:forEach>
</script>

<div class="row">
<c:if test="${info.admin || not empty contentContext.currentTemplate.templateData['fontHeading']}">
<div class="col-sm-6">
<div class="fonts">
	<h2>${i18n.edit['admin.title.font.heading']}</h2>
	<script>
		function updateHeadingFont(fontName) {
			jQuery('body').append(fontRef[fontName]);
			jQuery('#heading-exemple').attr('style', 'font-family:'+fontName)
		}
		document.addEventListener("DOMContentLoaded", function(event) { 
			updateHeadingFont('${currentContext.templateData.fontHeading}');
			updateTextFont('${currentContext.templateData.fontText}');
		});
	</script>

	<a class="nav" href="#" onclick="jQuery('#heading-font>option:selected').prop('selected',false).prev().prop('selected',true);updateHeadingFont(jQuery('#heading-font option:selected').text());return false;" title="${i18n.edit['global.previous']}"><i class="fa fa-arrow-circle-left" aria-hidden="true"></i></a>
	<select id="heading-font" name="fontHeading" onchange="updateHeadingFont(jQuery('#heading-font option:selected').text());">
		<option></option>
		<c:forEach var="font" items="${fonts}"><option ${currentContext.templateData.fontHeading == font?'selected="selected"':''}>${font}</option></c:forEach>
	</select>
	<a class="nav" href="#" onclick="jQuery('#heading-font>option:selected').prop('selected',false).next().prop('selected',true);updateHeadingFont(jQuery('#heading-font option:selected').text());return false;" title="${i18n.edit['global.next']}"><i class="fa fa-arrow-circle-right" aria-hidden="true"></i></a>
	
	<div class="exemple" id="heading-exemple">
		<p>0123456789</p>
		<p>abcdefghijklmnopqrstuvwxyz</p>
		<p>ABCDEFGHIJKLMNOPQRSTUVWXYZ</p>
		<p>ιθηΰλδοφω</p>
	</div>
</div></div></c:if>
<c:if test="${info.admin || not empty contentContext.currentTemplate.templateData['fontText']}">
<div class="col-sm-6">
<div class="fonts">
	<h2>${i18n.edit['admin.title.font.text']}</h2>
	<script>
		function updateTextFont(fontName) {
			jQuery('body').append(fontRef[fontName]);
			jQuery('#text-exemple').attr('style', 'font-family:'+fontName)
		}
	</script>
	
	<a class="nav" href="#" onclick="jQuery('#text-font>option:selected').prop('selected',false).prev().prop('selected',true);updateTextFont(jQuery('#text-font option:selected').text());return false;" title="${i18n.edit['global.previous']}"><i class="fa fa-arrow-circle-left" aria-hidden="true"></i></a>
	<select id="text-font" name="fontText" onchange="updateTextFont(jQuery('#text-font option:selected').text());">
		<option></option>
		<c:forEach var="font" items="${fonts}"><option ${currentContext.templateData.fontText == font?'selected="selected"':''}>${font}</option></c:forEach>
	</select>
	<a class="nav" href="#" onclick="jQuery('#text-font>option:selected').prop('selected',false).next().prop('selected',true);updateTextFont(jQuery('#text-font option:selected').text());return false;" title="${i18n.edit['global.next']}"><i class="fa fa-arrow-circle-right" aria-hidden="true"></i></a>

	
	<div class="exemple" id="text-exemple">
		<p>0123456789</p>
		<p>abcdefghijklmnopqrstuvwxyz</p>
		<p>ABCDEFGHIJKLMNOPQRSTUVWXYZ</p>
		<p>ιθηΰλδοφω</p>
	</div>
</div></div></c:if>
</div>
</fieldset>