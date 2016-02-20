$(".b-form-input_dropdown").click(function () {
	$(this).toggleClass("is-active");
});

$('body').on('click', '.js-notification', function() {
	$(this).next('.b-hub-status__dropdown').slideToggle(200);
	return false;
});

$(".b-form-input-dropdown-list").click(function(e) {
	e.stopPropagation();
});

$('.js-scrollbar').perfectScrollbar();
$('.js-scrollbar-cloud').perfectScrollbar();

$('body').on('click', '.js-hide-resources', function(){
	$('.b-cloud-add-tools').animate({'left': 0}, 300);
	return false;
});

var UPDATE_NIGHTLY_BUILD_STATUS;

//document.getElementById("uploadBtn").onchange = function () {
//	document.getElementById("uploadFile").value = this.value;
//};
/*$('a.js-cbox-modal').colorbox({
	title: " ",
	transition: "none",
	previous: false,
	next: false,
	arrowKey: false,
	rel: false,
	overlayClose: true,
	opacity: 0.8,
	closeButton: false,
	onComplete: function() {
		$.colorbox.resize();
	}
});*/
