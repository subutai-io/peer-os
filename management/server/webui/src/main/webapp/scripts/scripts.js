$(".b-form-input_dropdown").click(function () {
	$(this).toggleClass("is-active");
});

$(".b-form-input-dropdown-list").click(function(e) {
	e.stopPropagation();
});

$('.js-scrollbar').perfectScrollbar({
	"wheelPropagation": true,
	"swipePropagation": false
});

$('body').on('click', '.js-hide-resources', function(){
	$('.b-cloud-add-tools').animate({'left': 0}, 300);
	return false;
});

var UPDATE_NIGHTLY_BUILD_STATUS;

