<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div id="meta-edit" class="form-list">

<form id="form-meta" action="${info.currentURL}" method="post">

<input type="hidden" name="webaction" value="file.updateMeta" />
<c:if test="${not empty param.select}">
	<input type="hidden" name="select" value="${param.select}" />	
</c:if>
<c:if test="${not empty param.close}">
	<input type="hidden" name="close" value="${param.close}" />
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
	<li class="${file.directory?'directory':'file'} ${not empty param.select?'select':'no-select'}">
		<c:if test="${param.select != 'image' || file.image || file.directory}">
	    <c:set var="popularity" value=" - #${file.popularity}" />	    
		<div class="title">
			<span class="filename"><a href="${fileURL}" title="${file.name}">${file.name}</a></span>
			<c:if test="${empty param.select}">
				<c:url value="${info.currentURL}" var="deleteURL" context="/">
					<c:param name="webaction" value="file.delete" />
					<c:param name="file" value="${file.path}" />
					<c:if test="${not empty param[BACK_PARAM_NAME]}">
						<c:param name="${BACK_PARAM_NAME}" value="${param[BACK_PARAM_NAME]}" />
					</c:if>
				</c:url>			
				<span class="delete"><a class="needconfirm" href="${deleteURL}">X</a></span>
			</c:if>
			<span class="size">${file.size} <span class="popularity">${info.admin?popularity:''}</span></span>
			<span class="last">${file.manType}</span>
		</div>		
		<div class="body">
		<c:if test="${file.image}">		
		<div class="download picture">
			<div class="focus-zone">
			<c:if test="${empty param.select}">	
			<a class="image" rel="image" href="${file.URL}"><img src="${file.thumbURL}" />&nbsp;</a>
			</c:if>
			<c:if test="${not empty param.select}">	
			<a class="select-item" href="${file.URL}" data-url="${file.freeURL}"><img src="${file.thumbURL}" />&nbsp;</a>
			</c:if>
			<div class="focus-point">x</div>			
			<input class="posx" type="hidden" name="posx-${file.id}" value="${file.focusZoneX}" />
			<input class="posy" type="hidden" name="posy-${file.id}" value="${file.focusZoneY}" />
			</div>	
		</div>
		</c:if>
		<c:if test="${not file.image}">		
			
			<div class="download file ${file.type}">
			<c:if test="${empty param.select || file.type == 'directory'}">	
				<a href="${fileURL}"><span>${file.name}</span></a>
				</c:if>
				<c:if test="${not empty param.select && file.type != 'directory'}">	
				<a class="select-item"  data-url="${fileURL}" href="${fileURL}"><span>${file.name}</span></a>
				</c:if>
			</div>
			
		</c:if>
		<div class="line">
			<label for="title-${file.id}">${i18n.edit["field.title"]}</label>			
			<input class="file-title" type="text" id="title-${file.id}" name="title-${file.id}" value="<c:out value="${file.title}" escapeXml="true" />" />
		</div>
		<div class="line">
			<label for="description-${file.id}">${i18n.edit["field.description"]}</label>
			<textarea class="file-description" id="description-${file.id}" name="description-${file.id}" rows="5" cols="10">${file.description}</textarea>
		</div>
		<div class="line">
			<label for="location-${file.id}">${i18n.edit["field.location"]}</label>
			<input class="file-location" type="text" id="location-${file.id}" name="location-${file.id}" value="<c:out value="${file.location}" escapeXml="true" />" />
		</div>
		<div class="line">
			<label for="date-${file.id}">${i18n.edit["field.date"]}</label>
			<input class="file-date" type="text" id="date-${file.id}" name="date-${file.id}" value="${file.manualDate}" />
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
				<span><input type="checkbox" id="tag_${tag}_${file.id}" name="tag_${tag}_${file.id}" ${not empty file.tags[tag]?'checked="checked"':''}/><label for="tag_${tag}_${file.id}">${tag}</label></span>
			</c:forEach>
		</fieldset>
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
