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