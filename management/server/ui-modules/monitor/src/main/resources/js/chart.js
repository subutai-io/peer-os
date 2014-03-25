var DATA = $data;
var SERIES = [];

// Called from the server
function setData(data) {
    DATA = data;
}

function addPoints() {
    addPoint();
    setTimeout(addPoints, 1000);
}

function addPoint() {

    if (DATA == null || DATA.length == 0) {
        return;
    }

    var x = ( new Date() ).getTime();
    var y = DATA[ DATA.length-1 ].y;

    SERIES.addPoint([x, y], true, true);
}

$(function () {

    $(document).ready(function() {

        Highcharts.setOptions({
            global: {
                useUTC: false
            }
        });

        $('#chart').highcharts({
            chart: {
                type: 'spline',
                animation: Highcharts.svg, // don't animate in old IE
                marginRight: 10,
                events: {
                    load: function() {
                        SERIES = this.series[0];
                        addPoints();
                    }
                }
            },
            title: {
                text: '$mainTitle'
            },
            xAxis: {
                type: 'datetime',
//                tickPixelInterval: 150,
                title: {
                    text: 'Time'
                }
            },
            yAxis: {
                title: {
                    text: '$yTitle'
                },
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }]
            },
            tooltip: {
                formatter: function() {
                    return '<b>'+ this.series.name +'</b><br/>'+
                        Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) +'<br/>'+
                        Highcharts.numberFormat(this.y, 2);
                }
            },
            legend: {
                enabled: false
            },
            series: [{
                name: 'Value',
                data: $data
            }]
        });
    });
});
