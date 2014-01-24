<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div class="${contentContext.editPreview?'preview ':'edit '}content wizard">
	<form class="${contentContext.editPreview?'':'ajax '} standard-form" action="${info.currentURL}" method="post">
		<div>
			<input type="hidden" name="webaction" value="wizard" />
			<input type="hidden" name="box" value="sendwizard" />
			<c:if test="${contentContext.editPreview}"><input type="hidden" name="previewEdit" value="true" /></c:if>
		</div>
		<div class="line">
			<label for="mailing-sender">${i18n.edit['mailing.form.sender']}</label>
			<input type="text" id="mailing-sender" name="sender" value="<c:out value="${mailing.sender}" escapeXml="true" />" />
		</div>
		<div class="line">
			<label for="mailing-subject">${i18n.edit['mailing.form.subject']}</label>
			<input type="text" id="mailing-subject" name="subject" value="<c:out value="${mailing.subject}" escapeXml="true" />" />
		</div>
		<div class="line">
			<label for="mailing-report-to">${i18n.edit['mailing.form.report-to']}</label>
			<input type="text" id="mailing-report-to" name="report-to" value="<c:out value="${mailing.reportTo}" escapeXml="true" />" />
		</div>
		<div class="line">
			<label for="mailing-groups">${i18n.edit['mailing.form.groups']}</label>
			<select id="mailing-groups" name="groups" multiple="multiple">
				<c:forEach var="group" items="${groups}">
					<option ${fn:contains(mailing.groups, group)?'selected="selected"':''} value="${group}">${group}</option>
				</c:forEach>
			</select>
		</div>
		<div class="line">
			<label for="mailing-recipients">${i18n.edit['mailing.form.recipients']}</label>
			<textarea id="mailing-recipients" name="recipients"><c:out value="${mailing.recipients}" escapeXml="true" /></textarea>
		</div>
		<div class="inline">		
			<input type="checkbox" id="mailing-test-mailing" name="test-mailing" ${mailing.testMailing?'checked="checked"':''} />
			<label class="suffix" for="mailing-test-mailing">${i18n.edit['mailing.form.test-mailing']}</label>
		</div>
		<div class="action">
			<c:if test="${!contentContext.editPreview}"><input type="submit" name="previous" value="Previous" /></c:if>
			<input type="submit" name="next" value="Next" />
		</div>
	</form>
</div>
