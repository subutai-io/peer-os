var SERIES;
var POINT;
var Y = 0;

//function setSeries(s) {
//    SERIES = s;
//}

//function addPoint() {
//    if (POINT != null) {
//        SERIES.addPoint(POINT, true, true);
//        POINT = null;
//    }
//
//    console.log("i: " + i);
//}

//function createPoint() {
//    var x = ( new Date() ).getTime();
////    var y = Math.random();
////    POINT = [x, y];
////    var point = [x, i];
//    SERIES.addPoint( [x, i], true, true );
//}


function setY(y) {
    Y = y;
    console.log("setY: " + Y);
}

function addPoint() {
    var x = ( new Date() ).getTime();
    SERIES.addPoint( [x, Y], true, true );
    console.log("addPoint: " + Y);
}

function onLoad(series) {
    SERIES = series;
    setInterval(addPoint, 1000);
//    setInterval(createPoint, 1000);
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
                        onLoad( this.series[0] );
//                        setSeries(this.series[0]);
//                        setInterval(addPoint, 1000);
//                        setInterval(createPoint, 1000);
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
                data: (function() {

                    var data = [];
                    var time = ( new Date() ).getTime();

                    for (i = -19; i <= 0; i++) {
//                    for (var i = 0; i < 10; i++) {
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