<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">

<div class="remote-server-tree">

<ul>
	<c:forEach var="server" items="${remoteServers}">
		<li class="server">
			<span class="server-title">${server.hostname} (${server.address})</span>
			<ul>
				<c:forEach var="instance" items="${server.instances}">
					<c:if test="${instance.charge>=0}">
					<li class="instance">
						<strong>${instance.systemUser}</strong> - port: ${instance.port} - v ${instance.version} - #req/min : ${instance.charge}
						<ul>
							<c:forEach var="remote" items="${instance.sites}">
								<li class="site">
									<div class="progress">
                        				<a href="${remote.url}" target="_blank">${remote.url}</a> [#req/min : ${remote.siteCharge}]
                        				<c:set var="prop" value="" />
                        				<div class="bar"><div class="value ${remote.siteChargeProportion<25?'bluebar':remote.siteChargeProportion<75?'orangebar':'redbar'}" style="width: ${remote.siteChargeProportion}%;"></div></div>
                    				</div>									
								</li>
							</c:forEach>
						</ul>
					</li></c:if><c:if test="${instance.charge<0}"><li><strong>${instance.systemUser}</strong> - no charge info</li></c:if>
				</c:forEach>
			</ul>
		</li>
	</c:forEach>
</ul>

</div>

</div>
