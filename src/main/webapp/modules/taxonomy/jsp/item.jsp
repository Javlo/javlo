<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:set var="node" value="${taxonomy.taxonomyBeanMap[param.id]}" />
<div id="item-wrapper-${node.id}" class="item-wrapper" draggable="true" data-id="${node.id}" data-aschild="false">
	<a id="action-list-${node.id}" href="#" class="command action-list ${fn:length(node.children)>0?'open':'close'}" title="expand"><span class="glyphicon glyphicon-menu-down"></span><span class="glyphicon glyphicon-menu-right"></span></a>
	<a href="#" class="command delete" title="delete" onclick="jQuery('#input-delete').val('${param.id}'); jQuery('#taxonomy-form').submit(); return false;"><span class="glyphicon glyphicon-trash"></span></a>
	<a href="#" class="command move" title="move"><span class="glyphicon glyphicon-sort"></span></a>	
	<div class="item">	
		<span class="name"><input type="text" name="name-${param.id}" class="hidden-input form-control" value="${node.name}" /></span>
		<div class="collapse">
		<a href="#" class="action close ${node.labelsSize == 0?'empty':node.labelsSize<fn:length(info.contentLanguages)?'incomplete':'complete'}">
			<span class="glyphicon glyphicon-plus-sign"></span>
			<span class="glyphicon glyphicon-minus-sign"></span>			
		</a>		
		<ul class="translation bloc hidden">
		<li class="label id">			
				<label class="lang" for="change-id-${param.id}">ID</label>
				<span class="text"><input type="text" name="change-id-${param.id}" id="change-id-${param.id}" class="hidden-input form-control" value="${param.id}" placeholder="ID" /></span>
			</li>
		<li class="label deco">			
			<label class="lang" for="change-deco-${param.id}"><i class="fa fa-font" aria-hidden="true"></i></label>
			<span class="text"><input type="text" name="change-deco-${param.id}" id="change-deco-${param.id}" class="hidden-input form-control" value="${node.decoration}" placeholder="deco" /></span>			
		</li>
		<c:forEach var="lang" items="${info.contentLanguages}">
			<li class="label">			
				<label class="lang" for="label-${lang}-${param.id}">${lang}</label>
				<span class="text"><input type="text" name="label-${lang}-${param.id}" id="label-${lang}-${param.id}" class="hidden-input form-control" value="${node.labels[lang]}" placeholder="..." /></span>			
			</li>
		</c:forEach>			
		</ul>
		</div>
	</div>
</div>
