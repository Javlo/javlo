<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<div class="content">
<p>This is the slide box 1 renderer</p>
<p><b>message : </b>${demoMessage}
<p><b>test css : </b><span class="demo-text">this text is modified by CSS.</span></p>
<p><b>test i18n : </b>${i18n.edit['demo.text']}</p>
</div>