<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="info" class="content">
<fieldset>
<legend>Site</legend>
<ul class="site">
	<li><span class="label">ContextKey</span>${globalContext.contextKey}</li>
	<c:if test="${empty lightInterface}"><li><span class="label">Folder</span>${globalContext.folder}</li></c:if>
	<c:if test="${empty lightInterface}"><li><span class="label">User Factory</span>${globalContext.userFactoryClassName}</li></c:if>
	<c:if test="${empty lightInterface}"><li><span class="label">Admin User Factory</span>${globalContext.adminUserFactoryClassName}</li></c:if>
	<c:if test="${empty lightInterface && globalContext.staticConfig.accountSize}"><li><span class="label">Size</span>${globalContext.accountSizeLabel}</li></c:if>
	<li><span class="label">Admin</span><a href="mailto:${globalContext.administratorEmail}">${globalContext.administratorEmail}</a></li>
	<li><span class="label">Publish date</span>${globalContext.publishDateLabel}</li>
	<li><span class="label">Latest publisher</span>${globalContext.latestPublisher}</li>
	<c:if test="${not empty threadManager && threadManager.countThread>0}">
	<li><span class="label">#Thread : </span>${threadManager.countThread}</li>
	<li><span class="label">Current Thread</span>${threadManager.currentThreadName}</li>
	</c:if><c:if test="${empty lightInterface}">
	<li><span class="label">Memory</span>${memory.totalMemoryLabel}<c:if test="${info.admin}"><a class="btn btn-default btn-xs pull-right" href="${info.currentURL}?webaction=dashboard.garbage">garbage</a></c:if>
	<div class="progress"><div class="bar2"><div id="memory-bar" class="value redbar" style="width: ${memory.usedMemoryPercent}%;"><small>${memory.usedMemoryLabel}</small></div></div></div>
	<script>
		<c:url var="ajaxURL" value="${info.currentAjaxURL}" context="/">
			<c:param name="webaction" value="data.memory" />
		</c:url>
		function updateMemory() {
		    jQuery.ajax({
		    	dataType: "json",
		    	url: '${ajaxURL}'
		    }).done(function(jsonObj) {
		    	  console.log(jsonObj);
		    	  console.log(jsonObj.data.usedMemoryPercent);
		    	  jQuery('#memory-bar').attr("style", "width:"+jsonObj.data.usedMemoryPercent+"%");
		    	  jQuery('#memory-bar').html("<html><small>"+jsonObj.data.usedMemoryLabel+"</small></html>")
		    });		
		}
		setInterval(updateMemory, 2000);
	</script>
	</li>	
	</c:if>
</ul>
</fieldset>
<fieldset>
<legend>Server</legend>
<ul class="server">
<li><span class="label">Revision : </span>${globalContext.staticConfig.sourceRevision}</li>
<li><span class="label">Build : </span>${globalContext.staticConfig.buildTime}</li>
<li><span class="label">Request/minute : </span>${globalCount.count} (${globalCount.average})</li>
</ul>
</fieldset>
</div>