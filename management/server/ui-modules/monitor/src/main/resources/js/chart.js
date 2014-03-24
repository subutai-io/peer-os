var SERIES;
var POINT;
var i = 0;

function setSeries(s) {
    SERIES = s;
}

function addPoint() {
    if (POINT != null) {
        SERIES.addPoint(POINT, true, true);
        POINT = null;
    }

    console.log("i: " + i);
}

function createPoint() {
    var x = ( new Date() ).getTime();
    var y = Math.random();

//    POINT = [x, y];
    POINT = [x, i];
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
                        setSeries(this.series[0]);
                        setInterval(addPoint, 1000);
                        setInterval(createPoint, 1000);
                    }
                }
            },
            title: {
                text: ''
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
                    text: 'Value'
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
                data: (function() {

                    var data = [];
                    var time = ( new Date() ).getTime();

                    for (var i = 0; i < 10; i++) {
                        data.push({
                            x: time + i * 1000,
//                            y: Math.random()
                            y: 0
                        });
                    }

                    return data;
                })()
            }]
        });
    });
});