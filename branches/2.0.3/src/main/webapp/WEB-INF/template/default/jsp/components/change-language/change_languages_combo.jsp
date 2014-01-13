<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><c:choose><c:when test="${isContentLanguages}"><ul class="content-language-selector">
<c:forEach var="lg" items="${languagesList}"><c:if test="${lg.realContent}"><li><a href="${lg.url}" class="${lg.language == info.requestContentLanguage ? 'selected' : ''}" lang="${lg.language}" hreflang="${lg.language}" xml:lang="${lg.language}" title="${lg.label}">${lg.language}</a></li></c:if>
</c:forEach>
</ul></c:when><c:otherwise><form id="langbox" method="post" action="${info.currentURL}">
<input type="hidden" name="webaction" value="view.language" />
<label class="ep_hidden" for="langbox_select">Navigation language: </label>
<select id="langbox_select" title="Choose the navigation language" name="lg">
<c:forEach var="lg" items="${languagesList}"><option <c:if test="${lg.language == info.language}">selected="selected"</c:if> value="${lg.language}" lang="${lg.language}" xml:lang="${lg.language}">${lg.language} - ${lg.label}</option>    
</c:forEach></select>
<input type="submit" id="langbox_btn" value="OK" title="Change the navigation language" lang="en" />
</form></c:otherwise></c:choose>
