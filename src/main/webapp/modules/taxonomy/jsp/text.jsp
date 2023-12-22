<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<div class="content edit-text">
text 2
<form method="post">
<input type="hidden" name="webaction" value="taxonomy.updatetext" />
<textarea class="form-control" rows="20" name="text">${text}</textarea>
<br />
<button class="btn btn-primary pull-right" type="submit">update</button>

</form>
</div>