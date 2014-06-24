#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. $DIR/debian/etc/subutai/bsfl
init

if [ "$USER" != "root" ]; then 
  msg_error "You must be root to uninstall these scripts."
  exit 1
fi

if [ -d /etc/subutai ]; then
  rm -rf /etc/subutai
fi

rm /usr/bin/subutai-*

msg_ok "Uninstalled all scripts and configuration files successfully"

