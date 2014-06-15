#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. $DIR/etc/subutai/bsfl
init

if [ "$USER" != "root" ]; then 
  msg_error "You must be root to install these scripts."
  exit 1
fi

if [ ! -d /etc/subutai ]; then
  mkdir /etc/subutai
fi

cp $DIR/etc/subutai/* /etc/subutai/
chmod 755 /etc/subutai/*
cp $DIR/bin/subutai-* /bin     # Shouldn't this go into /usr/bin? or elsewhere
chmod 755 /bin/subutai-*

msg_ok "Installed all scripts and configuration files successfully"
msg_info "Initializing Subutai Scripts"

. /etc/subutai/config
subutai-setup
msg_ok "Setup successful"

