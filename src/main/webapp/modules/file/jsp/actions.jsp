<%@ taglib uri="jakarta.tags.core" prefix="c"
%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"
%>
<div class="part-1">
<c:if test="${fn:length(info.contentLanguages) > 1 and empty param.previewEdit && empty param.templateid}">
    <form id="form-languages" action="${info.currentURL}" method="get" class="js-submit">
        <div class="select-languages form_default">
            <input type="hidden" name="webaction" value="file.changeLanguage"/>
            <c:if test="${info.admin && globalContext.master}">
                <c:url var="createStructureFile" value="${info.currentURL}" context="/">
                    <c:param name="webaction" value="createfilestructure"/>
                </c:url>
                <a class="action-button" title="create file structure" href="${createStructureFile}">file struc.</a>
            </c:if>
            <c:if test="${not empty param[BACK_PARAM_NAME]}"><input type="hidden" name="${BACK_PARAM_NAME}"
                                                                    value="${param[BACK_PARAM_NAME]}"/></c:if>
            <c:if test="${not empty param['select']}"><input type="hidden" name="select" value="true"/></c:if>
            <select name="language">
                <c:forEach var="lang" items="${info.contentLanguages}">
                    <option value="${lang}"${lang eq info.contentLanguage?' selected="selected"':''}>${lang}</option>
                </c:forEach>
            </select>
                <%-- 	<input class="action-button" type="submit" name="ok" value="${i18n.edit['global.ok']}" /> --%>
        </div>
    </form>
    <a class="action-button notuse" href="${info.currentURL}?use=true"
       onclick=""><span>${i18n.edit['action.notuse']}</span></a>
</c:if>
<c:if test="${not empty param[BACK_PARAM_NAME]}"><a class="action-button back"
                                                    href="${param[BACK_PARAM_NAME]}&path=${param.path}&backreturn=true"><span>${i18n.edit['action.back']}</span></a></c:if>
<c:if test="${empty param.templateid && empty param.select}"><a class="action-button save" href="#save"
                                                                onclick="jQuery('#form-meta').submit(); return false;"><span>${i18n.edit['action.update']}</span></a></c:if>
<c:if test="${not empty globalContext.DMZServerInter}">
    <c:url var="synchroURL" value="${info.currentURL}">
        <c:param name="webaction" value="file.synchro"/>
    </c:url>
    <a class="action-button synchro" href="${synchroURL}"><span>${i18n.edit['action.synchro']}</span></a></c:if>
<c:url var="uploadCurrentURL" value="${info.currentURL}" context="/"/>
<c:url var="uploadJSPURL" value="${info.absoluteLocalURLPrefix}${currentModule.path}/jsp/upload.jsp">
    <c:param name="currentURL" value="${uploadCurrentURL}"/>
    <c:if test="${not empty param[BACK_PARAM_NAME]}"><c:param name="${BACK_PARAM_NAME}"
                                                              value="${param[BACK_PARAM_NAME]}"/></c:if>
    <c:if test="${not empty param['select']}"><c:param name="select" value="true"/></c:if>
</c:url>
<a href="${uploadJSPURL}" class="popup cboxElement action-button"><span>${i18n.edit['action.add-files']}</span></a>
<c:if test="${not empty param.templateid}">
    <a class="action-button ajax"
       href="${info.currentURL}?webaction=template.commit&webaction=file.browse&templateid=${param.templateid}&from-module=template"><span>${i18n.edit['template.action.commit']}</span></a>
    <a class="action-button ajax"
       href="${info.currentURL}?webaction=template.commitChildren&webaction=file.browse&templateid=${param.templateid}&from-module=template"><span>${i18n.edit['template.action.commit-children']}</span></a>

    <a class="action-button"
       href="${info.currentURL}?webaction=template.goEditTemplate&templateid=${param.templateid}&module=template">back
        edit template</a>

</c:if>

<form id="form-sorted" action="${info.currentURL}" method="get" class="js-submit">
    <input type="hidden" name="webaction" value="file.order"/>
    <c:if test="${not empty param[BACK_PARAM_NAME]}">
        <input type="hidden" name="${BACK_PARAM_NAME}" value="${param[BACK_PARAM_NAME]}"/>
    </c:if>
    <c:if test="${not empty param['select']}"><input type="hidden" name="select" value="${param['select']}"/></c:if>
    <select class="action-field" name="order">
        <option value="1"${sort == '1'?' selected="selected"':''}>${i18n.edit['action.sort.date']}</option>
        <option value="2"${sort == '2'?' selected="selected"':''}>${i18n.edit['action.sort.name']}</option>
        <option value="3"${sort == '3'?' selected="selected"':''}>${i18n.edit['action.sort.title']}</option>
        <option value="4"${sort == '4'?' selected="selected"':''}>${i18n.edit['action.sort.creation-date']}</option>
    </select>
</form>

</div>

<c:if test="${empty param.templateid}">
    <input class="action-field filter-field" type="text" name="filter" placeholder="${i18n.edit['global.filter']}"
           onkeyup="filter(this.value, '#form-meta li');"/>
    <c:if test="${empty param.select}">
        <form id="fill-all-form">
            <a title="lock all" href="#"
               onclick="jQuery('li.item').addClass('lock');jQuery('li.item').removeClass('unlock');"><i
                    class="bi bi-lock"></i></span></a>
            <a title="unlock all" href="#"
               onclick="jQuery('li.item').addClass('unlock');jQuery('li.item').removeClass('lock');"><i
                    class="bi bi-link-45deg"></i></a>
            <input type="text" id="allTitle" name="all-title" placeholder="${i18n.edit['action.change-all-title']}"
                   onkeyup="jQuery('.unlock .file-title').each(function(){var t = jQuery(this);if (t.val().length == 0 || t.data('empty') == true) {t.data('empty',true);t.val(jQuery('#allTitle').val())}});"/>
            <input type="text" id="allDescription" name="all-descritpion"
                   placeholder="${i18n.edit['action.change-all-description']}"
                   onkeyup="jQuery('.unlock .file-description').each(function(){var t = jQuery(this);if (t.val().length == 0 || t.data('empty') == true) {t.data('empty',true);t.val(jQuery('#allDescription').val())}});"/>
            <input type="text" name="test" placeholder="TEST"/>
            <a title="set all location" href="#" onclick="jQuery('.set-location').click();">
                <i class="bi bi-geo-alt"></i>
            </a>
            <input type="text" id="allLocation" name="all-location"
                   placeholder="${i18n.edit['action.change-all-location']}"
                   onkeyup="jQuery('.unlock .file-location').each(function(){var t = jQuery(this);if (t.val().length == 0 || t.data('empty') == true) {t.data('empty',true);t.val(jQuery('#allLocation').val())}});"/>
            <input type="text" id="allCopyright" name="all-copyright"
                   placeholder="${i18n.edit['action.change-all-copyright']}"
                   onkeyup="jQuery('.unlock .file-copyright').each(function(){var t = jQuery(this);if (t.val().length == 0 || t.data('empty') == true) {t.data('empty',true);t.val(jQuery('#allCopyright').val())}});"/>
            <input type="text" id="allDate" name="all-date" placeholder="${i18n.edit['action.change-all-date']}"
                   onkeyup="jQuery('.unlock .file-date').each(function(){var t = jQuery(this);if (t.val().length == 0 || t.data('empty') == true) {t.data('empty',true);t.val(jQuery('#allDate').val())}});"/>
            <c:if test="${fn:length(info.tags) > 0}">
                <select id="allTag" name="all-tag" onchange="jQuery('.tag-'+this.value).attr('checked', 'checked');">
                    <option>tag</option>
                    <c:forEach var="tag" items="${info.tags}">
                        <option>${tag}</option>
                    </c:forEach>
                </select>
            </c:if><c:if test="${fn:length(readRoles) > 0}">
            <select id="allRoles" name="all-role" onchange="jQuery('.role-'+this.value).attr('checked', 'checked');">
                <option>role</option>
                <c:forEach var="role" items="${readRoles}">
                    <option>${role}</option>
                </c:forEach>
            </select>
        </c:if>
        </form>
    </c:if>
</c:if>

<%--<c:if test="${not empty importFolder}">
	<c:url var="currentPageFolder" value="${info.currentURL}" context="/">
		<c:param name="path" value="/${importFolder}" />
	</c:url>
	<a href="${currentPageFolder}">page folder</a>
</c:if> --%>
<div class="clear">&nbsp;</div>

