<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<form method="post" action="${info.currentURL}" class="standard-form">
	<div>
		<input type="hidden" name="webaction" value="push-static-on-ftp.push" />
	</div>
	<div class="row"><div class="col-md-6">
	<div class="form-group">
		<label>host</label>		
		<input type="text" name="host" class="form-control" value="${host}" />
	</div>
	<div class="form-group">
		<label>port</label>
		<input type="text" name="port" class="form-control" value="${port}" />
	</div>
	</div><div class="col-md-6">
	<div class="form-group">
		<label>username</label>
		<input type="text" name="username" class="form-control" value="${username}"/>
	</div>
	<div class="form-group">
		<label>password</label>
		<input type="password" name="password" class="form-control" value="${password}"/>
		<div class="checkbox"><label>
			<input type="checkbox" name="storepassword" class="checkbox" ${fn:length(password)>0?'checked="checked"':''} /> store password
		</label></div>
	</div>
	<div class="form-group">
		<div class="checkbox"><label>
			<input type="checkbox" name="ziponly" class="checkbox" /> create zip file (no upload)
		</label></div>
	</div>
	<div class="form-group">
		<label>notification email : </label>
		<input type="email" name="email" class="form-control" value="${email}"/>
	</div>
	</div></div>
	<div class="form-group">
		<label>path</label>
		<input type="text" name="path" class="form-control" value="${path}" />
	</div>
	<div class="action">
		
		<button type="submit" class="btn btn-primary pull-right">push</button>
	</div>
</form>