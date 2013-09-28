<!-- PREVIEW CODE --!>
<%if (ctx.isInteractiveMode() && ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
%><jsp:include page="/jsp/preview/command.jsp" />
<%}%>
<%if (ctx.isInteractiveMode() && ctx.getRenderMode() == ContentContext.TIME_MODE) {%>
<jsp:include page="/jsp/time-traveler/command.jsp" />
<%MessageRepository messageRepository = MessageRepository.getInstance(ctx);
            %><div id="message-container" class="standard"><%
   if (messageRepository.getGlobalMessage().getMessage().trim().length() > 0) {%>
       <div class="notification <%=messageRepository.getGlobalMessage().getTypeLabel()%>"><%=messageRepository.getGlobalMessage().getMessage()%></div>
<%}%></div>
<%}%>
<!-- /PREVIEW CODE --!>
