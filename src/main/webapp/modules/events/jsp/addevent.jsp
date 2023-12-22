<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<div id="content" class="content ${not empty lightInterface?'light':''}">
	<form action="${info.currentURL}" method="post">
		<div>
			<input type="hidden" name="webaction" value="event.addical" />
			<input type="hidden" name="calendar" value="true" />
		</div>
		<div class="form-group"><label for="startdate">start date</label><input class="form-control date" type="date" placeholder="start date" name="startdate" id="startdate" /></div>
		<div class="form-group"><label for="enddate">end date</label><input class="form-control date" type="date" placeholder="end date" name="enddate" id="enddate" /></div>
		<div class="form-group"><label for="summary">summary</label><input class="form-control" type="text" placeholder="summary" name="summary" id="summary" /></div>
		<c:if test="${not empty list.eventcategories}">
		<div class="form-group"><label for="categories">category</label>
			<select id="categories" name="categories" class="form-control">
				<option></option>
				<c:forEach var="cat" items="${list.eventcategories}">
					<option value="${cat.key}">${cat.value}</option>
				</c:forEach>
			</select>
		</div></c:if>		
		<button type="submit" class="btn btn-primary pull-right">${i18n.edit['global.ok']}</button>
	</form>
	
	<hr />
	
	<form action="${info.currentURL}" method="post">
		<div>
			<input type="hidden" name="webaction" value="event.addicalurl" />
			<input type="hidden" name="calendar" value="true" />
		</div>
		<div class="form-group">
			<label for="url-list">ical provider (format:[cateogry]=[url])</label>
			<textarea id="url-list" rows="12" name="urllist" class="form-control">${icalproviders}</textarea>
		</div>
		<button type="submit" class="btn btn-primary pull-right">${i18n.edit['global.ok']}</button>
	</form>
</div>
