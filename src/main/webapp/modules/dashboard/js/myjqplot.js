function downloadImage(graph, filename) {
	 var imgData = jQuery(graph).jqplotToImageStr({});
     if (imgData) {
         download(imgData, filename+'.png', "image/png");
     } else {
    	 alert("no data !");
     }
}

//function downloadImage(graph, filename) {
//	var obj = jQuery(graph);
//    var newCanvas = document.createElement("canvas");
//    newCanvas.width = obj.find(".jqplot-base-canvas").width();
//    newCanvas.height = obj.find(".jqplot-base-canvas").height();
//    var baseOffset = obj.find(".jqplot-base-canvas").offset();
//
//    // make white background for pasting
//    var context = newCanvas.getContext("2d");
//    context.fillStyle = "rgba(255,255,255,1)";
//    context.fillRect(0, 0, newCanvas.width, newCanvas.height);
//
//    obj.children().each(function () {
//
//        if (jQuery(this)[0].tagName.toLowerCase() == 'canvas') {
//            // all canvas from the chart
//            var offset = jQuery(this).offset();
//            newCanvas.getContext("2d").drawImage(this,
//                        offset.left - baseOffset.left,
//                        offset.top - baseOffset.top
//                    );
//        } // for the div's with the X and Y axis
//    });
//
//    obj.children().each(function () {
//        if (jQuery(this)[0].tagName.toLowerCase() == 'div') {
//            if (jQuery(this).attr('class') == "jqplot-axis jqplot-yaxis") {
//
//            	jQuery(this).children("canvas").each(function () {
//                    var offset = jQuery(this).offset();
//                    newCanvas.getContext("2d").drawImage(this,
//                                offset.left - baseOffset.left,
//                                offset.top - baseOffset.top
//                            );
//                });
//            }
//            else if (jQuery(this).attr('class') == "jqplot-axis jqplot-xaxis") {
//
//            	jQuery(this).children("canvas").each(function () {
//                    var offset = jQuery(this).offset();
//                    newCanvas.getContext("2d").drawImage(this,
//                                offset.left - baseOffset.left,
//                                offset.top - baseOffset.top
//                            );
//                });
//            }
//        }
//    });
//    download(newCanvas.toDataURL('image/png'), filename+'.png', "image/png");
// }

//function downloadImage(graph, filename) {
//	var canvas = jQuery(graph).find("canvas.overlay");	
//	download(canvas[0].toDataURL('image/png'), filename+'.png', "image/png");
//}