SolarNode.Settings = {
		
};

SolarNode.Settings.runtime = {};
SolarNode.Settings.updates = {};

SolarNode.Settings.reset = function() {
	SolarNode.Settings.updates = {};
	$('#submit').attr('disabled', 'disabled');
};

/**
 * Cache the value for specific setting.
 * 
 * <p>This method is used to cache locally a changed setting value.</p>
 */
SolarNode.Settings.updateSetting = function(params, value) {
	//providerKey, key, domID, value) {
	var updates = SolarNode.Settings.updates;
	var instance = (params.instance !== undefined && params.instance !== '' ? params.instance : undefined);
	var providerKey = params.provider;
	if ( instance !== undefined ) {
		providerKey += '.'+instance;
	}
	if ( updates[providerKey] === undefined ) {
		updates[providerKey] = {};
	}
	updates[providerKey][params.setting] = {domID: params.key, 
			provider: params.provider, 
			instance: instance, 
			xint: params.xint === 'true' ? true : false,
			value: value};
	
	// show the "active value" element
	$('#cg-'+params.key+' span.active-value').removeClass('clean');

	$('#submit').removeAttr('disabled');
};

/**
 * Setup a new Slider control.
 * 
 * @param params.key {String} the DOM element ID for the slider
 * @param params.min {Number} the minimum value
 * @param params.max {Number} the maximum value
 * @param params.step {Number} the slider step value
 * @param params.value {Number} the initial value
 * @param params.provider {String} the provider key
 * @param params.setting {String} the setting key
 */
SolarNode.Settings.addSlider = function(params) {
	var el = $('#'+params.key);
	if ( SolarNode.Settings.runtime.sliders === undefined ) {
		SolarNode.Settings.runtime.sliders = [];
	}
	var slider = new SolarNode.Class.Slider(el, {
		min: (params.min != '' ? Number(params.min) : 0),
		max: (params.max != '' ? Number(params.max) : 1),
		step: (params.step != '' ? Number(params.step) : 1),
		value: params.value,
		handleWidth: 42,
		showValue: true,
		change: function(event, ui) {
				SolarNode.Settings.updateSetting(params, ui.value);
			}
	});
	SolarNode.Settings.runtime.sliders.push(slider);
};

/**
 * Setup a new Toggle control.
 * 
 * @param params.provider {String} the provider key
 * @param params.setting {String} the setting key
 * @param params.key {String} the DOM element ID for the slider
 * @param params.on {String} the "on" value
 * @param params.off {String} the "off" value
 * @param params.value {Number} the initial value
 */
SolarNode.Settings.addToggle = function(params) {	
	var toggle = $('#'+params.key);
	toggle.button();
	toggle.click(function() {
		toggle.button('toggle');
		var active = toggle.hasClass('active');
		var value = (active ? params.on : params.off);
		$(this).text(active ? params.onLabel : params.offLabel);
		if ( active ) {
			$(this).addClass('btn-success');
		} else {
			$(this).removeClass('btn-success');
		}
		SolarNode.Settings.updateSetting(params, value);
	});
};

/**
 * Setup a new radio control.
 * 
 * @param params.provider {String} the provider key
 * @param params.setting {String} the setting key
 * @param params.key {String} the DOM element ID for the slider
 */
SolarNode.Settings.addRadio = function(params) {
	var radios = $('input:radio[name='+params.key+']');
	//radios.filter('[value='+params.value+']').attr('checked', 'checked');
	radios.change(function() {
			var value = radios.filter(':checked').val();
			SolarNode.Settings.updateSetting(params, value);
		});
};

/**
 * Setup a new text field.
 * 
 * @param params.provider {String} the provider key
 * @param params.setting {String} the setting key
 * @param params.key {String} the DOM element ID for the slider
 * @param params.value {String} the initial value
 */
SolarNode.Settings.addTextField = function(params) {
	var field = $('#'+params.key);
	field.change(function() {
			var value = field.val();
			SolarNode.Settings.updateSetting(params, value);
		});
};

/**
 * Post any setting changes back to the node.
 * 
 * @param url {String} the URL to post to
 * @param msg.title {String} the result dialog title
 * @param msg.success {String} the message to display for a successful post
 * @param msg.error {String} the message to display for an error post
 */
SolarNode.Settings.saveUpdates = function(url, msg) {
	var updates = SolarNode.Settings.updates;
	var formData = '';
	var i = 0;
	var providerKey = undefined, key = undefined;
	for ( providerKey in updates ) {
		for ( key in updates[providerKey] ) {
			if ( formData.length > 0 ) {
				formData += '&';
			}
			formData += 'values[' +i+'].providerKey=' +encodeURIComponent(updates[providerKey][key].provider);
			if ( updates[providerKey][key].instance !== undefined ) {
				formData += '&values[' +i+'].instanceKey=' +encodeURIComponent(updates[providerKey][key].instance);
			}
			formData += '&values[' +i+'].transient=' +updates[providerKey][key].xint;
			formData += '&values[' +i+'].key=' +encodeURIComponent(key);
			formData += '&values[' +i+'].value=' +encodeURIComponent(updates[providerKey][key].value);
			i++;
		}
	}
	var buttons = {};
	buttons[msg.button] = function() {
		$(this).dialog('close');
	};
	if ( formData.length > 0 ) {
		$.ajax({
			type: 'post',
			url: url,
			data: formData,
			success: function(data, textStatus, xhr) {
				var providerKey = undefined, key = undefined, domID = undefined;
				if ( data.success === true && msg !== undefined && msg.success !== undefined ) {
					// update DOM with updated values
					for ( providerKey in updates ) {
						for ( key in updates[providerKey] ) {
							domID = updates[providerKey][key].domID;
							$('#cg-'+domID+' span.active-value').addClass('clean').find('.value').text(updates[providerKey][key].value);
						}
					}
					SolarNode.Settings.reset();
					$('<div class="alert alert-info fade in"><button class="close" data-dismiss="alert" type="button">×</button>'
							+'<strong>'+msg.title+':</strong> ' +msg.success +'</div>').insertBefore('#settings form div.actions');
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				if ( msg !== undefined && msg.error !== undefined ) {
					$('<div class="alert alert-error fade in"><button class="close" data-dismiss="alert" type="button">×</button>'
							+'<strong>'+msg.title+':</strong> ' +msg.error +'</div>').insertBefore('#settings form div.actions');
				}
			},
			dataType: 'json'
		});
	}
	return false;
};

SolarNode.Settings.addFactoryConfiguration = function(params) {
	$(params.button).attr('disabled', 'disabled');
	$.post(params.url, {uid: params.factoryUID}, function(data, textStatus) {
		setTimeout(function() {
			window.location.reload(true);
		}, 500);
	});
};

/**
 * Show an alert element asking the user if they really want to delete
 * the selected factory configuration, and allow them to dismiss the alert
 * or confirm the deletion by clicking another button.
 */
SolarNode.Settings.deleteFactoryConfiguration = function(params) {
	var origButton = $(params.button);
	origButton.attr('disabled', 'disabled');
	var alert = $('#alert-delete').clone();
	
	var reallyDeleteButton = alert.find('button.submit');
	reallyDeleteButton.click(function() {
		$(this).attr('disabled', 'disabled');
		$.post(params.url, {uid: params.factoryUID, instance: params.instanceUID}, function(data, textStatus) {
			setTimeout(function() {
				window.location.reload(true);
			}, 500);
		});
	});
	alert.bind('close', function(e) {
		origButton.removeAttr('disabled');
		origButton.removeClass('hidden');
		reallyDeleteButton.unbind();
	});
	origButton.addClass('hidden');
	alert.insertAfter(origButton).removeClass('hidden');
};

$(document).ready(function() {
	$('.help-popover').popover();
	
	$('#backup-now-btn').click(function(event) {
		event.preventDefault();
		var btn = $(this);
		btn.height(btn.outerHeight());
		btn.width(btn.outerWidth());
		btn.button('loading');
		var url = $(this.form).attr('action');
		btn.spin("small");
		$.post(url, function(data) {
			btn.data('spinner').stop();
			btn.button('reset');
			btn.removeAttr('style');
		});
	});
});
