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
					<li class="instance">
						<strong>${instance.systemUser}</strong> - port: ${instance.port} - v ${instance.version}
						<ul>
							<c:forEach var="remote" items="${instance.sites}">
								<li class="site">
									<a href="${remote.url}" target="_blank">${remote.url}</a>
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
