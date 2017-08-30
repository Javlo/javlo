<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><form id="delete-comments" action="${info.currentURL}" method="post"><div class="group-left"><label for="taxonomy">${i18n.edit["taxonomy"]}</label>${taxoSelect}</div><div class="special last">
<input type="hidden" name="webaction" value="search" />
<select name="type">
    <option value="">${i18n.edit['search.type.all']}</option>
	<option value="page" ${searchFilter.type == 'page'?'selected="selected"':''}>${i18n.edit['search.type.page']}</option>
	<option value="file" ${searchFilter.type == 'file'?'selected="selected"':''}>${i18n.edit['search.type.file']}</option>
</select>
<input type="text" name="title" value="${searchFilter.title}" placeholder="${i18n.edit['search.title']}" />
<input type="text" name="query" value="${searchFilter.global}" placeholder="${i18n.edit['search.global']}" />
<input type="submit" value="${i18n.edit['search.search']}" />
<input type="submit" name="reset" value="${i18n.edit['search.reset']}" /></div>
</form>

<div class="clear"></div>


