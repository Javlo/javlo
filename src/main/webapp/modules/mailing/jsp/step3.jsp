<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div class="${contentContext.editPreview?'preview ':'edit '}content wizard">
	<form class="${contentContext.editPreview?'':'ajax '} standard-form" action="${info.currentURL}" method="post">
		<div class="main">
		<div>
			<input type="hidden" name="webaction" value="wizard" />
			<input type="hidden" name="box" value="sendwizard" />
			<c:if test="${contentContext.editPreview}"><input type="hidden" name="previewEdit" value="true" /></c:if>
		</div>
		<h4>${confirmMessage}</h4>
		<ul id="mail-list">
			<c:forEach var="email" items="${mailing.allRecipients}">
				<li><c:out value="${email}" escapeXml="true" /></li>
			</c:forEach>
		</ul>
		<c:if test="${contentContext.editPreview}">
			<div class="mailing-preview">
			<iframe id="mailing-preview-frame" src="${previewURL}" onload="autoIframe('mailing-preview-frame');"></iframe>
			</div>
		</c:if>
		</div>
		<div class="action pull-right btn-group">
			<button type="submit" class="btn btn-default" name="previous">Previous</button>	
			<button type="submit" class="btn btn-primary btn-color " name="send">Send</button>					
		</div>
	</form>
	
	<script type="text/javascript">
    function autoIframe(frameId) {
       try {
          frame = document.getElementById(frameId);
          innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;                    
          jQuery(frame).height( innerDoc.body.scrollHeight + 10 );          
          jQuery("#mail-list").height( innerDoc.body.scrollHeight + 10 );
       }
       catch(err) {
          window.status = err.message;
       }
    }
	</script>
	
	
	
</div>
