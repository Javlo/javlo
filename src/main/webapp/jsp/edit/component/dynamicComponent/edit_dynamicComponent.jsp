<%@page import="org.javlo.servlet.IVersion"%>
<%@ page import="java.io.StringWriter" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="org.javlo.fields.Field" %>
<%@ page import="java.util.Collection" %>
<%@ page import="org.javlo.message.MessageRepository" %>
<%@ page import="org.javlo.message.GenericMessage" %>
<%@ page import="org.javlo.context.ContentContext" %>
<%@ page import="org.javlo.component.dynamic.DynamicComponent" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.javlo.i18n.I18nAccess" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="org.javlo.context.GlobalContext" %>
<%@ taglib
	uri="jakarta.tags.core" prefix="c"%><%@ taglib
	prefix="fn" uri="jakarta.tags.functions"%>


<%
	ContentContext ctx = ContentContext.getContentContext(request, response);
	DynamicComponent comp = (DynamicComponent) request.getAttribute("comp");
	Collection<Field> fields = comp.getFields(ctx);
	boolean allValid = true;
	for (Field field : fields) {
		if (field.getName().equalsIgnoreCase("info")) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage("field could not call info.", GenericMessage.ERROR));
		}
		if (!field.validate()) {
			allValid = false;
		}
	}
%>

<script>
	function refreshComponent() {
		fetch("${exportComponentUrlHtml}?mode=<%=ContentContext.EDIT_MODE%>").then(function(response) {
			return response.text();
		}).then(function(html) {
			document.querySelector('#tab1-${compid}').innerHTML = html;
		});
	}
</script>

<div class="dynamic-component <%=allValid?"all-":""%>valid cols">

	<%
		int colSize = 0;
		Iterator<Field> iter = fields.iterator();
		boolean first = true;
		String group = null;
		while (iter.hasNext()) {
			Field field = iter.next();
			if (field != null) {
				field.setFirst(first);
				first = false;
				field.setLast(!iter.hasNext());
				if (colSize >= 12) {
					colSize = 0;
				}
				out.println(field.getOpenRow(ctx));
				if (field.getTranslation() != null) {
					I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
					%>
						<fieldset><legend><%=i18nAccess.getText("field.translated")%></legend>
					<%
				}
				if (field.getName().equalsIgnoreCase("info")) {
					%><div class="alert alert-danger" role="alert">field could not call 'info'.</div><%
				}
				Collection<Locale> translatedField = new LinkedList<Locale>();
				if (field.getTranslation() == null) {
					translatedField = new LinkedList<Locale>();
					translatedField.add(null);
				} else {
					translatedField = field.getTranslation();
				}
				for (Locale locale : translatedField) {
					if (locale != null) {%>
						<fieldset><legend>
							<%=locale.getDisplayLanguage(new Locale(GlobalContext.getInstance(ctx.getRequest()).getEditLanguage(ctx.getRequest().getSession())))%>
						</legend>
					<%}
					field.setCurrentLocale(locale);
					String editXHTML = field.getEditXHTMLCode(ctx);
					if (editXHTML == null || editXHTML.trim().length() == 0) {
						%><div class="alert alert-danger" role="alert">field format error : <%=field.getName()%></div><div class="group-edit"><%
					} else {
						if (field.getGroup() != null) {
							if (!field.getGroup().equals(group)) {
								if (group != null) {
									%></div></div><%
								}
								%><div class="group-bloc"><div class="group-name"><%=field.getGroup()%></div><div class="group-edit"><%
							}
							group = field.getGroup();
						} else if (group != null) {
							%></div></div><%
							group = null;
						}
						%><%=editXHTML%><%
					}
					if (locale != null) {
						%></fieldset><%
					}
				}
				if (field.getTranslation() != null) {
					%></fieldset><%
				}
				if (iter.hasNext()) {
					%><hr /><%
				}
				%><%=field.getCloseRow(ctx)%><%
			}
		}
	if (group != null) {
%></div></div><%
	}
	%>
</div> <!-- close -->