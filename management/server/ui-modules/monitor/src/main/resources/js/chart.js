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
                        startPolling(this.series[0], $data);
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
                    return Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) +'<br/>'
                        + Highcharts.numberFormat(this.y, 2);
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
