<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<div class="content">

<div class="remote-server-tree">

<ul>
	<c:forEach var="server" items="${remoteServers}">
		<li class="server">
			<span class="server-title">
					<div class="server-name">${server.hostname} (${server.address})</div>
				    <small class="server-os">${server.os}</small>
			</span>

			<ul>
				<c:forEach var="instance" items="${server.instances}">
					<li class="instance">
						<strong>${instance.systemUser}</strong> - port: ${instance.port} - v ${instance.version}
						<ul>
							<c:forEach var="remote" items="${instance.sites}">
								<li class="site">
									<a href="${remote.url}" target="_blank">${remote.url}</a> [${remote.ipAddress}]
									<c:if test="${remote.serverInfo.connectedUsers != null}">
										<c:set var="userList"><c:forEach var="u" varStatus="status" items="${remote.serverInfo.connectedUsers}">${u}${not status.last ? ', ' : ''}</c:forEach></c:set>
										<span title="Users: ${empty userList ? '-' : userList}">(${fn:length(remote.serverInfo.connectedUsers)} connected users)</span>
									</c:if>
								</li>
							</c:forEach>
						</ul>
					</li>
				</c:forEach>
			</ul>
		</li>
	</c:forEach>
</ul>

</div>

</div>
