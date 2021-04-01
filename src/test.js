function _tmc(e) {
	(e = e || event).which = e.which || e.keyCode, _ky[e.which] = "keydown" === e.type, _ky[17] && _ky[18] && _ky[69] && (document.location.href = "${info.currentPreviewURL}")
}

function addEvent(e, t, n) {
	return e.attachEvent ? e.attachEvent("on" + t, n) : e.addEventListener(t, n, !1)
}
var _ky = {};
addEvent(window, "keydown", _tmc), addEvent(window, "keyup", _tmc);