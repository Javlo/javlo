<!doctype html>
<!--[if IE 8]><html class="ie ie8"><![endif]-->
<!--[if IE 9]><html class="ie ie9"><![endif]-->
<html>
    <head>
    	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <meta http-equiv="X-UA-Compatible" content="IE=edge" />
        
        <title>NewsGate (Login)</title>
        <link rel="shortcut icon" href="img/assets/favicon.ico" />
        
        <link href='http://fonts.googleapis.com/css?family=Open+Sans:300,400,700' rel='stylesheet' type='text/css' />
        <link href="${info.editTemplateURL}/css/main.css" rel="stylesheet" type="text/css" />
        
        <!--[if lt IE 10]><link href="css/assets/ie.css" rel="stylesheet" type="text/css"><![endif]-->
        <script src="${info.editTemplateURL}/js/assets/jquery-1.11.1.min.js"></script>
        <script src="${info.editTemplateURL}/js/assets/jquery.dataTables.min.js"></script>
        <script src="${info.editTemplateURL}/js/assets/jquery.flexslider-min.js"></script>
        <script src="${info.editTemplateURL}/js/assets/jquery-ui.min.js"></script>
        <script src="${info.editTemplateURL}/js/assets/jquery.tooltipster.min.js"></script>
        <script src="${info.editTemplateURL}/js/assets/plugins.js"></script>
        
        <!-- Add fancyBox main JS and CSS files -->
		<script type="text/javascript" src="${info.editTemplateURL}/fancybox/source/jquery.fancybox.js"></script>
		<link rel="stylesheet" type="text/css" href="${info.editTemplateURL}/fancybox/source/jquery.fancybox.css" media="screen" />
    </head>
    
    <body class="login-page">
    
    	<div id="bg-image">
        	<div class="flexslider">
            	<ul class="slides">
                	<li class="slide-1"></li>
                    <li class="slide-2"></li>
                    <li class="slide-3"></li>
                    <li class="slide-4"></li>
                    <li class="slide-5"></li>
                </ul>
            </div><!-- End-Flexslider -->
        </div><!-- End-Bg-Image -->
        
        
        <div class="wrapper for-header">
        	<div class="container">
            	<div id="header">
                    
                    <div id="logo"><a href="#"></a></div><!-- End-Logo -->
                    <div id="menu">
                        <ul>
                            <li class="home active"><a href="index.html"><span>Home</span></a></li>
                            <li class="new"><a href="new-1.html"><span>Create new mailing</span></a></li>
                            <li class="file"><a href="files.html"><span>Files</span></a></li>	
                        </ul>
                    </div><!-- End-Menu -->
                    <div id="presentation">
                        <ul>
                            <li class="icon icon-info"><a href="presentation.html" data-toggle="tooltip" data-placement="bottom" title="NewsGate"></a></li>
                        </ul>
                    </div><!-- End-Presentation -->
                    <div id="admin">
                        <ul>
                            <li class="icon icon-user">Fedulov Filip</li>
                            <li><a href="settings.html">Settings</a></li> 
                            <li><a href="login.html">Logout</a></li>              
                        </ul>
                    </div><!-- End-Admin -->
                    
                </div><!-- End-Header -->
            </div><!-- End-Container -->
        </div><!-- End-Wrapper (For-Header) -->
        
        
        <div class="wrapper for-content">
        	<div class="container">
            	<div id="content">
                <c:if test="${not empty info.globalMessage && not empty info.globalMessage.message}">
    				<div class="notification notifyError loginNotify">${info.globalMessage}</div>
				</c:if>
                    <div id="main">
                    	<div class="page-title">
                        	<h1>NewsGate mailing platform</h1>
                        </div><!-- End-Page-Title -->
                        <div id="login-box">
                        	<form action="${info.currentEditURL}" method="post">
                        		<c:if test="${not empty param.previewEdit}">
    								<input type="hidden" name="previewEdit" value="${param.previewEdit}" />
    							</c:if>
    							<input type="hidden" value="adminlogin" name="login-type" />
    							<input type="hidden" value="edit-login" name="edit-login" />    							
                            	<input id="j_username" class="username" type="text" name="j_username" placeholder="User name" />
								<input id="j_password" class="password" type="password" name="j_password" placeholder="Password" />
								<button class="submit" name="submit">Login</button>
                            </form>
                        </div><!-- End-Login-Box -->
                    </div><!-- End-Main -->
                    
                </div><!-- End-Content -->
            </div><!-- End-Container -->
        </div><!-- End-Wrapper (For-Content) -->
        
        
        <!-- Script only on login-page !!! -->
        <script>
        	$(document).ready(function(){
				setTimeout(function(){ 
					$("#info").slideToggle(600);
					$(".info-close").addClass("open");
					$(".info-close").parents(".for-info").addClass("open");
				}, 1000);
			});
        </script>
        <script src="${info.editTemplateURL}/js/interface.js"></script>
        
    </body>
</html>
