<!-- first try HTML5 playback: if serving as XML, expand `controls` to `controls="controls"` and autoplay likewise -->
<!-- warning: playback does not work on iOS3 if you include the poster attribute! fixed in iOS4.0 -->
<video width="${width}" height="${heigth}" controls>
	<!-- MP4 must be first for iPad! -->
	<source src="${file}" type="${type}" /><!-- Safari / iOS video    -->	
	<!-- fallback to Flash: -->
	<object width="${width}" height="${heigth}" type="application/x-shockwave-flash" data="flvplayer.swf">
		<!-- Firefox uses the `data` attribute above, IE/Safari uses the param below -->
		<param name="movie" value="flvplayer.swf" />
		<param name="flashvars" value="controlbar=over&amp;image=__POSTER__.JPG&amp;file=${file}" />
		<!-- fallback image. note the title field below, put the title of the video there -->
		<img src="${image}" width="${width}" height="${heigth}" alt="${title}"
		     title="No video playback capabilities, please download the video below" />
		<p><a href="${file}">Download Video.</a></p>
	</object>
</video>
<!-- you *must* offer a download link as they may be able to play the file locally. customise this bit all you want -->
