function escapeInput(target) {
    alert("key pressed in " + target.charCode);
}
document.getElementById("terminal").onkeydown = escapeInput;