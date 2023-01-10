<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${not empty param.changeRoot}">
	<c:set var="changeRoot" value="true" />
</c:if>
<c:if test="${not empty param.path}">
	<c:set var="ELPath" value="${param.path}" scope="session" />
</c:if>

<link rel="stylesheet" href="${info.currentModuleURL}/elfinder/css/elfinder.full.css">
<link rel="stylesheet" href="${info.currentModuleURL}/elfinder/css/theme-javlo.css">

<c:if test="${not info.editLanguage eq 'en'}"><script type="text/javascript" src="${currentModule.path}/js/i18n/elfinder.${info.editLanguage}.js"></script></c:if>
<div class="content nopadding">
<div id="fileManager" class="elfinder"></div>
</div>
<c:set var="params" value="" />
<c:if test="${not empty param.templateid}">
<c:set var="params" value="&templateid=${param.templateid}" />
</c:if>
<c:if test="${not empty param.previewEdit}">
<c:set var="params" value="${params}&previewEdit=${param.previewEdit}" />
</c:if>
<script type="text/javascript">
jQuery(document).ready(function() {
	var language = "${info.editLanguage}";
	changeFooter();
	
	if (document.getElementById("footer") != null) {
		var footerTop = jQuery("#footer").offset().top;
	} else {
		if (window.parent != null && window.parent.document != null  && window.parent.document.getElementById("preview-modal-frame") != null) {
			var footerTop = window.parent.document.getElementById("preview-modal-frame").offsetHeight;
		} else {
			var footerTop = Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);
		}
	}
	
	jQuery('#fileManager').elfinder({
		url : '${info.staticRootURL eq "/"?"":info.staticRootURL}${currentModule.path}/jsp/connector.jsp?v=1${not empty changeRoot?"&changeRoot=true":""}${params}  ',
		lang : '${info.editLanguage}',
		height: footerTop - jQuery("#fileManager").offset().top - (jQuery(".maincontent .left").outerHeight(true) - jQuery(".maincontent .left").height()),
		handlers: {	
			open: function(event) { ajaxRequest("${info.currentURL}?webaction=updateBreadcrumb${not empty changeRoot?"&changeRoot=true":""}${params}"); }
		},
		
		uiOptions : {
			contextmenu : {
				// navbarfolder menu
				navbar : ['open', '|', 'copy', 'cut', 'paste', 'duplicate', '|', 'rm', '|', 'info'],

				// current directory menu
				cwd    : ['reload', 'back', '|', 'upload', 'mkdir', 'mkfile', 'paste', '|', 'info'],

				// current directory file menu
				files  : [
					'getfile', '|','open','|', 'copy', 'cut', 'paste', 'duplicate', '|',
					'rm', '|', 'edit', 'rename', '|', 'archive', 'extract', '|', 'info'
				]
			},
		toolbar : [
		   		['back', 'forward'],
 		   		['mkdir', 'mkfile', 'upload'],
		   		['open', 'getfile'],
		   		['info'],
		   		/*['quicklook'],*/
		   		['copy', 'cut', 'paste'],
		   		['rm'],
		   		['duplicate', 'rename', 'edit', 'resize'],
		   		['extract', 'archive'],
		   		['search'],
		   		['view'],
		   		['help']
		   	] },
	   	contextmenu : {
	   		// navbarfolder menu
	   		navbar : ['open', '|', 'copy', 'cut', 'paste', 'duplicate', '|', 'rm', '|', 'info'],

	   		// current directory menu
	   		cwd    : ['reload', 'back', '|', 'upload', 'mkdir', 'mkfile', 'paste', '|', 'info'],

	   		// current directory file menu
	   		files  : [
	   			'getfile', '|','open', '|', 'copy', 'cut', 'paste', 'duplicate', '|',
	   			'rm', '|', 'edit', 'rename', '|', 'archive', 'extract', '|', 'info'
	   		]
	   	},
		commandsOptions : {
		    edit : {
		      editors : [
		        {
		          mimes : ['text/html','text/properties','text/plain','text/jsp','text/css','text/scss','text/less'],  // add here other mimes if required
		          load : function(textarea) {
		            openEditor(textarea);
		          },
		          close : function(textarea, instance) {
		            closeEditor(textarea);
		          },
		          save : function(textarea, editor) {		            
		            saveEditor(textarea);
		          }
		        }		        
		      ]
		    }
		  }
	}).elfinder('instance');	
});
</script>
