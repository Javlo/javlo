<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><form class="standard-form" id="form-area-${part.name}" action="${info.currentEditURL}" method="post" ${not empty param.upload?'enctype="multipart/form-data"':''}>
	<fieldset>
		<legend>${part.name}</legend>
		<input type="hidden" name="webaction" value="${param.webaction}" />
		<div class="cols">
		<div class="one_half">		
		<div class="line ${not empty exclude.width?' disabled':''}">
			<label for="width-${part.name}">width</label>
			<input type="text" id="width-${part.name}" name="width" value="${part.width}" />
		</div>
		<div class="line ${not empty exclude.margin?' disabled':''}">
			<label for="margin-${part.name}">margin</label>
			<input type="text" id="margin-${part.name}" name="margin" value="${part.margin}" />
		</div>		
		<div class="line${not empty exclude.borderWidth?' disabled':''}">
			<label for="borderWidth-${part.name}">border width</label>
			<input type="text" id="borderWidth-${part.name}" name="borderWidth" value="${part.borderWidth}" />
		</div>		
		<div class="line${not empty exclude.margin?' textSize':''}">
			<label for="textSize-${part.name}">text size</label>
			<input type="text" id="textSize-${part.name}" name="textSize" value="${part.textSize}" />
		</div>
		<div class="line ${not empty exclude.backgroundColor?' disabled':''}">
			<label for="backgroundColor-${part.name}">background color</label>
			<input class="color" type="text" id="backgroundColor-${part.name}" name="backgroundColor" value="${part.backgroundColor}" />
		</div>
		</div>
		<div class="one_half last">		
		<div class="line${not empty exclude.height?' disabled':''}">
			<label for="height-${part.name}">height</label>
			<input type="text" id="height-${part.name}" name="height" value="${part.height}" />
		</div>		
		<div class="line${not empty exclude.padding?' disabled':''}">
			<label for="padding-${part.name}">padding</label>
			<input type="text" id="padding-${part.name}" name="padding" value="${part.padding}" />
		</div>
		<div class="line${not empty exclude.borderColor?' disabled':''}">
			<label for="borderColor-${part.name}">border color</label>
			<input class="color" type="text" id="borderColor-${part.name}" name="borderColor" value="${part.borderColor}" />
		</div>
		<div class="line${not empty exclude.textColor?' disabled':''}">
			<label for="textColor-${part.name}">text color</label>
			<input class="color" type="text" id="textColor-${part.name}" name="textColor" value="${part.textColor}" />
		</div>		
		</div>
		</div>
		<div class="line${not empty exclude.font?' disabled':''}">
			<label for="font-${part.name}">font</label>
			<select name="font" id="font-${part.name}">
			<option></option>
			<c:forEach var="font" items="${fonts}"><option style="font-family: ${font};"${part.font == font?' selected=selected':''}>${font}</option></c:forEach>			
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