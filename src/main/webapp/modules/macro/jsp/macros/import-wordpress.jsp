<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<fieldset>
<legend>Import WordPress</legend>

<form method="post" action="${info.currentURL}" class="standard-form js-change-submit" enctype="multipart/form-data">
<input type="hidden" name="webaction" value="macro-import-wordpress.import" />
<input type="hidden" name="name" value="${page.name}" />

<div class="line">
    <label for="host">Host</label>
    <input class="action-button" type="text" name="host" placeholder="host" id="host" />
</div>

<div class="line">
    <label for="image">Main image css path</label>
    <input class="action-button" type="text" name="image" placeholder="#content img" id="image" />
</div>

<div class="line">
    <label for="file">File</label>
    <input class="action-button" type="file" name="file" id="file" />
</div>

<div class="action">
    <button type="submit">upload</button>
</div>


</fieldset>


