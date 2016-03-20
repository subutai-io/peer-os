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

//$('body').on('keydown', function(e) {
//	var keyCode = e.keyCode || e.which;
//	if (keyCode == 9) {
//		e.preventDefault();
//		var fields = document.querySelectorAll('[tabindex]');
//		if(document.activeElement.hasAttribute('tabindex')){
//			for(var i = 0; i < fields.length; i++) {
//				if(fields[i] == document.activeElement) {
//					if(fields[i + 1] == undefined) {
//						fields[0].focus();
//					} else {
//						if($(fields[i + 1]).is(":visible") == true){
//							fields[i + 1].focus();
//						} else {
//							continue;
//						}
//					}
//					break;
//				}
//			}
//		} else {
//			fields[0].focus();
//		}
//	}
//});

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

