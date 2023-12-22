<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<div class="content">

<div class="container">
<form id="sitemapform" action="${info.currentURL}" method="post">
	<div class="row">
	<div class="col-sm-11">
	<div class="form-group">
		<input class="form-control" name="sitemap" type="text" value="${param.sitemap}" placeholder="sitemap.xml" />
	</div>
	</div><div class="col-sm-1">
	<button type="submit" class="btn btn-standard btn-xs pull-right">&raquo;</button>
	</div>
	</div>
</form>
</div>

<c:if test="${not empty urls}">
<table cellpadding="0" cellspacing="0" border="0" class="dyntable cell-border compact stripe" id="remotelist">
 <thead>
     <tr>       
       <th class="head0">url</th>                  
       <th class="head1">latest change</th>       
       <th class="head0">priority</th>                  
       <th class="head1">changeFreq</th>
       <th class="head0">resp</th>
       <th class="head1">resp time</th>       
     </tr>     
</thead>
<colgroup>    
    <col class="con0" />
    <col class="con1" />
    <col class="con0" />
    <col class="con1" />    
    <col class="con0" />
    <col class="con1" />
</colgroup>
<tbody> 
 <c:forEach var="url" items="${urls}">
 <tr class="gradeX">	 
     <td class="con0"><a href="${url.link}" target="_blank">${url.link}</a></td>     	 
     <td class="con1">${url.date}</td>     
     <td class="con0">${url.priority}</td>
     <td class="con1">${url.changeFreq}</td>
     <td class="con0"><div class="result" id="${url.id}-response">Loading...</div></td>
     <td class="con1"><div class="result" id="${url.id}-response-time">Loading... </div></td>     
 </tr>     
</c:forEach>
</tbody>
<tfoot>
   <tr>       
      <th class="head0">url</th>                  
       <th class="head1">latest change</th>       
       <th class="head0">priority</th>                  
       <th class="head1">changeFreq</th>
       <th class="head0">resp</th>
       <th class="head1">resp time</th>       
     </tr>
</tfoot>
</table>

<script type="text/javascript">
jQuery(document).ready(function() {
	urls =  [<c:forEach var="url" items="${urls}" varStatus="status">["${url.id}", "${url.link}"]<c:if test="${!status.last}">,</c:if></c:forEach>];
	for (var i=0; i<urls.length; i++) {
		update(urls[i][0], urls[i][1]);	
	}
	jQuery('#remotelist').dataTable( {
		 "aaSorting": [[ 1, "desc" ]],
		 "aoColumns": [
		               null,
		               null,
		               null,
		               null,
		               null,
		               null
		           ], 
		 "oLanguage": {"sUrl": "${info.editTemplateURL}/js/plugins/i18n/datatable_${info.editLanguage}.txt"}		 
	});
	
});

<c:url var="remoteURL" value="${info.currentAjaxURL}" context="/">
	<c:param name="webaction" value="remote.testurl" />
</c:url>

function update(id, url) {
	var url = "${remoteURL}&url="+encodeURIComponent(url);
	jQuery.ajax({
		  url: url,
		  async: false,
		  dataType: "json"
		}).done(function( rep ) {		   
		  jQuery("#"+id+"-response").html('<span class="label label-'+(rep.data.responsecode>=400?'danger':rep.data.responsecode>=300?'waring':'success')+'">'+rep.data.responsecode+'</span>');
		  jQuery("#"+id+"-response-time").html('<span class="label label-'+(rep.data.responsetime>=10000?'danger':rep.data.responsetime>=1000?'warning':'success')+'">'+rep.data.responsetime+'</span>');
		}).error(function() {
		  jQuery("#"+id+"-response").html("error");
		  jQuery("#"+id+"-response-time").html("error");
		});
}
</script>
</c:if>

</div>