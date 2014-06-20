jQuery(document).ready(function(){
	initReaction();	
});

function initReaction() {
	jQuery(".reaction li .reaction-form form").each(function() {
		var formWrapper = jQuery(this);
		formWrapper.hide();
		var title = formWrapper.attr("title");
		if (title == null || title.length == 0) {
			title="reply";
		}
		jQuery('<div class="reply-wrapper"><input class="reply" type="button" value="'+title+'" /></div>').insertBefore(formWrapper);
		formWrapper.parent().find(".reply").click(function() {
			jQuery(this).hide();
			jQuery(this).parent().parent().find("form").show();
		});
	});
}