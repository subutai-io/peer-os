// ----------------------------------------------------------------------------
// Functions

function dateToStr(date) {
    return date.getDate() + "."
        + (date.getMonth() + 1) + "."
        + date.getFullYear() + " "
        + date.getMinutes() + ":"
        + date.getHours() + ":"
        + date.getSeconds();
}

function createTooltip() {
    $("<div id='tooltip'></div>").css({
        position: "absolute",
        display: "none",
        border: "1px solid #fdd",
        padding: "2px",
        "background-color": "#fee",
        opacity: 0.80
    }).appendTo("body");
}

function showTooltip(item) {
    var x = item.datapoint[0];
    var y = item.datapoint[1].toFixed(2);
    var date = new Date(x);

    $("#tooltip").html(dateToStr(date) + ": " + y)
        .css({
            top: item.pageY + 10,
            left: item.pageX + 10
        })
        .fadeIn(200);
}

function handleTooltip(event, pos, item) {
    if (item) {
        showTooltip(item);
    } else {
        $("#tooltip").hide();
    }
}

function createYLabel(chart) {
    var yaxisLabel = $("<div></div>")
        .text("$yTitle")
        .appendTo(chart);

    yaxisLabel.css({
        "position": "absolute",
        "text-align": "center",
        "top": "50%",
        "transform": "rotate(-90deg)",
        "-o-transform": "rotate(-90deg)",
        "-ms-transform": "rotate(-90deg)",
        "-moz-transform": "rotate(-90deg)",
        "-webkit-transform": "rotate(-90deg)",
        "transform-origin": "0 0",
        "-o-transform-origin": "0 0",
        "-ms-transform-origin": "0 0",
        "-moz-transform-origin": "0 0",
        "-webkit-transform-origin": "0 0"
    });
}

// ----------------------------------------------------------------------------
// Main

var chart = $("#chart");

var plot = $.plot(chart,
    [
        { data: $data, label: "$label" }
    ],
    {
        xaxis: {
            mode: "time",
            timezone: "browser"
        },
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
            clickable: true,
            margin: {
                top: 8,
                bottom: 20,
                left: 20
            }
        }
    }
);

chart.bind("plothover", handleTooltip);

createTooltip();
createYLabel(chart);