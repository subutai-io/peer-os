#!/bin/bash
# For run script: echo “my_password” | ./sshaskpass.sh ssh user@someMachine “ls -l”
if [ -n "$SSH_ASKPASS_TMPFILE" ]; then
    cat "$SSH_ASKPASS_TMPFILE"
    exit 0
elif [ $# -lt 1 ]; then
    echo "Usage: echo password | $0 <ssh command line options>" >&2
    exit 1
fi
 
sighandler() {
    rm "$TMP_PWD"
}
 
TMP_PWD=$(mktemp)
chmod 600 "$TMP_PWD"
trap 'sighandler' SIGHUP SIGINT SIGQUIT SIGABRT SIGKILL SIGALRM SIGTERM
 
export SSH_ASKPASS=$0
export SSH_ASKPASS_TMPFILE=$TMP_PWD
 
[ "$DISPLAY" ] || export DISPLAY=dummydisplay:0
read password
echo $password >> "$TMP_PWD"
 
# use setsid to detach from tty
exec setsid "$@"

rm "$TMP_PWD"