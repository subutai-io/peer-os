function escapeInput(e) {
    switch (e.keyCode) {
        case 37:
            // left key pressed
            return false;
        case 38:
            // up key pressed
            return false;
        case 39:
            // right key pressed
            return false;
        case 40:
            // down key pressed
            return false;
        default:
            return true;
    }
}
document.getElementById("terminal").onkeydown = escapeInput;