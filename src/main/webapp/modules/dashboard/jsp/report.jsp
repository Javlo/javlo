<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<br class="all" />

<div class="invoice three_fourth">
	
	<div class="invoice_inner">
		  <h2 class="title">Report : ${info.globalTitle}</h2>
		  <br class="all" /><br class="all" />		  
    	  <div class="one_half">
    	  <h2 class="title">Pages structure</h2>
    	  <div class="circle">
    	  <jsp:include page="circle.jsp?value=${report.globalComponentScore}&color=#FF7200&label=Total" />
    	  </div>
    	  </div>
    	  <div class="one_half last">
    	  <h2 class="title">Check list</h2>
    	  <div class="circle">
    	  <jsp:include page="circle.jsp?value=${report.globalCriteriaScore}&color=#39870A&label=Total" />
    	  </div>
    	  </div>
          <br class="all" /><br class="all" />
		  <div class="one_half">
		  <h2 class="title">Details</h2>
		  <div class="progress">
          	  Page with title
              <div class="bar2"><div class="value" style="background-color: #4DA74D; width: ${report.pageTitle}%;"><small>${report.pageTitle}%</small></div></div>
          </div>
          <div class="progress">
              Page with description
              <div class="bar2"><div class="value bluebar" style="width: ${report.pageDescription}%;"><small>${report.pageDescription}%</small></div></div>
          </div>          
          <div class="progress">
          	  Page with right title structure
              <div class="bar2"><div class="value orangebar" style="width: ${report.pageTitleStructure}%;"><small>${report.pageTitleStructure}%</small></div></div>
          </div>          
          <div class="progress">
          	  Page with label on all images
              <div class="bar2"><div class="value redbar" style="width: ${report.pageImageAlt}%;"><small>${report.pageImageAlt}%</small></div></div>
          </div>
          
          
          
          </div>
          <div class="one_half last">
          <h2 class="title">Content status</h2>
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
                        	<td>Bad external link</td>                            
                            <td align="right">${report.badExternalLink}</td>
                            <td align="right">${report.badExternalLink+report.rightExternalLink}</td>
                        </tr>
                        <tr>
                        	<td>Bad internal link</td>                            
                            <td align="right">${report.badInternalLink}</td>
                            <td align="right">${report.badInternalLink+report.rightInternalLink}</td>
                        </tr>
                        
                    </tbody>
                </table>
          </div>
     </div>
</div>
