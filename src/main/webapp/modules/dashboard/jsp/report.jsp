<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<br class="all" />

<div class="invoice">
	
	<div class="invoice_inner">
		  <span class="lang">${info.language}</span>
		  <span class="total">#pages : ${report.totalPageAnnalysed}</span>
		  <c:if test="${not empty reportFilter.startDateLabel}"><span class="date">from : ${reportFilter.startDateLabel}</span></c:if>
		  <h2 class="title">Report : ${info.globalTitle}</h2>
		  <br clear="all" /><br />	  		  
    	  <div class="one_half">
    	  <h3 class="title">Pages structure</h3>
    	  <div class="circle">
    	  <jsp:include page="circle.jsp?value=${report.globalComponentScore}&label=Total" />
    	  </div>
    	  </div>
    	  <div class="one_half last">
    	  <h3 class="title">Check list</h3>
    	  <div class="circle">
    	  <jsp:include page="circle.jsp?value=${report.globalCriteriaScore}&label=Total" />
    	  </div>
    	  </div>
          <br class="all" /><br class="all" />
		  <div class="one_half">
		  <h3 class="title">Details</h3>
		  <div class="progress">
          	  Page with title
              <div class="bar2"><div class="value orangebar" style="width: ${report.pageTitle}%;"><small>${report.pageTitle}%</small></div>
              <div class="subbar">
              	<div class="value" style="background-color: #4DA74D; width: ${report.pageTitleOkSize}%;" title="right size"><small>${report.pageTitleOkSize}%</small></div>
 				<div class="value redbar" style="width: ${report.pageTitleBadSize}%;" title="bad size"><small>${report.pageTitleBadSize}%</small></div>             	
              </div>
              </div>
          </div>
          <div class="progress">
              Page with description
              <div class="bar2">
              	<div class="value orangebar" style="width: ${report.pageDescription}%;"><small>${report.pageDescription}%</small></div>
              
              <div class="subbar">
              	<div class="value" style="background-color: #4DA74D; width: ${report.pageDescriptionOkSize}%;" title="right size"><small>${report.pageDescriptionOkSize}%</small></div>
 				<div class="value redbar" style="width: ${report.pageDescriptionBadSize}%;" title="bad size"><small>${report.pageDescriptionBadSize}%</small></div>             	
              </div>
              </div>              
          </div>          
          <div class="progress">
          	  Page with right title structure
              <div class="bar2"><div class="value" style="background-color: #4DA74D; width: ${report.pageTitleStructure}%;"><small>${report.pageTitleStructure}%</small></div></div>
          </div>          
          <div class="progress">
          	  Page with label on all images
              <div class="bar2"><div class="value" style="background-color: #4DA74D;  width: ${report.pageImageAlt}%;"><small>${report.pageImageAlt}%</small></div></div>
          </div>
          </div><div class="one_half last">
          <h3 class="title">Content status</h3>
          <table cellpadding="0" cellspacing="0" class="invoicetable" width="100%">
           	<thead>
               	<tr>
                   	<td width="60%">Criteria</td>
                       <td width="20%" align="right">result</td>                            
                       <td width="20%" align="right">#total</td>
                   </tr>
               </thead>
               <tbody>                    	
                   <tr>
                   	<td>Empty page</td>                            
                       <td align="right">${report.pageWithoutContent}</td>
                       <td align="right">${report.pageWithContent}</td>
                   </tr>
                   <tr>
                   	<td>Bad external link (max:${report.maxLinkCheck})
                   	<c:if test="${report.badExternalLink > 0}">
                      		<ul>
                      			<c:forEach var="link" items="${report.badExternalLinkPages}">
                      				<li><a href="${link.url}">${link.label}</a>
                      			</c:forEach>
                      		</ul>
                      	</c:if>
                   	</td>
                   	<c:if test="${info.internetAccess}">
                       <td align="right">${report.badExternalLink}</td>
                       <td align="right">${report.badExternalLink+report.rightExternalLink}</td>
                       </c:if>
                       <c:if test="${!info.internetAccess}">
                       <td align="right">?</td>
                       <td align="right">?</td>                            
                       </c:if>
                   </tr>
                   <tr>
                   	<td>Bad internal link (max:${report.maxLinkCheck})
                   	<c:if test="${report.badInternalLink > 0}">
                      		<ul>
                      			<c:forEach var="link" items="${report.badInternalLinkPages}">
                      				<li><a href="${link.url}">${link.label}</a>
                      			</c:forEach>
                      		</ul>
                      	</c:if>
                      	</td>                            
                       <td align="right">${report.badInternalLink}</td>
                       <td align="right">${report.badInternalLink+report.rightInternalLink}</td>
                   </tr>                    
                   <tr>
                   <tr>
                   	<td>Bad ressouce link</td>                            
                       <td align="right">${fn:length(report.badResourceLinkPages)}</td>
                       <td align="right">&nbsp;</td>
                   </tr>                    
                   <tr>
                   	<td colspan="3">
                   		<br class="all" />
                   		<c:if test="${report.allTitleDifferent}">
                   			<div class="notification msgsuccess"><p>All titles is different.</p></div>
                   		</c:if><c:if test="${!report.allTitleDifferent}">
                   			<div class="notification msgerror"><p>At least two pages have the same title. <a href="${report.sameTitlePage1.url}">[1]</a> <a href="${report.sameTitlePage2.url}">[2]</a></p></div>
                   		</c:if>
					</td>
                   </tr>
                   
               </tbody>
             </table>
          </div>
     </div>
</div>
