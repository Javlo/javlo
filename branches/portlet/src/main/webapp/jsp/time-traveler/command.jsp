<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html" import="
    	    java.util.Date,
    	    java.util.List,
    	    org.javlo.context.GlobalContext,
    	    org.javlo.context.ContentContext,
    	    org.javlo.service.ContentService,
    	    org.javlo.service.PersistenceService,
    	    org.javlo.helper.XHTMLHelper,
    	    org.javlo.helper.URLHelper,
    	    org.javlo.helper.MacroHelper,
    	    org.javlo.helper.StringHelper,
    	    org.javlo.helper.XHTMLNavigationHelper,
    	    org.javlo.navigation.MenuElement"
%><%
GlobalContext globalContext = GlobalContext.getInstance(request);
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentContext editCtx = new ContentContext(ctx);
editCtx.setRenderMode(ContentContext.EDIT_MODE);
ContentService content = ContentService.getInstance(globalContext);
%>
<div id="preview_command" lang="${info.editLanguage}" class="edit-${not empty currentUser}">
	<div class="pc_header">${i18n.edit["time.command"]}</div>
	<div class="pc_body">
		<c:if test="${not empty currentUser}">
			<form id="pc_logout_form" action="${info.currentURL}" method="post">
				<div class="pc_line">
					<a href="${info.currentURL}?edit-logout=logout">${i18n.edit['login.logout']}</a>
				</div>
			</form>
			<fieldset class="pc_command">
				<div class="pc_form_line">
					<form id="pc_form" action="${info.currentURL}" method="post">
						<div class="pc_line">
							<input type="hidden" name="webaction" value="time.replacecurrentpage" />
							<input id="tc_replace_current_page_button" type="submit" title="${i18n.edit['time.action.replace-current-page']}" class="pc_edit_true" />
						</div>
					</form>
					<form id="pc_form" action="${info.currentURL}" method="post">
						<div class="pc_line">
							<input type="hidden" name="webaction" value="time.ReplaceCurrentPageAndChildren" />
							<input id="tc_replace_current_page_and_children_button" type="submit" title="${i18n.edit['time.action.replace-current-page-and-children']}" class="pc_edit_true" />
						</div>
					</form>
				</div>
			</fieldset>
			<fieldset>
				<legend>${i18n.edit["time.label.status"]}</legend>
				<div class="pc_line"><%
					MenuElement timePage = ctx.getCurrentPage();
					String path = timePage.getPath();
					MenuElement viewPage = content.getNavigation(editCtx).searchChild(editCtx, path);
					String statusKey = "time.status";
					if (viewPage == null) {
						statusKey += ".deleted";
					} else {
						boolean metadataEquals = timePage.isMetadataEquals(viewPage);
						boolean contentEquals = timePage.isContentEquals(viewPage);
						boolean childrenEquals = timePage.isChildrenEquals(viewPage);
						if (metadataEquals && contentEquals && childrenEquals) {
							statusKey += ".same";
						} else {
							statusKey += ".different.on";
							if (!metadataEquals) {
								statusKey += "-metadata";
							}
							if (!contentEquals) {
								statusKey += "-content";
							}
							if (!childrenEquals) {
								statusKey += "-children";
							}
						}
					}
					pageContext.setAttribute("statusKey", statusKey);
					%>${i18n.edit[statusKey]}
				</div>
			</fieldset>
			<form action="${info.currentURL}" method="post">
				<fieldset>
					<legend>${i18n.edit["time.label.set-travel-time"]}</legend>
					<div class="pc_line">
						<input type="hidden" name="webaction" value="time.settraveltime" />
						<input type="text" name="date" value="<%= globalContext.getTimeTravelerContext().getTravelTime()==null ? "" : 
							StringHelper.renderDate(globalContext.getTimeTravelerContext().getTravelTime(), "dd/MM/yy HH:mm:ss") %>" />
						<input type="submit" value="${i18n.edit['time.action.set']}" />
						<input type="submit" name="previous" value="${i18n.edit['time.action.previous']}" />
						<input type="submit" name="next" value="${i18n.edit['time.action.next']}" />
					</div>
				</fieldset>
			</form>
			<form action="${info.currentURL}" method="post">
				<fieldset>
					<legend>${i18n.edit["time.label.set-travel-time"]}</legend>
					<div class="pc_line">
						<input type="hidden" name="webaction" value="time.settraveltime" />
						<select name="date">
							<option value=""><%=StringHelper.renderDate(globalContext.getPublishDate(), "dd/MM/yy HH:mm:ss")%></option>
						<%
						List<Date> dates = PersistenceService.getInstance(globalContext).getBackupDates();
						for (Date date : dates) {
							%><option <%if (date.equals(globalContext.getTimeTravelerContext().getTravelTime())) {%>selected="selected"<%}
								%> value="<%=StringHelper.renderDate(date, "dd/MM/yy HH:mm:ss")%>"><%=StringHelper.renderDate(date, "dd/MM/yy HH:mm:ss")%></option><%
						}
						%>
						</select>
						<input type="submit" value="${i18n.edit['time.action.set']}" />
						<input type="submit" name="previous" value="${i18n.edit['time.action.previous']}" />
						<input type="submit" name="next" value="${i18n.edit['time.action.next']}" />
					</div>
				</fieldset>
			</form>
		</c:if><c:if test="${empty currentUser}">
				<form id="pc_login" name="login" method="post" action="${info.currentURL}">
				<input type="hidden" name="login-type" value="adminlogin">
				<input type="hidden" name="edit-login" value="edit-login">
				<fieldset>
					<legend>${i18n.edit['login.authentification']}</legend>
					<div class="pc_line">
						<label for="login">${i18n.edit["login.user"]} :</label>
					    <div class="pc_input"><input id="login" type="text" name="j_username" value="" /></div>
					</div>
					<div class="pc_line">
						<label for="password">${i18n.edit['login.password']} :</label>
						<div class="pc_input"><input type="password" name="j_password" value="" /></div>
					</div>	
					<input type="submit" name="edit-login" value="${i18n.edit['global.submit']}" />
				</fieldset>
				</form>
			</c:if>
	</div>
	<div class="pc_footer">
	</div>
</div>
<script type="text/javascript">
	if (top.location != document.location) { // iframe ?		
		var olddiv = document.getElementById('preview_command');
		olddiv.parentNode.removeChild(olddiv);
	}
</script>