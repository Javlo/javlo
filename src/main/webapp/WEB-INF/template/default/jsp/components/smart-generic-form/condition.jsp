<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><script>
	function form${compid}Change() {
		var form = document.getElementById('form-${compid}');		
		<c:forEach var="field" items="${comp.fields}"><c:if test="${not empty field.conditionField}">
		    var value = form['${field.conditionField}'].checked;
		    if (value == undefined) {
		    	value = form['${field.conditionField}'].value;
		    }		    
			if (value == ${field.conditionTest}) {
				form['${field.name}'].className = form['${field.name}'].className.replace("hidden", "visible");
			} else {
				form['${field.name}'].className = form['${field.name}'].className.replace("visible", "hidden");
			}			
		</c:if></c:forEach>
	}
	var form = document.getElementById('form-${compid}');	
	<c:forEach var="field" items="${comp.fields}"><c:if test="${not empty field.conditionField}">
	  var value = form['${field.conditionField}'].checked;
	    if (value == undefined) {
	    	value = form['${field.conditionField}'].value;
	    }
	if (value == ${field.conditionTest}) {		
		form['${field.name}'].className = form['${field.name}'].className + " visible";
	} else {		
		form['${field.name}'].className = form['${field.name}'].className + " hidden";
	}
	</c:if></c:forEach>		
</script>