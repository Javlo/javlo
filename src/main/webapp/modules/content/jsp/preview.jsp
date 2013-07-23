<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="box preview">
<h3><span>${i18n.edit['command.preview']} <a class="preview-link" href="${previewURL}" title="${i18n.edit['preview.popup']}">${i18n.edit['preview.popup']}</a></span></h3>

	<script type="text/javascript">
    function autoIframe(frameId) {
       try {
          frame = document.getElementById(frameId);
          innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;                    
          jQuery(frame).height( innerDoc.body.scrollHeight + 10 );
       }
       catch(err) {
          window.status = err.message;
       }
    }
	</script>

<div id="preview" class="content auto-height">
<iframe id="preview-frame" src="${previewURL}" class="full-height" onload="autoIframe('preview-frame');"></iframe>
</div>
</div>
