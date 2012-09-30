<div class="video">
<h4>${label}</h4>
<object type="application/x-shockwave-flash" data="${info.absoluteTemplateFolder}/jsp/components/linked-video/player_flv_maxi.swf" width="${width}" height="${height}">
    <param name="movie" value="${info.absoluteTemplateFolder}/jsp/components/linked-video/player_flv_maxi.swf" />
    <param name="allowFullScreen" value="true" />
    <param name="FlashVars" value="flv=${file}&amp;title=${label}&amp;startimage=${image}" />
    alt:<video width="${width}" height="${height}" controls="controls"><source src="${file}" type="video/mp4"></source><a href="${file}">${file}</a></video>	 
</object>
</div>