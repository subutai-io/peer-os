$('#subdiv').highcharts({
    title: {
        text: '${mainTitle}'
    },
    xAxis: {
        categories: [${categories}],
        labels: {
            rotation: -45,
            align: 'right',
            style: {
                fontSize: '10px'
            }
        }
    },
    yAxis: {
        title: {
            text: '${yTitle}'
        },
        plotLines: [{
            value: 0,
            width: 1,
            color: '#808080'
        }]
    },
    legend: {
        layout: 'vertical',
        align: 'right',
        verticalAlign: 'middle',
        borderWidth: 0
    },
    series: [{
        //name: 'Bishkek',
        data: [${values}]
    }]
});