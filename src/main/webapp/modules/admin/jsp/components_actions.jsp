<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<a class="action-button needconfirm" href="${info.currentURL}?webaction=admin.componentsForAll&context=${param['context']}"><span>${i18n.edit['admin.component.components-to-all']}</span></a>
<a class="action-button" href="${info.currentURL}?webaction=admin.componentsDefault&context=${param['context']}"><span>${i18n.edit['admin.component.import-default']}</span></a>
<div class="clear">&nbsp;</div>

