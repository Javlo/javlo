<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div id="meta-edit" class="form-list">

<form id="form-meta" action="${info.currentURL}" method="post">

<input type="hidden" name="webaction" value="file.updateMeta" />

<c:if test="${not empty param.select}">
	<input type="hidden" name="select" value="${param.select}" />	
</c:if>
<c:if test="${not empty param.close || not empty param.one}">
	<input type="hidden" name="close" value="${empty param.one?param.close:'true'}" />
</c:if>
<c:if test="${not empty param[BACK_PARAM_NAME]}"><input type="hidden" name="${BACK_PARAM_NAME}" value="${param[BACK_PARAM_NAME]}" /></c:if>

<ul>
<c:forEach var="file" items="${files}">
	<c:url var="fileURL" value="${file.URL}" context="/">
		<c:if test="${not empty param.select}">
			<c:param name="select" value="${param.select}"></c:param>
		</c:if>
		<c:if test="${not empty param[BACK_PARAM_NAME]}">
			<c:param name="${BACK_PARAM_NAME}" value="${param[BACK_PARAM_NAME]}" />
		</c:if>
	</c:url>
	<li class="${file.directory?'directory':'file'} ${not empty param.select?'small':'no-small'} ${!file.directory && not empty param.select?'select':'no-select'} unlock item ${not empty param.one?'one':''}">
		<c:if test="${param.select != 'image' || file.image || file.directory}">
	    <c:set var="popularity" value=" - #${file.popularity}" />	    
		<c:if test="${empty param.select}">
		<div class="title">		
			<c:if test="${empty param.one}">
			<a class="lock" href="#" onclick="var list=jQuery(this).parent().parent();list.removeClass('lock'); list.addClass('unlock'); return false;"><span class="glyphicon glyphicon-lock"></span></a>
			<a class="unlock" href="#" onclick="var list=jQuery(this).parent().parent();list.removeClass('unlock'); list.addClass('lock'); return false;"><span class="glyphicon glyphicon-link"></span></a>
			</c:if>
			<span class="filename"><a href="${fileURL}" title="${file.name}">${file.name}</a></span>
			<c:if test="${empty param.select}">
				<c:url value="${info.currentURL}" var="deleteURL" context="/">
					<c:param name="webaction" value="file.delete" />
					<c:param name="module" value="file" />
					<c:param name="file" value="${file.path}" />
					<c:if test="${not empty param['select']}"><c:param name="select" value="true" /></c:if>
					<c:if test="${not empty param[BACK_PARAM_NAME]}">
						<c:param name="${BACK_PARAM_NAME}" value="${param[BACK_PARAM_NAME]}" />
					</c:if>
					<c:if test="${param.nobreadcrumbs}">
						<c:param name="close" value="true" />
					</c:if>
				</c:url>							
				<span class="delete"><a class="needconfirm" href="${deleteURL}"><span class="glyphicon glyphicon-trash last"></span></a></span>
			</c:if>
			<span class="size">${file.size} <span class="popularity">${info.admin?popularity:''}</span></span>
			<span class="last">${file.manType}</span>
		</div>
		</c:if>		
		<div class="body">
		
		<div class="download ${file.image?'picture':''}">
			<div ${file.image?'class="focus-zone"':'no-focus'} >			
			<c:url var="fileSelectURL" value="${file.URL}">
				<c:if test="${not empty param.select}"><c:param name="select" value="true" /></c:if>
			</c:url>
			<c:set var="dataURL" value="" />
			<c:if test="${not empty param.select && !file.directory}">
				<c:set var="dataURL" value='data-url="${file.freeURL}"' />
			</c:if>				
			<a ${!file.directory && not empty param.select?'class="select-item"':''} href="${fileSelectURL}" ${dataURL}><img src="${file.thumbURL}" />&nbsp;</a>			
			<c:if test="${file.image}">
			<div class="focus-point">x</div>			
			<input class="posx" type="hidden" name="posx-${file.id}" value="${file.focusZoneX}" />
			<input class="posy" type="hidden" name="posy-${file.id}" value="${file.focusZoneY}" />				
			</c:if>
			<div class="label">
				<a href="${fileSelectURL}" ${dataURL}>
				<span>${file.name}</span>
				</a>
			</div>
			</div>
		</div>
		
		
		<c:if test="${empty param.select}">
		<div class="line">
			<label for="title-${file.id}">${i18n.edit["field.title"]}</label>			
			<input class="file-title" type="text" id="title-${file.id}" name="title-${file.id}" value="<c:out value="${file.title}" escapeXml="true" />" />
		</div>
		<div class="line">
			<label for="description-${file.id}">${i18n.edit["field.description"]}</label>
			<textarea class="file-description" id="description-${file.id}" name="description-${file.id}" rows="5" cols="10">${file.description}</textarea>
		</div>
		<div class="line">
			<label for="location-${file.id}">${i18n.edit["field.location"]} <c:if test="${not empty file.position}">
			<a class="set-location" title="get location from image" href="#" onclick="loadLocalisation('${info.currentAjaxURL}', ${file.position.longitude}, ${file.position.latitude},'${info.language}', '#location-${file.id}'); return false;">
				<span class="glyphicon glyphicon-map-marker"></span>
			</a>
			</c:if></label>
			<input class="file-location" type="text" id="location-${file.id}" name="location-${file.id}" value="<c:out value="${file.location}" escapeXml="true" />" />			
		</div>
		<div class="line">
			<label for="copyright-${file.id}">${i18n.edit["field.copyright"]}</label>
			<input class="file-copyright" type="text" id="copyright-${file.id}" name="copyright-${file.id}" value="<c:out value="${file.copyright}" escapeXml="true" />" />
		</div>
		<div class="line">
			<label for="date-${file.id}">${i18n.edit["field.date"]}</label>
			<input class="file-date" type="text" id="date-${file.id}" name="date-${file.id}" value="${file.manualDate}" />
		</div>
		<div class="line">
			<div class="label">${i18n.edit["field.file-date"]}</div>
			<div class="value">${file.date}</div>
		</div>
		<div class="line">
			<div class="label">${i18n.edit["field.creation-date"]}</div>
			<div class="value">${file.creationDate}</div>
		</div>
		<div class="line">
			<label for="shared-${file.id}">${i18n.edit["field.shared"]}</label>
			<input type="checkbox" id="shared-${file.id}" name="shared-${file.id}" ${file.shared?'checked="checked"':''} />
		</div>				
						
		<c:if test="${fn:length(tags) > 0}">
		<fieldset class="tags">
		<legend>${i18n.edit["field.tags"]}</legend>
		    <c:forEach var="tag" items="${tags}">		    	
				<span><input class="tag-${tag} tag" type="checkbox" id="tag_${tag}_${file.id}" name="tag_${tag}_${file.id}" ${not empty file.tags[tag]?'checked="checked"':''}/><label for="tag_${tag}_${file.id}">${tag}</label></span>
			</c:forEach>
		</fieldset>
		</c:if>
		<c:if test="${fn:length(readRoles) > 0}">
		<fieldset class="roles">
		<legend>${i18n.edit["field.read-roles"]}</legend>
		    <c:forEach var="role" items="${readRoles}">		    	
				<span><input class="role-${role}" type="checkbox" id="readrole_${role}_${file.id}" name="readrole_${role}_${file.id}" ${not empty file.readRoles[role]?'checked="checked"':''}/><label for="readrole_${role}_${file.id}">${role}</label></span>
			</c:forEach>
		</fieldset>
		</c:if>
		</c:if>
		</div>
		</c:if>
	</li>
</c:forEach>
</ul>

<div class="actions">
	<input class="action-button" type="submit" value="${i18n.edit['global.save']}"/> 
</div>

</form>

</div>

<c:if test="${not empty param.select}">
	<script type="text/javascript">
		jQuery(".select-item").click(function() {
			if (parent.tinyMCE !== undefined) {
				var fieldName = parent.jQuery("body").data("fieldName");
				var url = jQuery(this).data("url");
				parent.jQuery("#"+fieldName).val(url);
				parent.tinyMCE.activeEditor.windowManager.close(window);
			} else {
				
			}
		});
	</script>	
</c:if>
