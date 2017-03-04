<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@
taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>
<c:set var="width" value="1200" />
<c:set var="max" value="${array[2][2].info.max}" />
<c:set var="scale" value="${400/max}" />
<c:set var="step" value="${((width-140)/(fn:length(array)-1))}" />
<svg width='${width+60}' height='500' viewBox='0 0 ${width} 500' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' xmlns:a3='http://ns.adobe.com/AdobeSVGViewerExtensions/3.0/' a3:scriptImplementation='Adobe'>
  <defs/>
  <!--SVG Background-->
  <rect width='${width}' height='500' x='0' y='0' class='svgBackground' style='fill:#ffffff;'/>
  <g transform='translate( 35.8 26 )'>    
    <path d='M 0 414.0 h${width-140}' class='axis' id='yAxis' style='stroke: #000000; stroke-width: 1px;'/>
    <path d='M 0.0 0 v414.0' class='axis' id='xAxis' style='stroke: #000000; stroke-width: 1px;'/>    
	
	<c:forEach begin="1" end="10" varStatus="status">
	<text x='-3' y='${418-(status.index*(414/status.end))}' class='yAxisLabels' style='text-anchor: end; fill: #000000; font-size: 12px; font-family: &quot;Arial&quot;, sans-serif; font-weight: normal;text-anchor: end'>${max*status.index/status.end}</text>
    <path d='M0 ${414-(status.index*(414/status.end))} h${width-140}' class='guideLines' style='stroke: #cccccc; stroke-width: 1px; stroke-dasharray: 5 5;'/>	
	</c:forEach>
	
    <c:forEach var="cell" items="${array}" varStatus="status">
	<c:if test="${cell[1].digit}">
    <rect x='${5+(status.index-1)*step}' y='${414-cell[1].value*scale}' width='${step-10}' height='${cell[1].value*scale}' class='fill1' style='fill: #ff0000; fill-opacity: 0.5; stroke: none; stroke-width: 0.5px;'/>		
	<text x='${5+(status.index-1)*step+((step-10)/2)}' y='${414-cell[1].value*scale-4}' class='barLabels' fill="red" text-anchor="middle">${cell[1].value}</text>
	<text x='${5+(status.index-1)*step+((step-10)/2)}' y='440' class='barLabels' fill="#000000" text-anchor="middle">${array[status.index][0].value}</text>
	</c:if>
	</c:forEach>
    
    
  </g>
  <text x='${width/2}' y='12' class='mainTitle' style='text-anchor: middle; fill: #000000; font-size: 14px; font-family: &quot;Arial&quot;, sans-serif; font-weight: normal;'>${summary}</text>
  <text x='${width-110}' y='480' class='xAxisTitle' style='fill: #000000; font-size: 14px; font-family: &quot;Arial&quot;, sans-serif; font-weight: normal;' text-anchor="end">${array[0][1].value}</text>
  <text x='0' y='15.0' class='yAxisTitle' style='fill: #000000; text-anchor: start; font-size: 14px; font-family: &quot;Arial&quot;, sans-serif; font-weight: normal;'>${array[1][0].value}</text>
  </svg>