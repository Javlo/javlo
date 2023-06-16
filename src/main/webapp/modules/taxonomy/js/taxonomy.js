console.log(">>> taxonomy.js V2.0.2");

var actionItemOpen = [];

function isOpen(item) {
	if (item.id.endsWith("-0")) {
		return true;
	}
	if (actionItemOpen[item.id] == null) {
		return false;
	} else {
		return actionItemOpen[item.id];
	}
}

function close(item) {
	if (item.classList.contains("open")) {
		actionItemOpen[item.id] = false;
		item.classList.remove("open");
		item.classList.add("close");
		children = item.parentElement.parentElement.getElementsByTagName("ul");
		for (var i = 0; i < children.length; i++) {
			if (children[i].parentElement == item.parentElement.parentElement) {
				children[i].classList.add("hidden");
			}
		}
	}
}

function open(item) {
	if (item.classList.contains("close")) {
		actionItemOpen[item.id] = true;
		item.classList.remove("close");
		item.classList.add("open");
		children = item.parentElement.parentElement.getElementsByTagName("ul");
		for (var i = 0; i < children.length; i++) {
			if (children[i].parentElement == item.parentElement.parentElement) {
				children[i].classList.remove("hidden");
			}
		}
	}
}

function changeAllStatus(status) {
	document.querySelectorAll(".action-list").forEach(i =>  {
		if (!i.id.endsWith("-0") || status) {
			if (status) {
				open(i);
			} else {
				close(i);
			}
		}
	});
}

function refreshAllStatus() {
	document.querySelectorAll(".action-list").forEach(i =>  {
		if (isOpen(i)) {
			open(i);
		} else {
			close(i);
		}
	});
}

jQuery(document).ready(function(){
	jQuery('.action-list').live("click", function(e) {
		if (e.preventDefault) {
			e.preventDefault(); // Necessary. Allows us to drop.
		}
		action = jQuery(this);
		if (action.hasClass('open')) {
			close(this);
		} else {
			open(this);
		}
		return false;
	});	
	jQuery('#taxonomy-form').on('ajaxUpdate', function() {
		console.log("- ",jQuery(this).find(".needfocus"));
		jQuery(this).find(".needfocus").focus();		
		jQuery(this).find(".needfocus").removeClass('needfocus');
		document.getElementById("moveto").value="";
		document.getElementById("moved").value="";
		addTaxoDragEvents();
		refreshAllStatus();
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
	
	function submitForm(e) {
		if (e != null && e.preventDefault) {
			e.preventDefault(); // Necessary. Allows us to drop.
		}
		var	form = jQuery("#taxonomy-form");
		var ajaxSubmit = true;
		if (form.data("ajaxSubmit") != null) {
			ajaxSubmit = form.data("ajaxSubmit");
		}
		if (ajaxSubmit) {			
//			event.preventDefault();
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

			console.log("moveto   = "+this.dataset.id);
			console.log("moved    = "+dragSrcEl.dataset.id);
			console.log("aschild  = "+this.dataset.aschild);

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
		console.log("...addTaxoDragEvents...");
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

	refreshAllStatus();
	
});