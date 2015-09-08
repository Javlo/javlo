// Only do anything if jQuery isn't defined
if (typeof jQuery == 'undefined') {	
	function getScript(url, success) {
		var script     = document.createElement('script');
		     script.src = url;		
		var head = document.getElementsByTagName('head')[0],
		done = false;
		script.onload = script.onreadystatechange = function() {		
			if (!done && (!this.readyState || this.readyState == 'loaded' || this.readyState == 'complete')) {			
				done = true;				
				success();				
				script.onload = script.onreadystatechange = null;
				head.removeChild(script);				
			};
		};		
		head.appendChild(script);	
	};
	
	getScript('http://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js', function() {
	
		if (typeof jQuery=='undefined') {		
			alert("error loading jQuery.");		
		} else {
			jQuery(document).ready(function() {
				jQuery("._area").click(function() {
					var area = jQuery(this);
					jQuery("._area").removeClass("area_active");
					area.addClass("area_active");		
					window.parent.updateProperties(jQuery(this).attr("id"));		
				});
			});		
		}
	
	});
	
}