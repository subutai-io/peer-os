$(".b-form-input_dropdown").click(function () {
	$(this).toggleClass("is-active");
});

$('body').on('click', '.js-notification', function() {
	$('.b-hub-status__dropdown').slideUp(100);
	var currentDropDown = $(this).next('.b-hub-status__dropdown');
	if(currentDropDown.hasClass('b-hub-status__dropdown_open')) {
		$('.b-hub-status__dropdown_open').removeClass('b-hub-status__dropdown_open');
	} else {
		$('.b-hub-status__dropdown_open').removeClass('b-hub-status__dropdown_open');
		currentDropDown.slideDown(200);
		currentDropDown.addClass('b-hub-status__dropdown_open');
	}
	return false;
});

$(document).on('click', function(event) {
	if(!$(event.target).closest('.js-header-dropdown').hasClass('js-header-dropdown')){
		$('.b-hub-status__dropdown').slideUp(100);
		$('.b-hub-status__dropdown_open').removeClass('b-hub-status__dropdown_open');
	}

	if(
		!$(event.target).closest('.js-dropen-menu').hasClass('js-dropen-menu') && 
		$(event.target).closest('g').attr('class') != 'element-call-menu' && 
		$(event.target).closest('g').attr('class') != 'b-container-plus-icon'
	){
		$('.b-template-settings').slideUp(100);
	}
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

