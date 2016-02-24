# Subutai Social repository

This repository contains source code of Subutai Social Console Project.
This is a multi-module maven Java project.

## Building the project

###Prerequisites

To build the project, you need to have the following tools:
- Maven version 3.2.2 or later
- Oracle JDK 1.7 or later
- Unlimited strength files (specific for Java version). See http://stackoverflow.com/a/6481658

###Build steps

- Clone the project by using:

    `git clone https://github.com/subutai-io/Subutai.git`

- Start maven build ( cd to management directory and issue ):

    `mvn clean install`

After this you will have `management/server/server-karaf/target` directory with **subutai-{version}.tar.gz** archive
which contains custom Karaf distribution of SS Console application.
Untar it to some directory and execute `{distr}/bin/karaf`.

After that go to `https://you_host_ip:8443` in your browser.