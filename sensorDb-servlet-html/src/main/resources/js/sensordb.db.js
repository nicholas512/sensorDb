;(function($,$n2,$s){
"use strict";

// Localization
var _loc = function(str,args){ return $n2.loc(str,'sensordb',args); };

function httpJsonError(XMLHttpRequest, defaultStr) {
	// Need JSON
	if( !JSON || typeof(JSON.parse) !== 'function' ) {
		return defaultStr;
	};
	
	// Need a response text
	var text = XMLHttpRequest.responseText;
	if( !text ) return defaultStr;
	
	// Parse
	var error = JSON.parse(text);
	if( !error ) return defaultStr;
	if( !error.reason ) return defaultStr;
	
	return error.reason;
};

//============================================
var Database = $n2.Class({
	
	dbUrl: null,
	
	initialize: function(opts_){
		var opts = $n2.extend({
			dbUrl: null
		},opts_);
		
		this.dbUrl = opts.dbUrl;
	},
	
	getLocations: function(opts_){
		var opts = $n2.extend({
			onSuccess: function(locations){}
			,onError: function(err){}
		},opts_);
		
		$.ajax({
			url: this.dbUrl + 'getLocations'
			,type: 'get'
			,async: true
			,dataType: 'json'
			,success: function(res) {
				if( res.ok ) {
					opts.onSuccess(res.locations);
				} else {
					opts.onError('Malformed response');
				};
			}
			,error: function(XMLHttpRequest, textStatus, errorThrown) {
				var errStr = httpJsonError(XMLHttpRequest, textStatus);
				opts.onError('Error obtaining location: '+errStr);
			}
		});
	},
	
	getDevices: function(opts_){
		var opts = $n2.extend({
			onSuccess: function(devices){}
			,onError: function(err){}
		},opts_);
		
		$.ajax({
			url: this.dbUrl + 'getDevices'
			,type: 'get'
			,async: true
			,dataType: 'json'
			,success: function(res) {
				if( res.ok ) {
					opts.onSuccess(res.devices);
				} else {
					opts.onError('Malformed response');
				};
			}
			,error: function(XMLHttpRequest, textStatus, errorThrown) {
				var errStr = httpJsonError(XMLHttpRequest, textStatus);
				opts.onError('Error obtaining location: '+errStr);
			}
		});
	}
});	

// ============================================
// Export
$s.db = {
	Database: Database
};

})(jQuery,nunaliit2,sensorDb);