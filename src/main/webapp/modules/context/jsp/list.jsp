<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<div class="content">
	
<div class="row">
<c:forEach var="context" items="${contextList}" varStatus="status">	
	<div class="col-sm-6">		
		<div class="panel panel-default">
  			<div class="panel-heading">${context.key}</div>
  		<div class="panel-body">
  			<div class="row">
  				<div class="col-sm-6">
		    		<dl>
		    			<dt>latest login</dt>
		    			<dd>${not empty context.latestLoginDate?context.latestLoginDate:'?'}</dd> 
		    			<dt>title</dt>
		    			<dd>${context.globalTitle}</dd>
		    			<dt>#pages</dt>
		    			<dd>${context.pages}</dd>
		    		</dl>
		    		<br />
		    		<table class="table table-hover templates">
		    			<thead>
		    			<tr>
		    				<th>templates</th>
		    				<th>#</th>
		    				<th><span class="glyphicon glyphicon-info-sign"></span></th>		    			
		    			</tr>
		    			</thead>
		    			<tbody>
		    			<c:forEach var="template" items="${context.templates}">
		    			<c:if test="${template.countRef>0}">
		    			<tr>
		    				<td>${template.template}</td>
		    				<td>${template.countRef}</td>
		    				<td>
		    					${!template.listed?'<span class="glyphicon glyphicon-exclamation-sign" title="template not linked with site."></span>':''}
		    					${template.unknow?'<span class="glyphicon glyphicon-question-sign" title="template not found."></span>':''}
		    				</td>
		    			</tr>
		    			</c:if>
		    			</c:forEach>
		    			</tbody>
		    		</table>
		    	</div><div class="col-sm-6">
		    		<table class="table table-hover components">
		    			<thead>
		    			<tr>
		    				<th>component</th>
		    				<th>#</th>
		    				<th><span class="glyphicon glyphicon-info-sign"></span></th>		    			
		    			</tr>
		    			</thead>
		    			<tbody>
		    			<c:forEach var="comp" items="${context.components}">
		    			<tr>
		    				<c:set var="i18nKey" value="content.${comp.component}" />
		    				<td>${i18n.edit[i18nKey]}</td>
		    				<td>${comp.countRef}</td>
		    				<td>
		    					${!comp.listed?'<span class="glyphicon glyphicon-exclamation-sign" title="component not linked with site."></span>':''}
		    					${comp.unknow?'<span class="glyphicon glyphicon-question-sign" title="component not found."></span>':''}
		    				</td>
		    			</tr>
		    			</c:forEach>
		    			</tbody>
		    		</table>
		    	</div>
		   
    		</div>    		 
  		</div>
	</div>		
	</div>
	${status.index%2!=0?'</div><div class="row">':''}
</c:forEach>
</div>

</div>