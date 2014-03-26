var DATA = [];
var SERIES = [];
var polling = false;

// Called from the server
function setData(data) {
    DATA = data;
}

function startPolling(series, data) {

    SERIES = series;
    DATA = data;

    // Several calls addPoints() can cause crap in chart drawing
    if (!polling) {
        polling = true;
        addPoints();
    }
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