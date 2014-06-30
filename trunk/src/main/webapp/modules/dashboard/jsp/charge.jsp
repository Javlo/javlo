<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="charge-wrapper" class="content">
<div id="charge" class="charge"></div>
</div>
<script type="text/javascript">
jQuery(document).ready(function () {
	
var updateInterval = 10000;

var url = "${info.currentURL}".replace("/edit", "/ajax")+"?webaction=dashboard.readTracker&type=charge";

function update() {
	jQuery.ajax({
		url : url,
		cache : false,		
		type : "post",
		dataType : "json",
		error : function(jqXHR, textStatus, errorThrown) {
			if (console != null) {
				console.log("ajax error : "+textStatus);
			}
		}
	}).done(function(jsonObj) {		
		var data = [];
		var max = 0;
		jQuery.each(jsonObj, function(i, v) {
				if (v>max) {
					max=v;
				}				
				data.push([i*100,v]);				
			});
		
		data.sort(function(a,b) {
			var va = parseInt(a[0]);
			var vb = parseInt(b[0]);
			
			if (va > vb) {
				return 1;
			} else {
				return -1;
			}
		});
		// setup plot
		var options = {
		    series: { lines: { fill: true, fillColor: '#fffccc' }, shadowSize: 0 }, // drawing is faster without shadows
		    yaxis: { min: 0, max: max+2 },
		    xaxis: { show: true, mode: "time"},
			grid: { borderColor: '#ccc', borderWidth: 1},
			
		};
		var plot = jQuery.plot(jQuery("#charge"), [ data ], options);
		
	});	
    
    setTimeout(update, updateInterval);
}

update();
});

</script>