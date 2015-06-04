<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"
%><div class="${contentContext.editPreview?'preview ':'edit '}content wizard">
	<form class="${contentContext.editPreview?'':'ajax '}" action="${info.currentURL}" method="post">
		<div class="main">
			<div>
				<input type="hidden" name="webaction" value="wizard" />
				<input type="hidden" name="box" value="sendwizard" />
				<c:if test="${contentContext.editPreview}"><input type="hidden" name="previewEdit" value="true" /></c:if>
			</div>
			<div class="form-group">
				<label for="mailing-sender">${i18n.edit['mailing.form.sender']}</label>
				<c:if test="${empty senders}"><input type="text" id="mailing-sender" name="sender" class="form-control" value="<c:out value="${mailing.sender}" escapeXml="true" />" /></c:if>
				<c:if test="${not empty senders}"><select id="mailing-sender" name="sender" class="form-control" >
					<c:forEach var="sender" items="${senders}">
						<option ${fn:contains(senders, mailing.sender)?'selected="selected"':''} value="<c:out value="${sender}" escapeXml="true" />"><c:out value="${sender}" escapeXml="true" /></option>
					</c:forEach>
				</select></c:if>
			</div>
			<div class="form-group">
				<label for="mailing-subject">${i18n.edit['mailing.form.subject']}</label>
				<input type="text" id="mailing-subject" name="subject" class="form-control" value="<c:out value="${mailing.subject}" escapeXml="true" />" />
			</div>
			<div class="form-group">
				<label for="mailing-report-to">${i18n.edit['mailing.form.report-to']}</label>
				<input type="text" class="form-control" id="mailing-report-to" name="report-to" value="<c:out value="${mailing.reportTo}" escapeXml="true" />" />
			</div>
			<c:if test="${fn:length(groups)>0}">
			<div class="form-group">
				<label for="mailing-groups">${i18n.edit['mailing.form.groups']}</label>
				<select id="mailing-groups" name="groups" multiple="multiple" class="form-control">
					<c:forEach var="group" items="${groups}">
						<option ${fn:contains(mailing.groups, group)?'selected="selected"':''} value="${group}">${group}</option>
					</c:forEach>
				</select>
			</div>
			</c:if><c:if test="${globalContext.collaborativeMode && fn:length(adminGroups) > 0}">
				<div class="form-group">
					<label for="mailing-admin-groups">${i18n.edit['mailing.form.admin-groups']}</label>
					<select id="mailing-admin-groups" name="admin-groups" multiple="multiple" class="form-control">
						<c:forEach var="group" items="${adminGroups}">
							<option ${fn:contains(mailing.groups, group)?'selected="selected"':''} value="${group}">${group}</option>
						</c:forEach>
					</select>
				</div>				
			</c:if>
			<div class="form-group">
				<label for="mailing-recipients">${i18n.edit['mailing.form.recipients']}</label>
				<textarea id="mailing-recipients" name="recipients" class="form-control"> <c:out value="${mailing.recipients}" escapeXml="true" /></textarea>
			</div>
			<div class="form-group">
				<label for="mailing-structured-recipients">${i18n.edit['mailing.form.structured-recipients']}</label>
				<textarea id="mailing-structured-recipients" name="structuredRecipients" class="form-control"> <c:out value="${mailing.structuredRecipients}" escapeXml="true" /></textarea>
			</div>
			<div class="checkbox">	
				<label class="suffix">	
				<input type="checkbox" id="mailing-test-mailing" name="test-mailing" ${mailing.testMailing?'checked="checked"':''} />
				${i18n.edit['mailing.form.test-mailing']}</label>
			</div>
			<div class="action">
				<c:if test="${!contentContext.editPreview}"><button type="submit" class="btn btn-default pull-right" name="previous">Previous</button></c:if>
				<button class="btn btn-primary btn-color pull-right" type="submit" name="next">next</button>
			</div>
		</div>
	</form>
</div>
