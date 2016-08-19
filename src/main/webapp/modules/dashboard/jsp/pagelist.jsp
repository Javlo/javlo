<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">
    <br clear="all"/>
    <div class="invoice three_fourth">
        <div class="invoice_inner">
            <span class="lang">${info.language}</span>
            <h2 class="title">Pages problems</h2>
            <br clear="all"/><br/>
            <div class="one_half">
                <h2>Bad external link (max:${report.maxLinkCheck})</h2>
                <c:if test="${report.badExternalLink > 0}">
                    <ul>
                        <c:forEach var="link" items="${report.badExternalLinkPages}">
                        <li><a href="${link.url}">${link.label}</a>
                            </c:forEach>
                    </ul>
                </c:if>
                <c:if test="${report.badExternalLink == 0}">no error</c:if>
            </div>
            <div class="one_half last">
                <h2>Bad internal link (max:${report.maxLinkCheck})</h2>
                <c:if test="${report.badInternalLink > 0}">
                    <ul>
                        <c:forEach var="link" items="${report.badInternalLinkPages}">
                        <li><a href="${link.url}">${link.label}</a>
                            </c:forEach>
                    </ul>
                </c:if>
                <c:if test="${report.badInternalLink == 0}">no error</c:if>
            </div>
            <span class="line"></span>
            <div class="one_half">
                <h2>Page without title</h2>
                <c:if test="${fn:length(report.noTitlePages) > 0}">
                    <ul>
                        <c:forEach var="link" items="${report.noTitlePages}">
                        <li><a href="${link.url}">${link.label}</a>
                            </c:forEach>
                    </ul>
                </c:if>
                <c:if test="${fn:length(report.noTitlePages) == 0}">no error</c:if>
            </div>
            <div class="one_half last">
                <h2>Page without description</h2>
                <c:if test="${fn:length(report.noDescriptionPages) > 0}">
                    <ul>
                        <c:forEach var="link" items="${report.noDescriptionPages}">
                        <li><a href="${link.url}">${link.label}</a>
                            </c:forEach>
                    </ul>
                </c:if>
                <c:if test="${fn:length(report.noDescriptionPages) == 0}">no error</c:if>
            </div>
        </div>
    </div>
</div>