<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><form method="post" action="${info.currentURL}" class="standard-form create-content-children">
<fieldset>
<input type="hidden" name="webaction" value="create-content-children.create" />
<legend>Create new message</legend>
<div class="line">
<label for="title">Title : </label>
<input id="title" type="text" name="title" />
</div><div class="line">
<label for="body">Message : </label>
<textarea id="body" name="body"></textarea>
</div>
<div class="actions">
<button class="btn btn-default pull-right" type="submit" >${i18n.edit['global.create']}</button>
</div>
</fieldset>
</form>
<script>
tinymce.init({
		mode : "specific_textareas",
		theme : "modern",
		convert_urls: false,		
		add_form_submit_trigger: true,	
		menubar : false,
		selector: "#body",
		plugins: "paste link",
		toolbar: "undo redo | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link pastetext"
});
</script>