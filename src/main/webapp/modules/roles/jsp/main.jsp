<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="content" class="content ${not empty lightInterface?'light':''}">
	<h2>${rolesContext.role}</h2>
	<form id="roles-form" action="${info.currentURL}" method="get">
		<div>
			<input type="hidden" name="webaction" value="roles.update" />
			<input type="hidden" name="role" value="${role.name}" />
		</div>
		<div class="form-group">
			<label for="mailingSenders">${i18n.edit['role.senders']}</label>
			<input class="form-control" type="text" id="mailingSenders" name="mailingSenders" value="${role.mailingSenders}" />
		</div>
		
		<div class="panel panel-default">
		<div class="panel-heading">Template</div>
		<div class="panel-body">
		<table class="table table-bordered templates">
			<thead>
				<tr>
					<th>Inherited <button type="submit" name="templateAction" value="inherited" class="btn btn-default btn-xs pull-right">move selection here</button></th>
					<th>Included <button type="submit" name="templateAction" value="included" class="btn btn-default btn-xs pull-right">move selection here</button></th>
					<th>Excluded <button type="submit" name="templateAction" value="excluded" class="btn btn-default btn-xs pull-right">move selection here</button></th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>
						<c:forEach var="template" items="${templateInherited}">
							<div class="input-group">
	      						<span class="input-group-addon">
	        						<input type="checkbox" name="templateSelected" id="${template}" value="${template}" />
	      						</span>
	      						<label for="${template}" class="form-control">${template}</label>
	    					</div>
						</c:forEach>					
					</td>
					<td>
						<c:forEach var="template" items="${templateIncluded}">
							<div class="input-group">
      							<span class="input-group-addon">
        							<input type="checkbox" name="templateSelected" id="${template}" value="${template}" />
      							</span>
	      						<label for="${template}" class="form-control">${template}</label>
    						</div>
						</c:forEach>
					</td>
					<td>
						<c:forEach var="template" items="${templateExcluded}">
							<div class="input-group">
      							<span class="input-group-addon">
        							<input type="checkbox" name="templateSelected" id="${template}" value="${template}" />
      							</span>
	      						<label for="${template}" class="form-control">${template}</label>
    						</div>
						</c:forEach>
					</td>
				</tr>
			</tbody>
		</table>
		</div>
		</div>
		<input type="submit" class="btn btn-primary pull-right" />
	</form>
</div>