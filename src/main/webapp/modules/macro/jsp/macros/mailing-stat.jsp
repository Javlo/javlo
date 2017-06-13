<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%> <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:if test="${fn:length(mailingList)==0}">
	<div class="alert alert-warning" role="alert">no mailing found on this page.</div>
</c:if>
<c:forEach var="mailing" items="${mailingList}">
	<div class="panel">
		<div class="panel-title">${mailing.dateString}</div>
		<div class="panel-body">
			<div class="progress">
				<fmt:formatNumber var="rate" value="${mailing.readersRate*100}" maxFractionDigits="0" />
				<h2>Open rate <span class="info">${rate}%</span></h2>
				<div class="bar2"><div class="value" style="background-color: #4DA74D; width: ${rate}%;"><small>${rate}%</small></div></div>
			</div>
			<table class="keyvalues"><tr>
				<td class="col">					
					<h3>${mailing.subject}</h3>
					<p>Subject</p>
				</td>
				<td class="col">					
					<h3>${mailing.from}</h3>
					<p>Sender</p>
				</td>								
				<td class="col">					
					<h3>${mailing.receiversSize}</h3>
					<p>#receivers</p>
				</td>
				<td class="col">					
					<h3>${mailing.countReaders}</h3>
					<p>#open</p>
				</td>
				<c:if test="${mailing.countUnsubscribe>0 }">
					<td class="col">					
						<h3>${mailing.countUnsubscribe}</h3>
						<p>#unsubscribe</p>
				</td>
				</c:if>
			</tr></table>			
		</div>
	</div>
</c:forEach>


