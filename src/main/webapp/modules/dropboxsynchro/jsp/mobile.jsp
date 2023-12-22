<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><!DOCTYPE html>
<html lang="${info.language}">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dropbox</title>
    <link href="../mobcss/mobile.css" rel="stylesheet" />    
</head>
<body>
<jsp:include page="actions.jsp"></jsp:include>
<script type="text/javascript">
function onMobileLoad() {
	var out = {
			message: '${messages.globalMessage.message}'
		};
	return JSON.stringify(out);
}
</script>
</body>