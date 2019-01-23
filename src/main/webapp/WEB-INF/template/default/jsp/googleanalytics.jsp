<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><c:if test="${!info.device.pdf}">
  <!-- Global site tag (gtag.js) - Google Analytics 23/01/2019 -->
<c:if test="${cookiesService.accepted}">
<script async src="https://www.googletagmanager.com/gtag/js?id=${globalContext.googleAnalyticsUACCT}"></script>
<script>
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());
  gtag('config', '${globalContext.googleAnalyticsUACCT}');
</script>
</c:if><c:if test="${!cookiesService.accepted}">

<script>
function loadGoogleAnalytics() {  
  var script = document.createElement('script');
  script.src = 'https://www.googletagmanager.com/gtag/js?id=${globalContext.googleAnalyticsUACCT}';
  var head = document.getElementsByTagName("head")[0];
  head.appendChild(script);
  window.dataLayer = window.dataLayer || [];  function gtag(){dataLayer.push(arguments);}  gtag('js', new Date());  gtag('config', '${globalContext.googleAnalyticsUACCT}');
}
</script>

<div class="_cookie-cache" data-status="1" data-function="loadGoogleAnalytics"></div>
</c:if>	
</c:if>