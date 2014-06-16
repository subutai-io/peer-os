# Thoughts About Git

The master creation script is going to need to do a few things before taking
the template snapshot. In fact the template snapshot might be best deferred
to be handled by another script. This is because we may need to start up the
container to perform some operations within the container. I would like to
see if we can do this without having to enter the container from the host OS.

So for Git to create the initial repository in /etc for the master we need to
issue the following commands within the /etc directory. Incidentally this should
work just fine whether we do this from inside or outside of the container.

Here is the sequence of commands to setup Git tracking of /etc in the master:

git init
git config --global user.email 'subutai@safehaus.org'
git config --global user.name 'subutai'
echo '*.old' >> .gitignore
echo blkid.tab >> .gitignore
echo adjtime >> .gitignore
git add .gitignore
git commit -m 'setting up git for master'
git add -A
git commit -m 'initial master /etc import for master'

This can all be done from outside of the container from within the rootfs of
the container. Git does not care it deposites the repository files locally in 
the /etc/.git directory.

## Updates and note on managing configuration points

It seems the best strategy here due to the inability to use simlinks and/or
hardlinks is to create the Git repository at the root / instead of in the 
/etc directory. In order to do this we also added a slew if .gitignores to
not pull in things like vmlinuz, initrd.img and all the directories in the
root folder. 

In the /etc/subutai/ folder there is a config file for subutai. It also
now contains the SUBUTAI_CONFIG_PATH with /etc in it but this variable
can be like the colon separated PATH variable. This way it lists all the
different configuration points.

In the same light we also added a SUBUTAI_APP_DATA_PATH with just /var 
in it for now but many can be added. This however has nothing to do with
git based configuration tracking.

## Adding New Configuration Path Points

We should create a subutai-add-config which can be used to add another
entry to the colon separated SUBUTAI_CONFIG_PATH. It should however first
test to make sure that the path to be added is not already contained by
the paths in the set. This is easy enough. It can then also add the new
path to git. 

NOTE: that to add a path for which a parent of the path is ignored by 
the .gitignores entries you must use the git add -f switch to force the 
addition. For example opt is ignored but we want to add /opt/hadoop/conf
as a configuration path. git add /opt/hadoop/conf will complain since 
opt is ignored. To force this issue a git add -f /opt/hadoop/conf. Then
a commit can be issued.

This will add the path and track changes under the newly added config
path entry.

Unfortunately git will need 

# Subutai Templates

We MUST be very careful with our nomenclature and define what exactly a 
template is and what a tsar file is and how this all works together.

An LXC container on the system is not by itself a template. To become a 
template it MUST have two snapshot sets applied one before the other. The 
first is the '@created' snapshot set to all zfs filesystems used by the
container. This snapshot set is always present on any container and is 
applied at creation time. The other snapshot set is the '@template' set
and this is applied when the LXC container is promoted to a template.

With these two snapshot sets the container is effectively a template. Once
turned into a template, the container can no longer be run. It is locked
to prevent users from unknowingly modifying the template. If a user modifies
the template after the '@template' snapshot sets are taken, then these changes
are no longer accounted for. Future clones will cause  all clone ancestors of the template. 

There are two ways in which a container can be promoted to a template. The 
first explicit way is to use the subutai-template command. This command 
will apply the 'template' snapshots to all the container ZFS datasets after
performing the following operations:

  o commit changes to config paths in the local git repo on $name branch
  o overwrite the package manifest kept in the /etc/subutai directory
  o shutdown the container

At this point the rootfs and each file system mount dataset is snapshotted 
with '@template'. Then a binary block level differential is generated for 
each ZFS dataset of the container. 


Basically there is no reason for us to create an additional file format that
needs to be kept locally on the physical host along side an lxc image





NOTES:

  - might be a good idea to prevent users of potential naming conflicts when
    cloning containers that are already registered as templates within the 
    Subutai template registry

  - also we should not allow users to run templates since they will get out
    of synchronization with the snapshots

  - later on we can perhaps allow this however there must be no child templates
    so we can re-template snapshot the container datasets

  - we can allow containers to be untemplated too if and only if there are no
    existing child templates

