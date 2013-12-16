<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form method="post" action="${info.currentURL}" class="standard-form" enctype="multipart/form-data">
<fieldset>
<legend>Content importation</legend>
<input type="hidden" name="webaction" value="macro-smart-import.upload" />

<div class="cols">
<div class="one_half">

<div class="line">
	<label for="file">files</label>
	<input id="files" name="files" type="file" multiple="multiple" />
</div>

</div><div class="one_half last">

<div class="line">
	<label for="url">file url</label>
	<input id="text" name="url" type="text"  />
</div>

</div>
</div>


<div class="cols">
<div class="one_half">
	<fieldset>
	<legend>content position : </legend>
	<div class="line">
		<label for="area">area</label>
		<select id="area" name="area">
			<c:forEach var="area" items="${info.template.areas}">
				<option ${area == "content"?'selected="selected"':''}>${area}</option>
			</c:forEach>
		</select>
	</div><div class="line"> 
		<label for="position">position</label>
		<select id="position" name="position">
			<option value="before">before current content</option>			
			<option value="after">after current content</option>
		</select>
	</div>
	</fieldset>
</div>
<div class="one_half last">
<fieldset>
	<legend>import image(s) as : </legend>
	<div class="line"><input type="radio" name="image" id="auto" value="auto" checked="checked" /><label for="auto">auto</label></div>
	<div class="line"><input type="radio" name="image" id="image" value="image" /><label for="image">image</label></div>
	<div class="line"><input type="radio" name="image" id="gallery" value="gallery" /><label for="gallery">gallery</label></div>
</fieldset>
</div>
</div>


<div class="action">
	<input type="submit" value="import" />
</div>

</fieldset>
</form>


