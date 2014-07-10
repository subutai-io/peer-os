function escapeInput(e) {
    console.log(e.keyCode);
    switch (e.keyCode) {
        case 37:
            // left key pressed
            break;
        case 38:
            // up key pressed
            break;
        case 39:
            // right key pressed
            break;
        case 40:
            // down key pressed
            break;
    }
}
document.getElementById("terminal").onkeydown = escapeInput;