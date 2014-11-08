<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><form method="post" action="${info.currentURL}" class="standard-form">
<fieldset>
<input type="hidden" name="webaction" value="macro-create-children.create" />
<legend>Create children</legend>
<textarea name="children"></textarea>
<input type="submit" >
</form>
