console.log(">>> taxonomy.js V2.1.0");

var actionItemOpen = [];
var dragSrcEl = null;

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
		var children = item.parentElement.parentElement.getElementsByTagName("ul");
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
		var children = item.parentElement.parentElement.getElementsByTagName("ul");
		for (var i = 0; i < children.length; i++) {
			if (children[i].parentElement == item.parentElement.parentElement) {
				children[i].classList.remove("hidden");
			}
		}
	}
}

function changeAllStatus(status) {
	document.querySelectorAll(".action-list").forEach(function(i) {
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
	document.querySelectorAll(".action-list").forEach(function(i) {
		if (isOpen(i)) {
			open(i);
		} else {
			close(i);
		}
	});
}

jQuery(document).ready(function() {

	/* ── Expand / collapse tree nodes ── */
	jQuery(document).on('click', '.action-list', function(e) {
		e.preventDefault();
		if (jQuery(this).hasClass('open')) {
			close(this);
		} else {
			open(this);
		}
		return false;
	});

	/* ── Toggle translation detail panel ── */
	jQuery(document).on('click', '.taxo-detail .action', function(e) {
		e.preventDefault();
		var $action = jQuery(this);
		var $panel  = $action.closest('.taxo-detail').find('.translation.bloc');
		if ($action.hasClass('close')) {
			$action.removeClass('close').addClass('open');
			$panel.removeClass('hidden');
		} else {
			$action.removeClass('open').addClass('close');
			$panel.addClass('hidden');
		}
		return false;
	});

	/* ── AJAX update callback ── */
	jQuery('#taxonomy-form').on('ajaxUpdate', function() {
		jQuery(this).find(".needfocus").focus();
		jQuery(this).find(".needfocus").removeClass('needfocus');
		document.getElementById("moveto").value = "";
		document.getElementById("moved").value  = "";
		addTaxoDragEvents();
		refreshAllStatus();
		return true;
	});

	/* ── Drag & drop ── */
	function handleDragStart(e) {
		dragSrcEl = this;
		this.style.opacity = '0.4';
	}

	function handleDragEnd(e) {
		e.preventDefault();
		this.style.opacity = '1';
		return false;
	}

	function submitForm(e) {
		if (e != null && e.preventDefault) {
			e.preventDefault();
		}
		var form = jQuery("#taxonomy-form");
		var ajaxSubmit = true;
		if (form.data("ajaxSubmit") != null) {
			ajaxSubmit = form.data("ajaxSubmit");
		}
		if (ajaxSubmit) {
			jQuery("#ajax-loader").addClass("active");
			jQuery(".ajax-loader").addClass("active");
			var queryString = form.attr("action");
			ajaxRequest(queryString, form[0], addTaxoDragEvents);
			return false;
		} else {
			return true;
		}
	}
	window.submitTaxonomyForm = submitForm;

	function handleDrop(e) {
		e.preventDefault();
		if (dragSrcEl != null && dragSrcEl != this) {
			document.getElementById("moveto").value  = this.dataset.id;
			document.getElementById("moved").value   = dragSrcEl.dataset.id;
			document.getElementById("aschild").value = this.dataset.aschild;
			submitForm();
		}
		return false;
	}

	function handleDragOver(e) {
		e.preventDefault();
		e.dataTransfer.dropEffect = 'move';
		return false;
	}

	function handleDragEnter(e) {
		this.classList.add('over');
		return false;
	}

	function handleDragLeave(e) {
		this.classList.remove('over');
		return false;
	}

	function addTaxoDragEvents() {
		var cols = document.querySelectorAll('.item-wrapper');
		[].forEach.call(cols, function(col) {
			col.addEventListener('dragstart',  handleDragStart, false);
			col.addEventListener('dragend',    handleDragEnd,   false);
			col.addEventListener('drop',       handleDrop,      false);
			col.addEventListener('dragenter',  handleDragEnter, false);
			col.addEventListener('dragover',   handleDragOver,  false);
			col.addEventListener('dragleave',  handleDragLeave, false);
		});
	}
	window.addTaxoDragEvents = addTaxoDragEvents;

	addTaxoDragEvents();
	refreshAllStatus();
});
