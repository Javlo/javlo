<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><form class="standard-form" id="form-area-${part.name}" action="${info.currentEditURL}" method="post" ${not empty param.upload?'enctype="multipart/form-data"':''}>
	<fieldset>
		<legend>
		<c:if test="${param.editName}">
			<input type="text" name="name" value="${part.name}" />
		</c:if>
		<c:if test="${not param.editName}">
			${part.name}
		</c:if>
		</legend>
		<input type="hidden" name="webaction" value="${param.webaction}" />
		<c:if test="${param.parent}">
		<div class="line">
			<label for="parent-${part.name}">parent :</label>
			<select name="parent" id="parent-${part.name}">			
			<c:forEach var="currentTemplate" items="${parentTemplates}"><option ${template.parent == currentTemplate?' selected="selected"':''}>${currentTemplate}</option></c:forEach>			
			</select>
		</div>		
		</c:if>
		<div class="cols">
		<div class="one_half">				
		<div class="line ${not empty exclude.width?' disabled':''}">
			<label for="width-${part.name}">width <c:if test="${not empty part.finalWidth}"> (${part.finalWidth})</c:if></label>
			<input type="text" id="width-${part.name}" name="width" value="${part.width}" />
		</div>
		<div class="line ${not empty exclude.margin?' disabled':''}">
			<label for="margin-${part.name}">margin</label>
			<input type="text" id="margin-${part.name}" name="margin" value="${part.margin}" />
		</div>			
		<div class="line${not empty exclude.margin?' textSize':''}">
			<label for="textSize-${part.name}">text size <c:if test="${not empty part.finalTextSize}"> (${part.finalTextSize})</c:if></label>
			<input type="text" id="textSize-${part.name}" name="textSize" value="${part.textSize}" />
		</div>
		<div class="line ${not empty exclude.backgroundColor?' disabled':''}">
			<label for="backgroundColor-${part.name}">background color</label>
			<input class="color" type="text" id="backgroundColor-${part.name}" name="backgroundColor" value="${part.backgroundColor}" />
		</div>
		<div class="line ${not empty exclude.linkColor?' disabled':''}">
			<label for="linkColor-${link.name}">link color<c:if test="${not empty part.finalLinkColor}"> (${part.finalLinkColor})</c:if></label>
			<input class="color" type="text" id="linkColor-${part.name}" name="linkColor" value="${part.linkColor}" />
		</div>
		</div>
		<div class="one_half last">	
		<div class="line${not empty exclude.titleColor?' disabled':''}">
			<label for="titleColor-${part.name}">title color<c:if test="${not empty part.finalTitleColor}"> (${part.finalTitleColor})</c:if></label>
			<input class="color" type="text" id="titleColor-${part.name}" name="titleColor" value="${part.titleColor}" />
		</div>		
		<div class="line${not empty exclude.padding?' disabled':''}">
			<label for="padding-${part.name}">padding <c:if test="${not empty part.finalPadding}"> (${part.finalPadding})</c:if></label>
			<input type="text" id="padding-${part.name}" name="padding" value="${part.padding}" />
		</div>
		<div class="line${not empty exclude.textColor?' disabled':''}">
			<label for="textColor-${part.name}">text color<c:if test="${not empty part.finalTextColor}"> (${part.finalTextColor})</c:if></label>
			<input class="color" type="text" id="textColor-${part.name}" name="textColor" value="${part.textColor}" />
		</div>
		<fieldset>	
		<legend>	
		<div class="checkline${not empty exclude.borderWidth?' disabled':''}">
			<label for="borderWidth-${part.name}">border</label>
			<input type="checkbox" id="borderWidth-${part.name}" name="borderWidth" value="1px" ${not empty part.borderWidth?'checked="checked"':''}/>			
		</div>
		</legend>
		<div class="line${not empty exclude.borderColor?' disabled':''}">
			<label for="borderColor-${part.name}">border color</label>
			<input class="color" type="text" id="borderColor-${part.name}" name="borderColor" value="${part.borderColor}" />
		</div>
		</fieldset>
		</div>
		</div>
		<fieldset class="title-size" class="cols">
			<legend>title size</legend>
			<div class="one_half">
			<div class="line${not empty exclude.finalH1Size?' disabled':''}">
				<label for="h1size-${part.name}">h1 size<c:if test="${not empty part.finalH1Size}"> (${part.finalH1Size})</c:if></label>
				<input type="text" id="textColor-${part.name}" name="h1size" value="${part.h1Size}" />
			</div>
			<div class="line${not empty exclude.finalH2Size?' disabled':''}">
				<label for="h1size-${part.name}">h2 size<c:if test="${not empty part.finalH2Size}"> (${part.finalH2Size})</c:if></label>
				<input type="text" id="textColor-${part.name}" name="h2size" value="${part.h2Size}" />
			</div>
			<div class="line${not empty exclude.finalH3Size?' disabled':''}">
				<label for="h3size-${part.name}">h3 size<c:if test="${not empty part.finalH3Size}"> (${part.finalH3Size})</c:if></label>
				<input type="text" id="textColor-${part.name}" name="h3size" value="${part.h3Size}" />
			</div>
			</div>	
			<div class="one_half last">				
			<div class="line${not empty exclude.finalH4Size?' disabled':''}">
				<label for="h1size-${part.name}">h4 size<c:if test="${not empty part.finalH4Size}"> (${part.finalH4Size})</c:if></label>
				<input type="text" id="textColor-${part.name}" name="h4size" value="${part.h4Size}" />
			</div>
			<div class="line${not empty exclude.finalH5Size?' disabled':''}">
				<label for="h1size-${part.name}">h5 size<c:if test="${not empty part.finalH5Size}"> (${part.finalH5Size})</c:if></label>
				<input type="text" id="textColor-${part.name}" name="h5size" value="${part.h5Size}" />
			</div>
			<div class="line${not empty exclude.finalH6Size?' disabled':''}">
				<label for="h1size-${part.name}">h6 size<c:if test="${not empty part.finalH6Size}"> (${part.finalH6Size})</c:if></label>
				<input type="text" id="textColor-${part.name}" name="h6size" value="${part.h6Size}" />
			</div>
			</div>
		</fieldset>
		<div class="line${not empty exclude.font?' disabled':''}">
			<label for="font-${part.name}">font<c:if test="${not empty part.finalFont}"> (${part.finalFont})</c:if></label>
			<select name="font" id="font-${part.name}">
			<option></option>
			<c:forEach var="font" items="${fonts}"><option style="font-family: ${font};"${part.font == font?' selected="selected"':''}>${font}</option></c:forEach>			
			</select>			
		</div>
		<c:if test="${not empty param.upload}">
		<div class="line">
			<label for="image-${part.name}">preview image</label>
			<input type="file" id="image-${part.name}" name="image" />
		</div>
		<div class="preview">
			<img src="${template.previewUrl}?random-id=${info.randomId}" alt="template preview" />
		</div>
		</c:if>
		
		<div class="action">
			<input type="submit" value="ok" />
			<c:if test="${not empty param.delete}">
			<input class="needconfirm" type="submit" name="delete" value="delete" />
			</c:if>
		</div>

	</fieldset>
</form>