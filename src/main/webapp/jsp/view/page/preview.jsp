<!-- preview part -->
<%if (ctx.isInteractiveMode() && ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
%><jsp:include page="<%=ctx.getGlobalContext().getStaticConfig().getPreviewCommandFilePath()%>" />
<%}%>
<%if (ctx.isInteractiveMode() && ctx.getRenderMode() == ContentContext.TIME_MODE) {%>
<jsp:include page="<%=ctx.getGlobalContext().getStaticConfig().getTimeTravelerFilePath()%>" />
<%MessageRepository messageRepository = MessageRepository.getInstance(ctx);
            %><div id="message-container" class="standard"><%
   if (messageRepository.getGlobalMessage().getMessage().trim().length() > 0) {%>
       <div class="notification <%=messageRepository.getGlobalMessage().getTypeLabel()%>"><%=messageRepository.getGlobalMessage().getMessage()%></div>
<%}%></div>
<%}%>