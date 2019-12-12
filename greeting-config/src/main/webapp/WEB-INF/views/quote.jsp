<%@ include file="/WEB-INF/views/_taglibs.jspf"
%><!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>Greetings</title>
	<link href="<c:url value="/favicon.ico?v=2" />" rel="icon" type="image/png" />
	<link type="text/css" rel="stylesheet" href="<c:url value="/webjars/bootstrap/css/bootstrap.min.css" />" />
</head>
<body>
<div class="container" style="padding-top: 20%">
	<div class="row">
		<div class="col-xs-12 col-md-10 col-lg-6 col-md-offset-1 col-lg-offset-3">
		    <h1 class="text-center">${quote.text}</h1>
		    <h2 class="text-center">${quote.author}</h2>
		    <p class="text-center">This quote originated from: ${uri}</p>
		</div>
	</div>
</div>
</body>
</html>
