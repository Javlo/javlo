jQuery(document).ready(function(){
	jQuery('.action-list').live("click", function() {
		action = jQuery(this);
		if (action.hasClass('open')) {
			action.removeClass('open');
			action.addClass('close');
			action.parent().parent().find('> ul').addClass("hidden");				
		} else {
			action.removeClass('close');
			action.addClass('open');
			action.parent().parent().find('> ul').removeClass("hidden");
		}
		return false;
	});	
	jQuery('#taxonomy-form').on('ajaxUpdate', function() {
		console.log("- ",jQuery(this).find(".needfocus"));
		jQuery(this).find(".needfocus").focus();		
		jQuery(this).find(".needfocus").removeClass('needfocus');
		document.getElementById("moveto").value="";
		document.getElementById("moved").value="";
		return true;
	});

	
	
	function handleDragStart(e) {	
		dragSrcEl = this;
		this.style.opacity = '0.4';
	}
	
	function handleDragEnd(e) {	
		if (e.preventDefault) {
		    e.preventDefault(); // Necessary. Allows us to drop.
		}
		this.style.opacity = '1';
		return false;
	}
	
	function submitForm() {
		var	form = jQuery("#taxonomy-form");
		var ajaxSubmit = true;
		if (form.data("ajaxSubmit") != null) {
			ajaxSubmit = form.data("ajaxSubmit");
		}
		if (ajaxSubmit) {			
			event.preventDefault();
			jQuery("#ajax-loader").addClass("active");
			jQuery(".ajax-loader").addClass("active");
			var queryString = form.attr("action"); 
			ajaxRequest(queryString, form[0], addTaxoDragEvents);
			return false;
		} else {
			return true;
		}
	}
	
	function handleDrop(e) {
	    if (e.preventDefault) {
		    e.preventDefault(); // Necessary. Allows us to drop.
		}
		if (dragSrcEl != this) {
			moveto = document.getElementById("moveto");
			moveto.value = this.dataset.id;
			moved = document.getElementById("moved");			
			moved.value = dragSrcEl.dataset.id;
			aschild = document.getElementById("aschild");			
			aschild.value = this.dataset.aschild;		
			//document.getElementById("taxonomy-form").submit();
			submitForm();
		}
		return false;
	}
	
	function handleDragOver(e) {
	  if (e.preventDefault) {
	    e.preventDefault(); // Necessary. Allows us to drop.
	  }

	  e.dataTransfer.dropEffect = 'move';  // See the section on the DataTransfer object.

	  return false;
	}
	
	function handleDragEnter(e) {
	  // this / e.target is the current hover target.
	  this.classList.add('over');
	  return false;
	}

	function handleDragLeave(e) {
	  this.classList.remove('over');  // this / e.target is previous target element.
	  return false;
	}
	
	function addTaxoDragEvents() {
		/** drag&drop **/	
		var dragSrcEl = null;
		var cols = document.querySelectorAll('.item-wrapper');
		[].forEach.call(cols, function(col) {
		  col.addEventListener('dragstart', handleDragStart, false);
		  col.addEventListener('dragend', handleDragEnd, false);
		  col.addEventListener('drop', handleDrop, false);
		  col.addEventListener('dragenter', handleDragEnter, false);
		  col.addEventListener('dragover', handleDragOver, false);
		  col.addEventListener('dragleave', handleDragLeave, false);
		});
	}
	addTaxoDragEvents();
	
});