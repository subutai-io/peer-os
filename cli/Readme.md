# What is this?

These are scripts that wrap the LXC and ZFS commands on the physical host OS
that runs containers. The scripts were written to prototype and demonstrate
the application of a scheme to manage templates, and their file system mounts
using ZFS snapshots.

These scripts can be used for now as the interface for managing templates on
the physical host OS. However they should be replaced with a better mechanism
for controlling these facilities. If used a package should be made for them
for distribution with the subutai OS.

# How to install and uninstall?

Currently these scripts expect there to be two ZFS zpools. One 'lxc' default 
zpool for system files, and a 'lxc-data' zpool for application data files. If
these are not present the scripts are useless.

# How does it work?

Describe the scheme here.


