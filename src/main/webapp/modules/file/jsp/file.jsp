<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${not empty param.changeRoot}">
	<c:set var="changeRoot" value="true" />
</c:if>
<c:if test="${not empty param.path}">
	<c:set var="ELPath" value="${param.path}" scope="session" />
</c:if>
<c:if test="${not info.editLanguage eq 'en'}"><script type="text/javascript" src="${currentModule.path}/js/i18n/elfinder.${info.editLanguage}.js"></script></c:if>
<div class="content nopadding">
<div id="fileManager" class="elfinder"></div>
</div>
<c:set var="params" value="" />
<c:if test="${not empty param.templateid}">
<c:set var="params" value="&templateid=${param.templateid}" />
</c:if>
<script type="text/javascript">
jQuery(document).ready(function() {
	var language = "${info.editLanguage}";
	changeFooter();
	jQuery('#fileManager').elfinder({
		url : '${info.staticRootURL eq "/"?"":info.staticRootURL}${currentModule.path}/jsp/connector.jsp${not empty changeRoot?"?changeRoot=true":""}${params}',
		lang : '${info.editLanguage}',
		height: jQuery("#footer").offset().top - jQuery("#fileManager").offset().top - (jQuery(".maincontent .left").outerHeight(true) - jQuery(".maincontent .left").height()),
		handlers : {	
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
					'rm', '|', 'edit', 'rename', 'resize', '|', 'archive', 'extract', '|', 'info'
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
	   			'rm', '|', 'edit', 'rename', 'resize', '|', 'archive', 'extract', '|', 'info'
	   		]
	   	},
		commandsOptions : {
		    edit : {
		      editors : [
		        {
		          mimes : ['text/html','text/properties','text/plain','text/jsp','text/css'],  // add here other mimes if required
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
