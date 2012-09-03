<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<a rel="shadowbox" class="${type}" href="${url}">
	<img src="${previewURL}" alt="${not empty label?label:description}" />
</a>