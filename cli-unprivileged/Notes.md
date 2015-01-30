# Thoughts About Git

The master has a local git repository initialized at the file system root. Yes
that is /.git. Several .gitignores are needed to prevent git from pulling in 
everything or complaining about other directories and files at the root level.

Ignoring everything however makes it so adding an additional path requires the
git add -f switch. Without the switch adding directories and files under 
ignored parent paths like for example /opt (which is ignored) we get an error
telling us that you are trying to add stuff under ignored paths. For example
when adding /opt/hadoop/conf.

NOTE: git must be installed within the container since the mounts will not 
be present to enable additional configuration paths in for example /opt. 

Additional information needs to be maintained about additional configuration
paths. Perhaps this should be kept in a /etc/subutai directory and updated by
a custom script which also does the git add operation for the path. 
Additionally the script can validate the path, and make sure it does not 
overlap with existing paths in a colon separated list of path elements.

In the /etc/subutai/ folder there is a config file for subutai. It also
now contains the SUBUTAI_CONFIG_PATH with /etc in it but this variable
can be like the colon separated PATH variable. This way it lists all the
different configuration points.

In the same light we also added a SUBUTAI_APP_DATA_PATH with just /var 
in it for now but many can be added. This however has nothing to do with
git based configuration tracking.

# Subutai Templates

We MUST be very careful with our nomenclature and define what exactly a 
template is and what a debian package file is and how this all works together.

An LXC container on the system is not by itself a template. To become a 
template it MUST have a snapshot with the name 'template'. The master is
by default is already a template. 

Once a container becomes a template, the container can no longer be run. 
A pre-start hook script is used to only enable containers that do not have 
the 'template' snapshot to run. It is locked to prevent users from modifying 
the template. 

There are two ways in which a container can be promoted to a template. The 
first explicit way is to use the subutai-template command. This command 
will apply the 'template' snapshots to all the container ZFS datasets after
performing the following operations:

  o commit changes to config paths in the local git repo on $name branch,
    note that when cloning from a template, a new branch is created with 
    the name of the new child container
  o overwrite the package manifest kept in the /etc/subutai directory
  o shutdown the container

At this point the rootfs and each file system mount dataset is snapshotted 
with '@template'. Then a binary block level differential is generated for 
each ZFS dataset of the container. The new local git branch is committed 
and pushed to the remote site level repository running on the management 
server. Once the branch is pushed, the dataset deltas are uploaded for the
new template to the management console and associated with the new branch.
The container configuration files are also uploaded. The package manifest 
is already available from git but it can be uploaded as well to have change
calculations take place without accessing git.

So we just created a site wide template for Subutai from a container. The
new template can then be imported by all the physical machines with the 
same architecture under the control of Subutai. Users should be able to 
publish their templates and enable others to use them. We have a rough idea
of how to achieve this. The best way is to perform a similar operation
that the physical server performed to push the template files up to the
site management server and push the new git branch. There are some 
complications however:

  o we need to qualify published templates by the publisher
  o when pulling down and importing we need to qualify by user and publisher
  o a publisher.desriptor scheme should probably be used for local 
    physical machine based qualification of imported templates
  o templates developed locally will not be qualified especially if not
    pubished: the system should make up for this after it is published
    by either renaming containers and snapshots or by just doing some
    local accounting

zfs rename -r lxc/master@foo @akarasulu.foo 
 - this recursively renames the foo snapshot to akarasulu.foo with akarasulu
   as the new publisher qualification on the template

git branch -m foo akarasulu.foo
 - this renames the foo branch to akarasulu.foo locally
 - the same can be done on the site local repository in the management server

Rename operations for the container however are another story. It seems we
will have to implement the rename ourselves with is a matter of massaging
the config file and moving the container, but a zfs remount will be needed
for the rootfs of the container before doing the move.

The inability to run templates may seem a bit restrictive however it is only 
a perception which will not inhibit users in most cases. However the benefits
are massive. The fact that there is no change to the template allows for us
to have read only, write once branches in git which can replicate easily once
with multi-datacenter configurations of Subutai. 


NOTES:

  - Each version of Subutai will lock into a specific version of Ubuntu
    server. It will not allow a heterogenous mix of different physical and
    LXC containers. However Subutai may have multiple APT repositories for
    different architectures like ARM or x86 etc. There may be a heterogenous
    mix of physical machines this way. Each architecture will have a master.

  - might be a good idea to prevent users of potential naming conflicts when
    cloning containers that are already registered as templates within the 
    Subutai template registry

  - also we should not allow users to run templates since they will get out
    of synchronization with the snapshots

  - later on we can perhaps allow this however there must be no child templates
    so we can re-template snapshot the container datasets

  - we can allow containers to be untemplated too if and only if there are no
    existing child templates

  - IDEA: maybe we can use different LXC groups to manage containers for 
    different users. Or better yet we might be able to use different users
    on the physical machine for each user in the system, and use unprivileged
    containers for added security.

# Required packages on physical server

  - build-essential
  - packaging-dev

