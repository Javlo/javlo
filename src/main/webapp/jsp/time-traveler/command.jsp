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
	<div class="pc_header">${i18n.edit["preview.command"]}</div>
	<div class="pc_body">
		<c:if test="${not empty currentUser}">
			<form id="pc_logout_form" action="${info.currentURL}" method="post">
				<div class="pc_line">
					<a href="${info.currentURL}?edit-logout=logout">${i18n.edit['login.logout']}</a>
				</div>
			</form>
			<fieldset class="pc_command">
				<legend>${i18n.edit['global.command']}</legend>
				<div class="pc_form_line">
					<form id="pc_form" action="${info.currentURL}" method="post">
						<div class="pc_line">
							<input type="hidden" name="webaction" value="time.replacecurrentpage" />
							<input id="tc_replace_current_page_button" type="submit" title="replace current page in edit" class="pc_edit_true" />
						</div>
					</form>
				</div>
			</fieldset>
			<fieldset>
				<legend>status in preview</legend>
				<div class="pc_line">
					The page is <%
					MenuElement timePage = ctx.getCurrentPage();
					String path = timePage.getPath();
					MenuElement viewPage = content.getNavigation(editCtx).searchChild(editCtx, path);
					if (viewPage == null) {
						%>deleted<%
					} else {
						boolean metadataEquals = timePage.isMetadataEquals(viewPage);
						boolean contentEquals = timePage.isContentEquals(viewPage);
						boolean childrenEquals = timePage.isChildrenEquals(viewPage);
						if (metadataEquals && contentEquals && childrenEquals) {
							%>the same<%
						} else {
							%>different (<%
							if (metadataEquals) {
								%>metadata<%
							}
							if (contentEquals) {
								if (metadataEquals) {
									%>, <%
								}
								%>content<%
							}
							if (childrenEquals) {
								if (metadataEquals || contentEquals) {
									%>, <%
								}
								%>children<%
							}
							%>)<%
						}
					}
					%>
				</div>
			</fieldset>
			<form action="${info.currentURL}" method="post">
				<fieldset>
					<legend>set travel time</legend>
					<div class="pc_line">
						<input type="hidden" name="webaction" value="time.settraveltime" />
						<input type="text" name="date" value="<%= StringHelper.renderDate(globalContext.getTimeTravelerContext().getTravelTime()==null ? new Date() : globalContext.getTimeTravelerContext().getTravelTime(), "dd/MM/yy HH:mm:ss") %>" />
						<input type="submit" value="set" />
					</div>
				</fieldset>
			</form>
			<form action="${info.currentURL}" method="post">
				<fieldset>
					<legend>set travel time</legend>
					<div class="pc_line">
						<input type="hidden" name="webaction" value="time.settraveltime" />
						<select name="date">
							<option value=""><%=StringHelper.renderDate(globalContext.getPublishDate(), "dd/MM/yy HH:mm:ss")%>-current</option>
						<%
						List<Date> dates = PersistenceService.getInstance(globalContext).getBackupDates();
						for (Date date : dates) {
							%><option value="<%=StringHelper.renderDate(date, "dd/MM/yy HH:mm:ss")%>"><%=StringHelper.renderDate(date, "dd/MM/yy HH:mm:ss")%></option><%
						}
						%>
						</select>
						<input type="submit" value="set" />
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