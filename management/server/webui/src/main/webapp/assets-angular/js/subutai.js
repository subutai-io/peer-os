$(document).ready(function () {
	// Full height of sidebar
	function fix_height() {
		var heightWithoutNavbar = $("body > #wrapper").height() - 61;
		$(".sidebard-panel").css("min-height", heightWithoutNavbar + "px");

		var navbarHeigh = $('nav.navbar-default').height();
		var wrapperHeigh = $('#page-wrapper').height();

		if(navbarHeigh > wrapperHeigh){
			$('#page-wrapper').css("min-height", navbarHeigh + "px");
		}

		if(navbarHeigh < wrapperHeigh){
			$('#page-wrapper').css("min-height", $(window).height()  + "px");
		}

	}

	$(window).bind("load resize scroll", function() {
		if(!$("body").hasClass('body-small')) {
			fix_height();
		}
	});

	// Move right sidebar top after scroll
	$(window).scroll(function(){
		if ($(window).scrollTop() > 0 && !$('body').hasClass('fixed-nav') ) {
			$('#right-sidebar').addClass('sidebar-top');
		} else {
			$('#right-sidebar').removeClass('sidebar-top');
		}
	});


	setTimeout(function(){
		fix_height();
	});


});

$(function() {
	$(window).bind("load resize", function() {
		if ($(this).width() < 769) {
			$('body').addClass('body-small')
		} else {
			$('body').removeClass('body-small')
		}
	})
});
//
//
//(function($){
//    $(window).load(function(){
//        $("#tree-root").mCustomScrollbar({
//            axis: 'y',
//            theme: 'dark'
//        });
//    });
//})(jQuery);
