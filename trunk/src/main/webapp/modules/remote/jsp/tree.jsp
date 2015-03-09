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
						Listening on ${instance.port}
						<ul>
							<c:forEach var="remote" items="${instance.sites}">
								<li class="site">
									<a href="${remote.url}" target="_blank">${remote.url}</a>
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
