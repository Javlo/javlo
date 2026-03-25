<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%-- ============================================================
     Social & SEO meta tags
     Pratiques 2025 : Open Graph, X/Twitter Card, JSON-LD WebPage
     ============================================================ --%>
<%-- Variables locales pour eviter la repetition --%>
<c:set var="_desc"  value="${not empty info.pageDescription ? info.pageDescription : i18n.view['default.description']}"/>
<c:set var="_title" value="${info.pageTitleForAttribute}"/>
<c:set var="_url"   value="${info.currentAbsoluteURL}"/>
<c:set var="_img"   value="${info.facebookImageUrl}"/>
<c:set var="_date"  value="${info.page.contentDateValue}"/>
<c:set var="_type"  value="${info.page.depth gt 1 and not empty _date ? 'article' : 'website'}"/>

<%-- SEO standard --%>
<meta name="description" content="${fn:escapeXml(_desc)}"/>

<%-- Open Graph --%>
<meta property="og:site_name"   content="${fn:escapeXml(info.globalTitle)}"/>
<meta property="og:type"        content="${_type}"/>
<meta property="og:title"       content="${fn:escapeXml(_title)}"/>
<meta property="og:description" content="${fn:escapeXml(_desc)}"/>
<meta property="og:url"         content="${_url}"/>
<meta property="og:locale"      content="${fn:replace(info.languageAndCountry, '-', '_')}"/>
<c:if test="${not empty _img}">
  <meta property="og:image"       content="${_img}"/>
  <meta property="og:image:width"  content="1200"/>
  <meta property="og:image:height" content="630"/>
  <meta property="og:image:type"   content="image/jpeg"/>
  <meta property="og:image:alt"    content="${fn:escapeXml(_title)}"/>
</c:if>
<c:if test="${_type eq 'article'}">
  <meta property="article:published_time" content="${_date}"/>
  <c:if test="${not empty info.page.latestEditor}">
    <meta property="article:author" content="${fn:escapeXml(info.page.latestEditor)}"/>
  </c:if>
</c:if>

<%-- X / Twitter Card --%>
<meta name="twitter:card"        content="${not empty _img ? 'summary_large_image' : 'summary'}"/>
<meta name="twitter:title"       content="${fn:escapeXml(_title)}"/>
<meta name="twitter:description" content="${fn:escapeXml(_desc)}"/>
<c:if test="${not empty _img}">
  <meta name="twitter:image"       content="${_img}"/>
  <meta name="twitter:image:alt"   content="${fn:escapeXml(_title)}"/>
</c:if>
<c:if test="${not empty vi18n['social.twitter']}">
  <meta name="twitter:site"        content="${vi18n['social.twitter']}"/>
</c:if>

<%-- JSON-LD Schema.org --%>
<script type="application/ld+json">
  {
    "@context": "https://schema.org",
    "@type": "${_type eq 'article' ? 'Article' : 'WebPage'}",
  "name": "${fn:escapeXml(_title)}",
  "description": "${fn:escapeXml(_desc)}",
  "url": "${_url}"<c:if test="${not empty _img}">,
  "image": {
  "@type": "ImageObject",
  "url": "${_img}",
  "width": 1200,
  "height": 630
  }</c:if><c:if test="${_type eq 'article'}">,
  "datePublished": "${_date}"<c:if test="${not empty info.page.latestEditor}">,
    "author": { "@type": "Person", "name": "${fn:escapeXml(info.page.latestEditor)}" }</c:if></c:if>,
  "isPartOf": {
    "@type": "WebSite",
    "name": "${fn:escapeXml(info.globalTitle)}",
    "url": "${info.rootAbsoluteURL}"
  }
}
</script>
