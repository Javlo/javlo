jQuery(document).ready(function(){
	
	/**
	 * Add an hover effect in images (gallery.html)
	**/
	jQuery('.thumbview .thumb').hover(function(){
		var t = jQuery(this);
		t.find('.info').stop(true,true).fadeIn('slow');
	},function(){
		var t = jQuery(this);
		t.find('.info').stop(true,true).fadeOut('slow');
	});
	
	/**
	 * Sub Menu
	**/
	jQuery('.submenu a').click(function(){
		var id = jQuery(this).attr('href');
		jQuery('.submenu a').each(function(){
			jQuery(this).parent().removeClass('current');
			jQuery(jQuery(this).attr('href')).hide();
		});
		jQuery(this).parent().addClass('current');
		jQuery(id).fadeIn();
	});
	
	
	/**
	 * Gallery Colorbox
	**/
	jQuery(".thumb .view").colorbox({rel:'view'});
	jQuery(".listview .view").colorbox({rel:'listview'});
	jQuery(".popup").colorbox();
	
	
	/**
	 * Delete a single image in a row
	**/
	jQuery('.deleteimage').click(function(){
		var c = confirm("Are you sure you want to delete this image?");
		if(c) {
			jQuery(this).parents('tr').fadeOut();
		}
		return false;
	});

});
