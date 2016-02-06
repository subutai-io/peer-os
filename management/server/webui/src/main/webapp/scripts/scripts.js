$(document).ready(function() {
	$('.b-nav-menu-link').on('click', function(){
		if($(this).next('.b-nav-menu__sub').length > 0) {
			if($(this).parent().hasClass('b-nav-menu_active')) {
				$(this).parent().removeClass('b-nav-menu_active');
				$(this).next('.b-nav-menu__sub').slideUp(300);
			} else {
				$(this).parent().addClass('b-nav-menu_active');
				$(this).next('.b-nav-menu__sub').slideDown(300);
			}
			return false;
		}
	});

	function colEqualHeight() {
		if( $('.b-nav').height() > $('.b-workspace').height() ) {
			$('.b-workspace').height( $('.b-nav').height() );
		}else if( $('.b-nav').height() < $('.b-workspace').height() ) {
			$('.b-nav').height( $('.b-workspace').height() );
		}
	}
	colEqualHeight();
});
$('.js-cloud-btn').click(function(event) {
	$( ".js-grid-list-switcher_list" ).toggleClass( "js-grid-list-switcher_list_show" );
});
$(".b-form-input_dropdown").click(function () {
	$(this).toggleClass("is-active");
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
