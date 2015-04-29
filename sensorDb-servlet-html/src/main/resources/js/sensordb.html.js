;(function($,$n2,$s){

var HtmlFixer = $n2.Class({
	
	dbService: null,
	
	initialize: function(opts_){
		var opts = $n2.extend({
			dbService: null
		},opts_);
		
		this.dbService = opts.dbService;
	},
	
	fixHtml: function($elem){
		var _this = this;
		
		if( !$elem ){
			$elem = $('body');
		};
		
		var $set = $elem.find('*').addBack();
		
		// Locations
		$set.filter('select.sdb_getLocations').each(function(){
			var $select = $(this);
			_this._getLocationOptions($select);
		});
		
		// Device Types
		$set.filter('select.sdb_getDeviceTypes').each(function(){
			var $select = $(this);
			_this._getDeviceTypeOptions($select);
		});
		
		// Devices
		$set.filter('select.sdb_getDevices').each(function(){
			var $select = $(this);
			_this._getDeviceOptions($select);
		});
	},
	
	_getLocationOptions: function($select){
		this.dbService.getLocations({
			onSuccess: function(locations){
				$select.empty();
				
				locations.sort(function(o1,o2){
					if(o1.name < o2.name) return -1;
					if(o1.name > o2.name) return 1;
					return 0;
				});
				
				for(var i=0,e=locations.length; i<e; ++i){
					var location = locations[i];
					var name = location.name;
					var id = location.id;
					
					$('<option>')
						.text(name)
						.attr('value',id)
						.appendTo($select);
				};
			}
		});
	},
	
	_getDeviceTypeOptions: function($select){
		this.dbService.getDeviceTypes({
			onSuccess: function(deviceTypes){
				$select.empty();
				
				deviceTypes.sort(function(o1,o2){
					if(o1.name < o2.name) return -1;
					if(o1.name > o2.name) return 1;
					return 0;
				});
				
				for(var i=0,e=deviceTypes.length; i<e; ++i){
					var deviceType = deviceTypes[i];
					var name = deviceType.name;
					
					$('<option>')
						.text(name)
						.attr('value',name)
						.appendTo($select);
				};
			}
		});
	},
	
	_getDeviceOptions: function($select){
		this.dbService.getDevices({
			onSuccess: function(devices){
				$select.empty();
				
				devices.sort(function(o1,o2){
					if(o1.name < o2.name) return -1;
					if(o1.name > o2.name) return 1;
					return 0;
				});
				
				for(var i=0,e=devices.length; i<e; ++i){
					var device = devices[i];
					var serialNumber = device.serialNumber;
					var id = device.id;
					
					$('<option>')
						.text(serialNumber)
						.attr('value',id)
						.appendTo($select);
				};
			}
		});
	}
});	

// ============================================
// Export
$s.html = {
	HtmlFixer: HtmlFixer
};

})(jQuery,nunaliit2,sensorDb);