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


