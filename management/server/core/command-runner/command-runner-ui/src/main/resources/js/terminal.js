function escapeInput(e) {
    console.log(document.getElementById("terminal").value);
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
        case 13:
            return true;
    }
}
document.getElementById("terminal").onkeydown = escapeInput;