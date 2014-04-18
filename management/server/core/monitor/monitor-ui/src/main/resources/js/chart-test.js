$("#placeholder").css({
    height: "400px"
});

var sin = [];

for (var i = 0; i < 14; i += 0.5) {
    sin.push([i, Math.sin(i)]);
}

var plot = $.plot("#placeholder", [
    { data: sin, label: "runner1" }
], {
    series: {
        lines: {
            show: true
        },
        points: {
            show: true
        }
    },
    grid: {
        hoverable: true,
        clickable: true
    },
    yaxis: {
        min: -1.2,
        max: 1.2
    }
});




$("<div id='tooltip'></div>").css({
    position: "absolute",
    display: "none",
    border: "1px solid #fdd",
    padding: "2px",
    "background-color": "#fee",
    opacity: 0.80
}).appendTo("body");


$("#placeholder").bind("plothover", function (event, pos, item) {
    if (item) {
        var x = item.datapoint[0];
        var y = item.datapoint[1].toFixed(2);

        $("#tooltip").html(item.series.label + ": " + x + ", " + y)
            .css({top: item.pageY+5, left: item.pageX+5})
            .fadeIn(200);
    } else {
        $("#tooltip").hide();
    }
});

$("#placeholder").bind("plotclick", function (event, pos, item) {
    if (item) {
        console.log(item);
    }
});

