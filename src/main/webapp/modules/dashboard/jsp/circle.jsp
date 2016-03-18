<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><c:if test="${not empty param.color}"><c:set var="color" value="${param.color}" /></c:if>
<c:if test="${empty param.color}">	
	<c:if test="${param.value < 75}">
		<c:set var="color" value="#EDC240" />
	</c:if>	
	<c:if test="${param.value < 50}">
		<c:set var="color" value="#FF7200" />
	</c:if>
	<c:if test="${param.value < 25}">
		<c:set var="color" value="#CC0000" />
	</c:if>
	<c:if test="${param.value > 75}">
		<c:set var="color" value="#39870A" />
	</c:if>
</c:if>
<svg xmlns="http://www.w3.org/2000/svg" version="1.1"
	viewBox="0 0 220 186" class="circliful">
	<circle cx="100" cy="100" r="57" class="border" fill="none"
		stroke="#ccc" stroke-width="20" stroke-dasharray="360"
		transform="rotate(-90,100,100)"></circle>
	<circle class="circle" cx="100" cy="100" r="57" fill="none"
		stroke="${color}" stroke-width="20"
		stroke-dasharray="${360*(param.value/100)}, 20000"
		transform="rotate(-90,100,100)"></circle>
	<text class="timer" text-anchor="middle" x="100" y="110"
		style="font-size: 22px;" fill="#aaa">${param.value}%</text>
	<c:if test="${not empty param.label}">	
		<g stroke="#ccc">
		<text class="timer" text-anchor="middle" x="175" y="35" style="font-size: 14px;" fill="#ccc">${param.label}</text>
		<g stroke="#ccc">		
		<polyline fill="none" stroke="black" style="fill:none;stroke:#ccc;stroke-width:1" points="133,50 140, 40 200,40" />		
	</c:if>	
</svg>