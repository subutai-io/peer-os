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



