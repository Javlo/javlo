jQuery(document).ready(function() {	
	var contentHeight = jQuery(".footer").offset().top - jQuery("#content-edit").offset().top;
	jQuery("#content-edit").css("height", contentHeight+"px");
});