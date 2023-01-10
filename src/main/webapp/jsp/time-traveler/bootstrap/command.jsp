<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
    	    org.javlo.navigation.MenuElement,
    	    org.javlo.service.PersistenceService.MetaPersistenceBean"%>
<%
GlobalContext globalContext = GlobalContext.getInstance(request);
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentContext editCtx = new ContentContext(ctx);
editCtx.setRenderMode(ContentContext.EDIT_MODE);
ContentService content = ContentService.getInstance(globalContext);



MenuElement timePage = ctx.getCurrentPage();
String path = timePage.getPath();
MenuElement viewPage = content.getNavigation(editCtx).searchChild(editCtx, path);
String statusKey = "time.status";
String icon = "<i class='bi bi-file-earmark-check'></i>";
String messageStatus = "success";
boolean pageSame = true;
if (viewPage == null) {
	statusKey += ".deleted";
} else {
	pageSame = timePage.isMetadataEquals(viewPage) && timePage.isContentEquals(viewPage) && timePage.isChildrenEquals(viewPage);
}
request.setAttribute("pageSame", pageSame);
%>
<div id="preview_command" lang="${info.editLanguage}" class="_time-mode edit-${not empty editUser} ${editPreview == 'true'?'edit':'preview'}">
	<jsp:include page="header.jsp"></jsp:include>
	<div class="sidebar panel panel-default">
		<div class="panel-body">
			<div class="tab-pane">
				<c:if test="${not empty info.editUser}">
					<h2 class="first-title">
						<i class="bi bi-clock-history"></i> ${i18n.edit["time.title"]}
					</h2>
					
					<div class="well"><i class="bi bi-question-circle"></i> ${i18n.edit["time.help"]}</div>

					<form action="${info.currentURL}" method="post">
						<div class="pc_line">
							<input type="hidden" name="webaction" value="time.settraveltime" />
							<div class="form-group">
								<select name="date" class="form-control" onchange="this.form.submit();">
									<option value=""></option>
									<%
									MetaPersistenceBean selectBean = null;
									for (MetaPersistenceBean persBean : PersistenceService.getInstance(globalContext).getPersistences()) {
										if (persBean.getVersion() > 0) {
											if (globalContext.getTimeTravelerContext().getVersion() != null && persBean.getVersion() == globalContext.getTimeTravelerContext().getVersion()) {
												selectBean = persBean;
											}
									%>
									<option <%if (globalContext.getTimeTravelerContext().getVersion() != null
		&& persBean.getVersion() == globalContext.getTimeTravelerContext().getVersion()) {%> selected="selected" <%}%> value="<%=persBean.getVersion()%>"><%=persBean.getVersion()%> |
										<%=persBean.getDate()%></option>
									<%
									}
									}
									%>
								</select>
							</div>
							<div class="form-group">
								<div class=_jv_flex-line>
									<div class="time-command">
										<button type="submit" class="btn btn-default pull-right btn-block" name="previous" title="${i18n.edit['time.action.next']}">
											<i class="bi bi-chevron-left"></i>
										</button>
									</div>
									<div class="time-status">
										<div class="btn" title="version:<%=selectBean==null?'?':selectBean.getVersion()%>">
											<%=(selectBean != null ? selectBean.getHumanDate() : "")%>
										</div>
									</div>
									<div class="time-command">
										<button type="submit" class="btn btn-default pull-right btn-block" name="next" title="${i18n.edit['time.action.previous']}">
											<i class="bi bi-chevron-right"></i>
										</button>
									</div>
								</div>
							</div>
						</div>
					</form>


<%-- 					<div class="well backup">
						<div class="title">
							<i class="bi bi-archive"></i> backup
						</div>
						<form action="${info.currentURL}" method="post">
							<div class="pc_line">
								<input type="hidden" name="webaction" value="time.settraveltime" />
								<div class="form-group">
									<select name="date" class="form-control" onchange="this.form.submit();">
										<option value=""></option>
										<%
										List<Date> dates = PersistenceService.getInstance(globalContext).getBackupDates();
										for (Date date : dates) {
										%><option <%if (date.equals(globalContext.getTimeTravelerContext().getTravelTime())) {%> selected="selected" <%}%> value="<%=StringHelper.renderDate(date, "dd/MM/yy HH:mm:ss")%>"><%=StringHelper.renderDate(date, "dd/MM/yy HH:mm:ss")%></option>
										<%
										}
										%>
									</select>
								</div>
							</div>
						</form>
					</div> --%>

					<div class="pc_form_line">
						<div class="form-group">
							<form id="pc_form" action="${info.currentURL}" method="post">
								<div class="pc_line">
									<input type="hidden" name="webaction" value="time.replacecurrentpage" />
									<input class="btn btn-primary btn-block" id="tc_replace_current_page_button" ${pageSame?'disabled':''} type="submit" value="${i18n.edit['time.action.replace-current-page']}" class="pc_edit_true" />
								</div>
							</form>
						</div>
						<div class="form-group">
							<form id="pc_form" action="${info.currentURL}" method="post">
								<div class="pc_line">
									<input type="hidden" name="webaction" value="time.ReplaceCurrentPageAndChildren" />
									<input class="btn btn-default btn-block" id="tc_replace_current_page_and_children_button" ${pageSame?'disabled':''} type="submit" value="${i18n.edit['time.action.replace-current-page-and-children']}" class="pc_edit_true" />
								</div>
							</form>
						</div>
					</div>

				</c:if>
			</div>
			<div class="pc_footer"></div>
		</div>
	</div>
</div>
<script type="text/javascript">
	if (top.location != document.location) { // iframe ?		
		var olddiv = document.getElementById('preview_command');
		olddiv.parentNode.removeChild(olddiv);
	}
</script>