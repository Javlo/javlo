jQuery(document).ready(function() {
	jQuery("._area").click(function() {
		var area = jQuery(this);
		jQuery("._area").removeClass("area_active");
		area.addClass("area_active");		
		window.parent.updateProperties(jQuery(this).attr("id"));		
	});
});