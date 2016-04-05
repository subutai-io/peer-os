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


$('body').click(function (event) {
	if (
		!$(event.target).hasClass('ssh-info-button') &&
		!$(event.target).parent().hasClass("ssh-plugin-info-tooltip") &&
		!$(event.target).hasClass("ssh-plugin-info-tooltip")
	) {
		$('.ssh-plugin-info-tooltip').hide();
	}
});

$(document).keyup(function(e) {
	if (e.keyCode == 27) {
		$('.ssh-plugin-info-tooltip').hide();
	}
});

$(document).on('click', '.ssh-info-button', function (event) {
	if($(event.target).hasClass("ssh-info-button")) {
		var status = $(event.target).find(".ssh-plugin-info-tooltip").css('display') == "none";
		if(status) {
			$('.ssh-plugin-info-tooltip').hide();
			$(event.target).find('.ssh-plugin-info-tooltip').toggle();
		} else {
			$(event.target).find('.ssh-plugin-info-tooltip').toggle();
		}
	}
});

var UPDATE_NIGHTLY_BUILD_STATUS;

