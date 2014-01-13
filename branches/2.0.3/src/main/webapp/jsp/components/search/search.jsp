<%@page contentType="text/html"
import="org.javlo.I18nAccess,
		org.javlo.ContentContext"
%><%
ContentContext ctx = ContentContext.getContentContext ( request, response );
I18nAccess i18nAccess = I18nAccess.getInstance ( request.getSession() );
%>
<div id="search-teaser">
<h1><%=i18nAccess.getViewText("search.title")%></h1>
<form id="search-form" method="post" action="">
<div>
<input type="hidden" name="webaction" value="search.search" />
<div class="text-countainer"><input class="text" type="text" name="keywords" /></div>
<div class="submit-countainer"><input class="submit" type="submit" name="ok" value="ok" /></div>
</div>
</form>
</div>


		    	
