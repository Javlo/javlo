<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">
text
<form>
<input type="hidden" name="webaction" value="taxonomy.update" />
<textarea class="form-control" rows="20" name="text">${text}</textarea>
<br />
<button class="btn btn-primary pull-right" type="submit">update</button>

</form>
</div>