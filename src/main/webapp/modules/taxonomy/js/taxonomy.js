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
	});	
	jQuery('#taxonomy-form').on('ajaxUpdate', function() {
		console.log("- ",jQuery(this).find(".needfocus"));
		jQuery(this).find(".needfocus").focus();		
		jQuery(this).find(".needfocus").removeClass('needfocus');
		document.getElementById("moveto").value="";
		document.getElementById("moved").value="";
		return true;
	});

	/** drag&drop **/	
	var dragSrcEl = null;
	
	function handleDragStart(e) {	
		dragSrcEl = this;
		this.style.opacity = '0.4';
	}
	
	function handleDragEnd(e) {		
		this.style.opacity = '1';
	}
	
	function handleDrop(e) {
		if (dragSrcEl != this) {
			moveto = document.getElementById("moveto");
			moveto.value = this.dataset.id;
			moved = document.getElementById("moved");			
			moved.value = dragSrcEl.dataset.id;
			aschild = document.getElementById("aschild");			
			aschild.value = this.dataset.aschild;
			console.log("this.dataset.aschild = "+this.dataset.aschild);
			document.getElementById("taxonomy-form").submit();
		}
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
	}

	function handleDragLeave(e) {
	  this.classList.remove('over');  // this / e.target is previous target element.
	}
	
	var cols = document.querySelectorAll('.item-wrapper');
	[].forEach.call(cols, function(col) {
	  col.addEventListener('dragstart', handleDragStart, false);
	  col.addEventListener('dragend', handleDragEnd, false);
	  col.addEventListener('drop', handleDrop, false);
	  col.addEventListener('dragenter', handleDragEnter, false);
	  col.addEventListener('dragover', handleDragOver, false);
	  col.addEventListener('dragleave', handleDragLeave, false);
	});


});