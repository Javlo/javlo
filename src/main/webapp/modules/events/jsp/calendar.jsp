<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div id="content" class="content ${not empty lightInterface?'light':''}">
	
	<div class="calendar">
	
	<div class="btn-group pull-right" role="group">
	
		<c:url var="previousURL" value="${info.currentURL}" context="/"><c:param name="calendar" value="true" /><c:param name="calkey" value="${calendar.previousKey}" /></c:url>
		<c:url var="nextURL" value="${info.currentURL}" context="/"><c:param name="calendar" value="true" /><c:param name="calkey" value="${calendar.nextKey}" /></c:url>
	
		<a class="btn btn-standard" href="${previousURL}"><i class="fas fa-caret-left" aria-hidden="true"></i></a>
		<a class="btn btn-standard" href="${nextURL}"><i class="fas fa-caret-right" aria-hidden="true"></i></a>
	</div>
	
	<h2>${calendar.label}</h2>	
	
	<table>
		<thead>
			<tr>
				<c:forEach var="day" items="${calendar.days}">
					<th>${day.largeLabel}</th>
				</c:forEach> 
			</tr>
		</thead>
		<tbody>
			<c:forEach var = "w" begin = "0" end = "5" varStatus="wstatus">
				<tr class="week-${wstatus.index+1}">
				<c:forEach var = "d" begin = "0" end = "6" varStatus="dstatus">
					<td class="${calendar.daysBloc[w][d].active?'active':'unactive'} ${calendar.daysBloc[w][d].toDay?'today':'nottoday'} day-${dstatus.index+1}">
						<div class="day-number">${calendar.daysBloc[w][d].monthDay}</div>
						<a href="#" class="prepare-event" onclick="jQuery('#startdate').val('${calendar.daysBloc[w][d].sortableDate}'); jQuery('#enddate').val('${calendar.daysBloc[w][d].sortableDate}');"><i class="far fa-plus-square"></i></a>
						<c:if test="${calendar.daysBloc[w][d].active}">						
						<ul class="events">
							<c:forEach var="ical" items="${calendar.monthEvents[calendar.daysBloc[w][d].monthDay]}">
							<li title="${ical.summaryOrCategories}" class="cal-${ical.categories} ${ical.next || ical.previous?'multidays':'oneday'} bg-color-${ical.colorGroup}">
								<c:if test="${!ical.previous && ical.editable}">								
									<c:url var="pdelIcalURL" value="${info.currentURL}" context="/"><c:param name="calendar" value="true" /><c:param name="ical" value="${ical.id}" /><c:param name="webaction" value="event.deleteical" /></c:url>
									<div class="action"><a href="${pdelIcalURL}"><i class="fas fa-times"></i></a></div>
								</c:if><c:if test="${!ical.previous && !ical.editable}">
									<div class="action"><i class="fas fa-calendar"></i></div>
								</c:if><c:if test="${ical.previous}">
								<c:if test="${!ical.next}">
								<div class="action"><i class="fas fa-long-arrow-alt-left"></i></div>
								</c:if><c:if test="${ical.next}">
								<div class="action"><i class="fas fa-exchange-alt"></i></div>
								</c:if>
								</c:if>							
								<div class="summary">${ical.summaryOrCategories}</div>								
							</li>
							</c:forEach>
						</ul></c:if>
					</td>
				</c:forEach>
				</tr>
			</c:forEach>
		</tbody>
		
	</table>
	
	
	</div>
	
	
</div>
