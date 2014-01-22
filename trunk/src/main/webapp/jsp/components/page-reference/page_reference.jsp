<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<div class="page-reference">
<c:forEach items="${pages}" var="page" varStatus="status">
	<!-- Element de contenu (affichage en 2 colonnes) : INTERDICTION DE PLACER UN COMMENTAIRE OU UNE BALISE ENTRE LES DEUX ELEMENTS -->
	<div class="item" lang="${page.language}">
	        <a href="${page.url}">	
			<span class="ep_title">${page.title}</span> <span
				class="ep_subtitle">${page.startDate} <c:if
					test="${not (page.startDate eq page.endDate)}">- ${page.endDate}</c:if>
			</span>
			</a>
		
		    <!-- Image de l'element -->					
		    <c:set var="groupClass" value="one" scope="page" />
		    <c:if test="${fn:length(page.images) gt 1}">
		    	<c:set var="groupClass" value="multi" scope="page" />
		    </c:if>		   
		    
			<span class="img ${groupClass}"> <!-- Gallerie de l'élément -->
				<c:forEach items="${page.images}" var="image" varStatus="status">
				
				   <c:url value="" var="linkURL" context="/"/>
				   <c:if test="${empty image.linkURL}">
				     <c:if test="${not empty image.viewURL}">
				       <c:url value="${image.viewURL}" var="linkURL" context="/" />
		             </c:if>
		           </c:if>
			       <c:if test="${not empty image.linkURL}">
				     <c:url value="${image.linkURL}" var="linkURL" context="/" />
			       </c:if>
			       						       
			       <c:if test="${fn:contains(linkURL,'?')}">
			       	   <c:set var="linkURL" value="${linkURL}&epbox=&gallery=gal${page.id}" scope="page" />
			       </c:if>
			       
			       <c:if test="${not fn:contains(linkURL,'?')}">
			       	   <c:set var="linkURL" value="${linkURL}?epbox=&gallery=gal${page.id}" scope="page" />
			       </c:if>

				 <c:if test="${status.count == 1}">	
				   <c:if test="${not empty linkURL}">							
					 <a class="${image.cssClass}" href="${linkURL}" title="${image.description}" rel="gal-${comp.id}">
				       <img src="${image.url}" alt="${image.description}" />
				       <span class="ep_endbox">&nbsp;</span>
					 </a>
				   </c:if>	
				    <c:if test="${empty linkURL}">
				      <img src="${image.url}" alt="${image.description}" />
				    </c:if>								
				 </c:if>
				 							
				</c:forEach>
				
				<c:if test="${fn:length(page.images) gt 1}">
				<ul class="ep-hidden-images">
				<c:forEach var="image" items="${page.images}" varStatus="status">
				<c:if test="${status.count > 1}">
				
				 <c:url value="${image.url}" var="linkURL" context="/" />
				   <c:if test="${empty image.linkURL}">
				     <c:if test="${not empty image.viewURL}">
				       <c:url value="${image.viewURL}" var="linkURL" context="/">
		 	            <c:param name="epbox" value="" />
		                <c:param name="gallery" value="gal${page.id}" />
		               </c:url>
		             </c:if>
		           </c:if>
			     <c:if test="${not empty image.linkURL}">
				   <c:url value="${image.linkURL}" var="linkURL" context="/">
				     <c:param name="epbox" value="" />
				     <c:param name="gallery" value="gal${page.id}" />
				   </c:url>
			     </c:if>
				 <c:if test="${status.count == 1}">								
					<a class="${image.cssClass}" href="${linkURL}" title="${image.description}" rel="gal${comp.id}">
				      <img src="${image.url}" alt="${image.description}" />
				      <span class="endbox">&nbsp;</span>
					</a>									
				 </c:if>
				 
				 <li>
					<a class="${image.cssClass}" href="${linkURL}" title="${image.description}" rel="gal${comp.id}">
					    <span lang="en">download</span>
					</a>
				 </li>
				</c:if>	
					</c:forEach>
					</ul>
				</c:if>
			</span>
		<!-- Texte de l'element -->
		<div class="description">
			<p>${page.description}</p>
		</div>
	</div>
</c:forEach>
</div>