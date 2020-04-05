<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">
	
	<form action="${info.currentURL}">
		<input type="hidden" name="page" value="sitelog" />
		<input type="hidden" name="module" value="monitoring" />		
		log active : <select name="sitelog" onchange="this.form.submit()" >
			<option>false</option>
			<option ${logActive?'selected="selected"':''}>true</option>			
		</select>
		stack : <select name="sitelogstack" onchange="this.form.submit()" >
			<option>false</option>
			<option ${logActive?'selected="selected"':''}>true</option>			
		</select>
		<button type="submit"><i class="fa fa-refresh" aria-hidden="true"></i></button>
	
	
	<hr />

	<c:if test="${!logActive}"></form></c:if>

	<c:if test="${logActive}">
	
	<div class="row">
		<div class="col-md-4">
		<div class="filter">
			filter :		
			<input type="text" name="filter" id="filter" value="${param.filter}" onkeydown="updateList()" />	
		</div>
		</div><div class="col-md-4">
		<div class="groups">
			groups :		
			<c:forEach var="group" items="${sitelogGroups}">
				<input id="group-${group}" type="checkbox" name="activeGroups" value="${group}" checked="checked" onchange="updateList()" /> <label for="group-${group}">${group}</label>
			</c:forEach>	
		</div>
		</div><div class="col-md-4">
		<div class="levels">
			levels :		
			<c:forEach var="level" items="${sitelogLevels}">
				<input id="level-${level}" type="checkbox" name="activeLevel" value="${level}" checked="checked" onchange="updateList()" /> <label for="level-${level}">${level}</label>
			</c:forEach>	
		</div>
		</div>
	</div>

	</form>
	
	<br/>
	
	<script>
		function updateList() {
			document.querySelectorAll(".log-line").forEach(function(log) {
				log.classList.remove("hidden");
			});
			document.querySelectorAll(".groups input").forEach(function (item) {
				if (!item.checked) {					
					document.querySelectorAll(".log-line."+item.id).forEach(function(log) {
						log.classList.add("hidden");
					});
				}
			});
			document.querySelectorAll(".levels input").forEach(function (item) {
				if (!item.checked) {					
					document.querySelectorAll(".log-line."+item.id).forEach(function(log) {
						log.classList.add("hidden");
					});
				}
			});
			if (document.getElementById('filter').value.length > 0) {
				let filter = document.getElementById('filter').value.toLowerCase();
				document.querySelectorAll(".log-line").forEach(function(log) {
					if (!log.classList.contains('hidden') && log.querySelector('.log-text').innerHTML.toLowerCase().indexOf(filter)<0) {
						log.classList.add("hidden");
					}
				});
			}
			
		}
	</script>

	<div class="log-content">
		<jsp:include page="logsite_content.jsp" />
	</div>

	</c:if>
</div>
