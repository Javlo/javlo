var dragging = false;

jQuery(document).ready(
		function() {
			try {
				jQuery("#preview_command").draggable({
					handle : ".pc_header"
				});
				jQuery("body").after(
						'<div id="preview-layer"><span>&nbsp;</span></div>');
				jQuery("body").after(
						'<div id="droppable-layer"><span>&nbsp;</span></div>');
			} catch (err) {
				if (typeof console != 'undefined') {
					console.log("error on : " + err);
				}
			}

			initPreview();

			jQuery("body").mouseover(function() {
				if (!dragging) {
					layerOver(null);
				}
				return true;
			});
			jQuery("#preview-layer").click(
					function() {
						layerOver(null);
						jQuery(this).data("subItem").trigger("click",
								jQuery(this).data("subItem"));
						return true;
					});
			jQuery("#pc_change_template").click(function() {
				var elems = jQuery(this);
				var editURL = jQuery("#change_template_form").attr("action");
				var param = "";
				jQuery.colorbox({
					href : editURL + param,
					opacity : 0.6,
					iframe : true,
					width : "95%",
					height : "95%"
				});
				return false;
			});
		});

layerOver = function(item) {
	var layer = jQuery("#preview-layer");
	var insideLayer = jQuery("#preview-layer span");
	if (item == null) {
		// layer.fadeOut(10);
		layer.css("z-index", -1);
		// layer.animate({"top": 0, "left": 0, "width": 0, "height": 0},100);
		/*
		 * layer.css("top", 0); layer.css("left", 0); layer.css("width", 0);
		 * layer.css("height", 0);
		 */
	} else {
		var comp = jQuery(item);
		// layer.fadeIn(100);
		// console.log("layer.width() = "+layer.width());
		if (layer.width() > 0) {
			layer.css("z-index", 10000);
		}
		layer.css("top", comp.offset().top);
		layer.css("left", comp.offset().left);
		layer.css("width", comp.width());
		layer.css("height", comp.height());
		layer.data("subItem", comp);
	}
}

initPreview = function() {

	jQuery(".editable-component").click(function() {

		layerOver(null);

		var elems = jQuery(this);
		var editURL = editPreviewURL + "&comp_id=" + elems.attr("id");
		var param = "";
		if (jQuery("#search-result").length > 0) {
			param = "&noinsert=true";
		}
		jQuery.colorbox({
			href : editURL + param,
			opacity : 0.6,
			iframe : true,
			width : "95%",
			height : "95%"
		});
		return false;
	});

	jQuery(".editable-component").mouseover(function() {
		if (!dragging) {			
			layerOver(this);
		}
		return false;
	});

	jQuery("#preview-layer").on('dragover', function(e) {
		e.preventDefault();
		e.stopPropagation();
	});
	jQuery("#preview-layer").on('dragenter', function(e) {
		e.preventDefault();
		e.stopPropagation();
	});
	jQuery("#preview-layer").on('drop', function(e) {
		alert("drop");
		e.preventDefault();
		e.stopPropagation();
	});

	jQuery(".editable-component, #preview-delete-zone")
			.droppable(
					{
						cursor : 'move',
						greedy : true,
						tolerance : 'pointer',
						drop : function(event, ui) {
							var layer = jQuery("#preview-layer");
							var comp = layer.data("subItem");
							if (jQuery(comp).attr('id') != jQuery(this).attr(
									"id")) {

								layerOver(comp);

								var compId = jQuery(comp).attr("id").replace(
										"cp_", "");
								if (jQuery(this).attr("id") == "preview-delete-zone") {
									var ajaxURL = currentURL
											+ "?webaction=edit.delete&id="
											+ compId;
									jQuery(comp).remove();
								} else {
									jQuery(comp).insertAfter(jQuery(this));
									var previewId = jQuery(this).attr("id")
											.replace("cp_", "");
									var area = jQuery(comp).parent().attr("id");
									if (area == undefined) { // change
																// language div
																// or container
										area = jQuery(comp).parent().parent()
												.attr("id");
									}
									if (area == undefined) { // container +
																// language
										area = jQuery(comp).parent().parent()
												.parent().attr("id");
									}
									if (area == undefined) { // container +
																// container +
																// language
										area = jQuery(comp).parent().parent()
												.parent().parent().attr("id");
									}
									var ajaxURL = currentURL
											+ "?webaction=edit.moveComponent&comp-id="
											+ compId + "&previous=" + previewId
											+ "&area=" + area;
								}

								ajaxRequest(ajaxURL);

							}

						},
						over : function(event, ui) {
							dragging = true;
							var dropLayer = jQuery("#droppable-layer");
							var layer = jQuery("#preview-layer");
							var target = jQuery(this);
							if (dropLayer.offset.top < 0) {
								dropLayer.css("top", "" + target.offset().top
										+ "px");
								dropLayer.css("left", "" + target.offset().left
										+ "px");
							}
							dropLayer.animate({
								"top" : target.offset().top,
								"left" : target.offset().left,
								"width" : target.width(),
								"height" : target.height()
							}, 50);
							layer.animate({
								"width" : target.parent().width()
							}, 150);
						}
					});

	dragging = false;

	jQuery("#preview-layer").draggable({
		cursor : "move",
		start : function(event, ui) {
			dragging = true;
			var layer = jQuery("#preview-layer");

			jQuery(".free-edit-zone").addClass("droppable");
			jQuery("#preview-delete-zone").removeClass("hidden");
			jQuery("#preview_command .pc_body").addClass("hidden");

		},
		stop : function(event, ui) {
			console.log("drop");
			var dropLayer = jQuery("#droppable-layer");
			dropLayer.css("top", "-9999px");
			dragging = false;
			jQuery(".free-edit-zone").removeClass("droppable");
			jQuery("#preview_command .pc_body").removeClass("hidden");
			jQuery("#preview-delete-zone").addClass("hidden");
		}
	});
}

function doNothing(evt) {
	evt.stopPropagation();
	evt.preventDefault();
}
