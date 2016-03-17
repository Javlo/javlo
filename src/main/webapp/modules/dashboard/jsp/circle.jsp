<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><svg xmlns="http://www.w3.org/2000/svg" version="1.1"
	viewBox="0 0 220 186" class="circliful">
	<circle cx="100" cy="100" r="57" class="border" fill="none"
		stroke="#ccc" stroke-width="20" stroke-dasharray="360"
		transform="rotate(-90,100,100)"></circle>
	<circle class="circle" cx="100" cy="100" r="57" fill="none"
		stroke="${param.color}" stroke-width="20"
		stroke-dasharray="${360*(param.value/100)}, 20000"
		transform="rotate(-90,100,100)"></circle>
	<text class="timer" text-anchor="middle" x="100" y="110"
		style="font-size: 22px;" fill="#aaa">${param.value}%</text>
	<c:if test="${not empty param.label}">
		<g stroke="#ccc">
		<line x1="133" y1="50" x2="140" y2="40" stroke-width="2"></line></g>
		<g stroke="#ccc">
		<line x1="140" y1="40" x2="200" y2="40" stroke-width="2"></line></g>
		<text class="timer" text-anchor="middle" x="175" y="35"
		style="font-size: 22px; undefined;" fill="#aaa">${param.label}</text></c:if>
</svg>