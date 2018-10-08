<%@page import="org.javlo.servlet.IVersion"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<html lang="en">
<head>
<link rel="stylesheet"
	href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.2/css/bootstrap.min.css"
	integrity="sha384-Smlep5jCw/wG7hdkwQ/Z5nLIefveQRIY9nfy6xoR1uRYBtpZgI6339F5dgvm/e9B"
	crossorigin="anonymous">
<style type="text/css">
body {
	background-color: rgba(0, 0, 0, 0.06);
	font-size: 0.9em;
}

h1 {
	left: 0;
	top: 1vh;
	text-align: center;
	text-align: center;
	color: #D31996;
	margin-top: 0;
	font-family: javloFont, Verdana;
	position: absolute;
	font-size: 6vh;
	width: 100%;
}

h2 {
	font-size: 1.4em;
}

.main-container {
	height: 100vh;
	margin: 0 auto;	
}
.card {
	opacity: 0.95;
}
</style>
<style type="text/css">
		@font-face {
    		font-family: "javloFont";
    		<c:url var="javloURL" value="/fonts/javlo-italic.ttf" />
    		src: url('${javloURL}') format("truetype");
		}
</style>
</head>
<body>

	<div class="container main-container row align-items-center">
		<h1>Javlo</h1>
		<div class="col-12">
			<div class="card">
				<div class="card-header">Javlo : Install</div>
				<div class="card-body">
					<c:if test="${not empty error}">
						<div class="alert alert-danger" role="alert">${error}</div>
					</c:if>
					<div class="row">
						<div class="col-md-6">
							<h2 class="card-title">Welcome to the installation of Javlo</h2>
							<p class="card-text">The installation is almost complete.
								Thank you for giving the latest information needed.</p>
							<form method="get" ${param.install?'was-validated':''}>
								<input type="hidden" name="install" value="true" />
								<div class="form-group row">
									<label for="config" class="col-sm-3 col-form-label">Config folder*</label>
									<div class="col-sm-9">
									<input required type="text"
										class="form-control" id="config" name="config"
										aria-describedby="config folder" placeholder="Config folder"
										value="$HOME/etc"> <small id="config"
										class="form-text text-muted">The file
										"static-config.properties" will be created with the default
										values if it does not already exist.</small>
									</div>
								</div>
								<div class="form-group row">
									<label for="data" class="col-sm-3 col-form-label">Data folder*</label>
									<div class="col-sm-9">
									<input required type="text" class="form-control" id="data" name="data" aria-describedby="data folder" placeholder="date folder"
										value="$HOME/data"><small id="data"
										class="form-text text-muted">If the file
										"static-config.properties" already exists the data directory
										will not be modified.</small>
										</div>
									
								</div>
								<div class="form-group row">
									<label for="admin" class="col-sm-3 col-form-label">Admin password*</label>
									<div class="col-sm-9">
									<input required type="text" class="form-control" id="admin" name="admin" aria-describedby="admin password" placeholder="password"
										value=""><small id="admin"
										class="form-text text-muted">Password of 'admin' user (no change if "static-config.properties" already exists).</small>
										</div>
									
								</div>
								<div class="form-group form-check">
									<input type="checkbox" class="form-check-input"
										id="import-template" name="import-template"> <label
										class="form-check-label" for="import-template">Import
										default template from javlo.org.</label>
								</div>
								<div class="form-group form-check">
									<input type="checkbox" class="form-check-input"
										id="import-demo" name="import-template"> <label
										class="form-check-label" for="import-demo">Import demo
										content from javlo.org.</label>
								</div>
								<div class="form-group form-check">
									<input type="checkbox" class="form-check-input"
										id="import-demo" name="import-template" checked="checked">
									<label class="form-check-label" for="import-demo">Notifying
										javlo.org of my installation anomynically.</label>
								</div>
								<div class="form-group">
									<label for="email">Email address (for newsletter and
										survey)</label> <input type="email" class="form-control" id="email"
										name="email" aria-describedby="emailHelp"
										placeholder="Enter email"> <small id="emailHelp"
										class="form-text text-muted">We'll never share your
										email with anyone else.</small>
								</div>
								<div class="text-right">
									<button type="submit" class="btn btn-primary btn-sm">finish install</button>
								</div>
							</form>
						</div>
						<div class="col-md-6">
							${remoteinfo}
							<h2>System information</h2>
							<ul class="list-group">
								<li
									class="list-group-item d-flex justify-content-between align-items-center">Javlo
									Version : <span class="badge badge-primary badge-pill"><%=IVersion.VERSION%></span>
								</li>
								<li
									class="list-group-item d-flex justify-content-between align-items-center">Tomcat
									Version (min 7.0) : <span class="badge badge-primary badge-pill"><%=application.getServerInfo()%></span>
								</li>
								<li
									class="list-group-item d-flex justify-content-between align-items-center">Java
									Version (min 1.8) : <span class="badge badge-primary badge-pill"><%=System.getProperty("java.version")%></span>
								</li>
								<li
									class="list-group-item d-flex justify-content-between align-items-center">Max
									Memory (min 2048MB) : <span
									class="badge badge-primary badge-pill"><%=Runtime.getRuntime().maxMemory() / (1024 * 1024)%>
										MB</span>
								</li>
							</ul>							
						</div>
					</div>
				</div>
				<div class="card-footer text-muted text-right"><a href="//javlo.org#install" target="_blanck">javlo.org</a></div>
			</div>
		</div>
	</div>


</body>
</html>