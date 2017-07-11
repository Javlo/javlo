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
			<c:if test="${fn:length(mailing.errorReveicers)>0}">
			<div class="progress">
				<fmt:formatNumber var="deliveryErrorRate" value="${mailing.deliveryErrorRate*100}" maxFractionDigits="0" />
				<h2>Not delivery rate <span class="info">${deliveryErrorRate}%</span></h2>
				<div class="bar2"><div class="value" style="background-color: #d9534f; width: ${deliveryErrorRate}%;"><small>${deliveryErrorRate}%</small></div></div>
			</div>
			</c:if>
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
				<c:forEach var="click" items="${mailing.countClicks}">
					<td class="col">					
						<h3>${click.value}</h3>
						<p>#${click.key}</p>
					</td>
				</c:forEach> 
			</tr></table>	
			<div class="byhours">
			<h2>Read by hours</h2>
			<div id="graph-${mailing.id}"></div>	
			</div>		
			<script type="text/javascript">
				jQuery(document).ready(function () {
				    var s1 = [<c:forEach var = "u" begin = "0" end = "23" varStatus="status">${mailing.countReadersByHour[u]}${!status.last?',':''}</c:forEach>];				    
				    var ticks = [<c:forEach var = "u" begin = "0" end = "23" varStatus="status">'${u} > ${u+1}'${!status.last?',':''}</c:forEach>];
				 
				    plot1 = jQuery.jqplot('graph-${mailing.id}', [s1], {
			            // Only animate if we're not using excanvas (not in IE 7 or IE 8)..
			            animate: !jQuery.jqplot.use_excanvas,
			            seriesDefaults:{
			                renderer:jQuery.jqplot.BarRenderer,
			                pointLabels: { show: true }
			            },
			            axes: {
			                xaxis: {
			                    renderer: jQuery.jqplot.CategoryAxisRenderer,
			                    ticks: ticks
			                }
			            },
			            highlighter: { show: false }
			        });
				});
		</script>
					
		</div>
	</div>
</c:forEach>



