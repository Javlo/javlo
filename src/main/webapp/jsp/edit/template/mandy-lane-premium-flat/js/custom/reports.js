jQuery(document).ready(function () {
	
	/***START OF SIMPLE CHART***/
	
	var flash = [], html5 = [];
	for (var i = 0; i < 14; i += 0.5) {
		flash.push([i, Math.sin(i)]);
		html5.push([i, Math.cos(i)]);
	}
		
	function showTooltip(x, y, contents) {
		jQuery('<div id="tooltip" class="tooltipflot">' + contents + '</div>').css( {
			position: 'absolute',
			display: 'none',
			top: y + 5,
			left: x + 5
		}).appendTo("body").fadeIn(200);
	}

		
	var plot = jQuery.plot(jQuery("#chartplace"),
		   [ { data: flash, label: "Flash(x)", color: "#ff7200"}, { data: html5, label: "HTML5(x)", color: "#39870a"} ], {
			   series: {
				   lines: { show: true },
				   points: { show: true }
			   },
			   grid: { hoverable: true, clickable: true, borderColor: '#ccc', borderWidth: 1 },
			   yaxis: { min: -1.2, max: 1.2 }
			 });
	
	var previousPoint = null;
	jQuery("#chartplace").bind("plothover", function (event, pos, item) {
		jQuery("#x").text(pos.x.toFixed(2));
		jQuery("#y").text(pos.y.toFixed(2));
		
		if(item) {
    	    if (previousPoint != item.dataIndex) {
        	    previousPoint = item.dataIndex;
                    
                jQuery("#tooltip").remove();
                var x = item.datapoint[0].toFixed(2),
                y = item.datapoint[1].toFixed(2);
                    
                showTooltip(item.pageX, item.pageY,
                                item.series.label + " of " + x + " = " + y);
            }
        
		} else {
           jQuery("#tooltip").remove();
           previousPoint = null;            
        }
	
	});
	
	jQuery("#chartplace").bind("plotclick", function (event, pos, item) {
		if (item) {
			jQuery("#clickdata").text("You clicked point " + item.dataIndex + " in " + item.series.label + ".");
			plot.highlight(item.series, item.datapoint);
		}
	});
	
	
	
	
	/**ANNOTATING CHART**/
	
	// generate a dataset
    var d1 = [];
    for (var i = 0; i < 20; ++i)
        d1.push([i, Math.sin(i)]);
    
    var data = [{ data: d1, label: "Pressure", color: "#333" }];

    // setup background areas
    var markings = [
        { color: '#eee', yaxis: { from: 1 } },
        { color: '#eee', yaxis: { to: -1 } },
        { color: '#ccc', lineWidth: 1, xaxis: { from: 2, to: 2 } },
        { color: '#ccc', lineWidth: 1, xaxis: { from: 8, to: 8 } }
    ];
    
    var placeholder = jQuery("#annotation");
    
    // plot it
    var plot = jQuery.plot(placeholder, data, {
        bars: { show: true, barWidth: 0.5, lineWidth: 0, fill: 0.9, fillColor: "#069"},
        xaxis: { ticks: [], autoscaleMargin: 0.02 },
        yaxis: { min: -2, max: 2 },
        grid: { markings: markings, borderColor: '#ccc', borderWidth: 1}
    });

    // add labels
    var o;

    o = plot.pointOffset({ x: 2, y: -1.2});
    // we just append it to the placeholder which Flot already uses
    // for positioning
    placeholder.append('<div style="position:absolute;left:' + (o.left + 4) + 'px;top:' + o.top + 'px;color:#666;font-size:smaller">Warming up</div>');

    o = plot.pointOffset({ x: 8, y: -1.2});
    placeholder.append('<div style="position:absolute;left:' + (o.left + 4) + 'px;top:' + o.top + 'px;color:#666;font-size:smaller">Actual measurements</div>');

    // draw a little arrow on top of the last label to demonstrate
    // canvas drawing
    var ctx = plot.getCanvas().getContext("2d");
    ctx.beginPath();
    o.left += 4;
    ctx.moveTo(o.left, o.top);
    ctx.lineTo(o.left, o.top - 10);
    ctx.lineTo(o.left + 10, o.top - 5);
    ctx.lineTo(o.left, o.top);
    ctx.fillStyle = "#000";
    ctx.fill();
	
	
	/**PIE CHART**/
	var data = [];
	var series = 5;
	for( var i = 0; i<series; i++) {
		data[i] = { label: "Series"+(i+1), data: Math.floor(Math.random()*100)+1 }
	}
	jQuery.plot(jQuery("#piechart"), data, {
			series: {
				pie: {
					show: true
				}
			}
	});
	
	
	
	/**REAL TIME CHART**/
	
	// we use an inline data source in the example, usually data would
    // be fetched from a server
    var data = [], totalPoints = 300;
    function getRandomData() {
        if (data.length > 0)
            data = data.slice(1);

        // do a random walk
        while (data.length < totalPoints) {
            var prev = data.length > 0 ? data[data.length - 1] : 50;
            var y = prev + Math.random() * 10 - 5;
            if (y < 0)
                y = 0;
            if (y > 100)
                y = 100;
            data.push(y);
        }

        // zip the generated y values with the x values
        var res = [];
        for (var i = 0; i < data.length; ++i)
            res.push([i, data[i]])
        return res;
    }

    // setup control widget
    var updateInterval = 500;
    jQuery("#updateInterval").val(updateInterval).change(function () {
        var v = jQuery(this).val();
        if (v && !isNaN(+v)) {
            updateInterval = +v;
            if (updateInterval < 1)
                updateInterval = 1;
            if (updateInterval > 2000)
                updateInterval = 2000;
            jQuery(this).val("" + updateInterval);
        }
    });

    // setup plot
    var options = {
        series: { lines: { fill: true, fillColor: '#fffccc' }, shadowSize: 0, }, // drawing is faster without shadows
        yaxis: { min: 0, max: 100 },
        xaxis: { show: false },
		grid: { borderColor: '#ccc', borderWidth: 1},
		
    };
    var plot = jQuery.plot(jQuery("#realtime"), [ getRandomData() ], options);

    function update() {
        plot.setData([ getRandomData() ]);
        // since the axes don't change, we don't need to call plot.setupGrid()
        plot.draw();
        
        setTimeout(update, updateInterval);
    }

    update();

		
		
});