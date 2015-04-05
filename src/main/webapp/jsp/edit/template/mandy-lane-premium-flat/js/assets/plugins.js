
	// Placeholder for old browser
	$.fn.placeholder = function () { 
		this.focus(function () {
			var input = $(this);
			if (input.val() === input.attr('placeholder')) {
				input.val('');
				input.removeClass('placeholder');
			}
		}).blur(function () {
			var input = $(this);
			if (input.val() === '' || input.val() === input.attr('placeholder')) {
				input.addClass('placeholder');
				input.val(input.attr('placeholder'));
			}
		}).blur().parents('form').submit(function () {
			$(this).find('[placeholder]').each(function () {
				var input = $(this);
				if (input.val() === input.attr('placeholder')) {
					input.val('');
				}
			});
		});
		return this;
	};

	//Checkbox with style & accessible
	$.fn.checkbox = function() {
		var inputs = this.find('input[type=checkbox]');
		inputs.css('opacity',0);
		inputs.each(function() {
			if ($(this).is(':checked')) {
				$(this).parent('label').addClass('checked');
			}
			$(this).on('change', function() {
				if($(this).is(':checked')) {
					$(this).parent('label').addClass('checked');
				} else {
					$(this).parent('label').removeClass('checked');
				}
			});
		});
		return this;
	};

	//Radio with style & accessible
	$.fn.radioButton = function() {
		var that = this;
		var inputs = this.find('input[type=radio]');
		inputs.css('opacity',0);
		inputs.each(function() {
			if ($(this).is(':checked')) {
				$(this).parent('label').addClass('checked');
			}
			$(this).focus(function() {
				$(this).parent('label').addClass('hover');
			}).blur(function() {
				$(this).parent('label').removeClass('hover');
			}).on('change', function() {
				that.find('input[name='+$(this).attr('name')+']').parent('label.radioButton').removeClass('checked');
				if($(this).is(':checked')) {
					$(this).parent('label').addClass('checked');
				} else {
					$(this).parent('label').removeClass('checked');
				}
			});
		});
		return this;
	};
	
	//TextArea limiter (plugin required)
	$.fn.textarea = function () {
		var limitChar = parseInt($(this).attr('data-limit'), 10);
		if (!isNaN(limitChar)) {
			$(this).limit(limitChar, '#' + $(this).attr('data-target'));
		}
		return this;
	};
