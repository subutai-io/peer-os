sudo -s <<EOF 
systemctl stop snappy-autopilot.service
systemctl disable snappy-autopilot.service
systemctl stop snappy-autopilot.timer
systemctl disable snappy-autopilot.timer
mount -o remount,rw /
rm -rf /lib/systemd/system/snappy-autopilot.timer
EOF
