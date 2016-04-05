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

$(document).on('click', '.b-nav-menu-link', function () {
	if ($(this).next('.b-nav-menu__sub').length > 0) {
		if ($(this).parent().hasClass('b-nav-menu_active')) {
			$(this).parent().removeClass('b-nav-menu_active');
			$(this).next('.b-nav-menu__sub').stop().slideUp(300);
		} else {
			$('.b-nav-menu_active .b-nav-menu__sub').parent().removeClass('b-nav-menu_active')
			$('.b-nav-menu__sub').stop().slideUp(300);
			$(this).parent().addClass('b-nav-menu_active');
			$(this).next('.b-nav-menu__sub').stop().slideDown(300);
		}
		return false;
	} else {
		if($(this).parent().hasClass('b-nav-menu_active')) {
			location.reload();
		} else {
			$('.b-nav-menu__sub').stop().slideUp(300);
			$('.b-nav-menu_active').removeClass('b-nav-menu_active');
		}
	}
});

$('body').on('click', '.js-notification', function() {
	$('.b-hub-status__dropdown').stop().slideUp(100);
	var currentDropDown = $(this).next('.b-hub-status__dropdown');
	if(currentDropDown.hasClass('b-hub-status__dropdown_open')) {
		$('.b-hub-status__dropdown_open').removeClass('b-hub-status__dropdown_open');
	} else {
		$('.b-hub-status__dropdown_open').removeClass('b-hub-status__dropdown_open');
		currentDropDown.stop().slideDown(200);
		currentDropDown.addClass('b-hub-status__dropdown_open');
	}
	return false;
});

$(document).on('click', function(event) {
	if(!$(event.target).closest('.js-header-dropdown').hasClass('js-header-dropdown')){
		$('.b-hub-status__dropdown').stop().slideUp(100);
		$('.b-hub-status__dropdown_open').removeClass('b-hub-status__dropdown_open');
	}

	if(
		!$(event.target).closest('.js-dropen-menu').hasClass('js-dropen-menu') && 
		$(event.target).closest('g').attr('class') != 'element-call-menu' && 
		$(event.target).closest('g').attr('class') != 'b-container-plus-icon'
	){
		$('.b-template-settings').stop().slideUp(100);
	}
});

$(document).keyup(function(e) {
	if (e.keyCode == 27) {
		$('.b-hub-status__dropdown').stop().slideUp(100);
		$('.b-hub-status__dropdown_open').removeClass('b-hub-status__dropdown_open');
	}
});

var UPDATE_NIGHTLY_BUILD_STATUS;

