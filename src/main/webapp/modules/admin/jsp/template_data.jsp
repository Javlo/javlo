<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div class="row">	
		<div class="col-xs-4">
		<label for="${param.name}">${param.name}</label>
		</div><div class="col-xs-8">	
			<input class="form-control color" type="text" name="${param.name}" value="${param.value}" size="7" maxlength="7" />
		</div>	
	</div>