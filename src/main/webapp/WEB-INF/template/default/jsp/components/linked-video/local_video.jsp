<object width="${width}" height="${heigth}" type="application/x-shockwave-flash" data="${info.templateFolder}/jsp/components/linked-video/flvplayer.swf">
	<!-- Firefox uses the `data` attribute above, IE/Safari uses the param below -->
	<param name="movie" value="${info.absoluteTemplateFolder}/jsp/components/linked-video/flvplayer.swf" />
	<param name="flashvars" value="controlbar=over&amp;image=${image}&amp;file=${file}" />
	<!-- fallback image. note the title field below, put the title of the video there -->
	<img src="${image}" width="${width}" height="${heigth}" alt="${title}"
	     title="No video playback capabilities, please download the video below" />
	<p><a href="${file}">Download Video.</a></p>
</object>
