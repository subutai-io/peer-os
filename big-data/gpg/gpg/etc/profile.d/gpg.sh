#!/bin/bash

envfile="$HOME/.gnupg/gpg-agent.env"
if [[ -e "$envfile" ]] && kill -0 $(grep GPG_AGENT_INFO "$envfile" | cut -d: -f 2) 2>/dev/null; then
    eval "$(cat "$envfile")"
else
    eval "$(gpg-agent --daemon --allow-loopback-pinentry --enable-ssh-support --pinentry-program `which pinentry-curses`)"
fi
export GPG_AGENT_INFO # the env file does not contain the export statement
export SSH_AUTH_SOCK # enable gpg-agent for ss
