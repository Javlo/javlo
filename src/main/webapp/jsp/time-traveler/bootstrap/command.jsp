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
<div id="preview_command" lang="${info.editLanguage}" class="edit-${not empty editUser} ${editPreview == 'true'?'edit':'preview'}">
<jsp:include page="../../preview/bootstrap/header.jsp"></jsp:include>
<div class="sidebar panel panel-default">
	<div class="panel-body">	
	<div class="pc_body">
		<c:if test="${not empty info.editUser}">
			<h2 class="first-title"><i class="fa fa-clock-o" aria-hidden="true"></i> ${i18n.edit["time.title"]}</h2>
			<h3>${i18n.edit["time.label.status"]}</h3>
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
			
			<form action="${info.currentURL}" method="post">
					<h3>${i18n.edit["time.label.set-travel-time"]}</h3>
					<div class="pc_line">
						<input type="hidden" name="webaction" value="time.settraveltime" />
						<div class="form-group">
						<select name="date" class="form-control">
							<option value=""><%=StringHelper.renderDate(globalContext.getPublishDate(), "dd/MM/yy HH:mm:ss")%></option>
						<%
						List<Date> dates = PersistenceService.getInstance(globalContext).getBackupDates();
						for (Date date : dates) {
							%><option <%if (date.equals(globalContext.getTimeTravelerContext().getTravelTime())) {%>selected="selected"<%}
								%> value="<%=StringHelper.renderDate(date, "dd/MM/yy HH:mm:ss")%>"><%=StringHelper.renderDate(date, "dd/MM/yy HH:mm:ss")%></option><%
						}
						%>
						</select></div>
						<div class="form-group">
						<div class="row">
						<div class="col-md-3">						
						<button type="submit" class="btn btn-default pull-right btn-block" name="next" title="${i18n.edit['time.action.next']}"><i class="fa fa-chevron-left" aria-hidden="true"></i></button>
						</div><div class="col-md-6"><input type="submit" class="btn btn-primary btn-block" value="${i18n.edit['time.action.set']}" /></div><div class="col-md-3">
						<button type="submit" class="btn btn-default pull-right btn-block" name="previous" title="${i18n.edit['time.action.previous']}"><i class="fa fa-chevron-right" aria-hidden="true"></i></button>
						</div>
						</div>
						</div>
					</div>
			</form>
			
			<div class="pc_form_line">
				<div class="form-group">
					<form id="pc_form" action="${info.currentURL}" method="post">
						<div class="pc_line">
							<input type="hidden" name="webaction" value="time.replacecurrentpage" />
							<input class="btn btn-default btn-block" id="tc_replace_current_page_button" type="submit" value="${i18n.edit['time.action.replace-current-page']}" class="pc_edit_true" />
						</div>
					</form>
					</div><div class="form-group">					
					<form id="pc_form" action="${info.currentURL}" method="post">
						<div class="pc_line">
							<input type="hidden" name="webaction" value="time.ReplaceCurrentPageAndChildren" />
							<input class="btn btn-default btn-block" id="tc_replace_current_page_and_children_button" type="submit" value="${i18n.edit['time.action.replace-current-page-and-children']}" class="pc_edit_true" />
						</div>
					</form>
					</div>
				</div>
			
		</c:if>
	</div>
	<div class="pc_footer">
	</div>
</div>
</div>
</div>
<script type="text/javascript">
	if (top.location != document.location) { // iframe ?		
		var olddiv = document.getElementById('preview_command');
		olddiv.parentNode.removeChild(olddiv);
	}
</script>