Meta:

Narrative:
As a QA
I want to verify an Unprivileged containers PLAYBOOK SS-3028
So that I will create test scenarios

Scenario: Create Environment with 2 containers
Given the first user is on the home page of Subutai
And the user enter login and password: 'admin', 'secret'
And the user click on the button: Login
When the user click on the menu item: Environment
Then the user observe items of Environment menu
When the user click on the menu item: Blueprint
And the user click on the button: Create Blueprint
Then the user observe field: Enter blueprint name
When the user enter blueprint name: 'Sample blueprint'
And the user enter node name: 'Node'
And the user select template: 'master'
And the user enter number of containers: '2'
And the user enter SSH group ID: '0'
And the user enter host Group ID: '0'
And the user select quota size: 'TINY'
And the user click on the button: Add to node list
And the user click on the button: Create
Then the user observe created blueprint
When the user click on the icon: Build
Then the user observe build environment
When the user enter environment name: 'Local Environment'
And the user select peer: One
And the user select Strategie: 'DEFAULT-STRATEGY'
And the user click on the button: Place
Then the user observe icon: two containers
When the user click on the link: Environment Build List
Then the user observe popup: Build Environment
When the user click on the button: Build
Then the user observe header: Success!
And the user observe text: Your environment start creation.
When the user click on the button: OK
Then the user observe header: Success!
And the user observe text: Your environment has been created.

Scenario: Verify unprivileged containers
Given the first user is on the home page of Subutai
When the user click on the menu item: Console
And the user select any available resource host from select menu
And the user enter console command: 'sudo subutai list'
Then the user verify output console command and observe expected phrase: 'master'
And the user verify output console command and observe expected phrase: 'CONT/TEMP'
When the user enter console command: 'clear'
And the user enter console command: 'sudo subutai list -c'
And the user enter console command: sudo lxc-info -Ssip -n for Container One
Then the user verify output console command and observe expected phrase: 'PID:'
When the user enter console command: ps -ef and grep PID
Then the user verify output console command and observe expected phrase: 'upstart-udev-bridge --daemon'
When the user enter console command: 'clear'
And the user enter console command: 'sudo subutai list -c'
And the user enter console command: sudo lxc-info -Ssip -n for Container Two
Then the user verify output console command and observe expected phrase: 'PID:'
When the user enter console command: ps -ef and grep PID
Then the user verify output console command and observe expected phrase: 'upstart-udev-bridge --daemon'
When the user click on the button: Environment
And the user select current environment
And the user select first container
And the user enter console command: 'mknod /dev/tmp c 12 12'
Then the user verify output console command and observe expected phrase: 'mknod: ‘/dev/tmp’: Operation not permitted'
When the user enter console command: 'ping 10.10.10.1 -c 2'
Then the user verify output console command and observe expected phrase: '2 packets transmitted, 2 received, 0% packet loss'
When the user click on the button: Peer
And the user select again one resource host
When the user enter console command: 'clear'
And the user enter console command: 'sudo subutai list -c'
And the user enter console command: ls -l /var/lib/apps/subutai/current/var/lib/lxc/ContainerName/rootfs
Then the user verify output console command and observe expected phrase: 'total 0'

Scenario: Destroy Environment and Blueprint
Given the first user is on the home page of Subutai
When the user click on the menu item: Environment
And the user click on the menu item: Blueprint
And the user click on the icon: Remove
Then the user observe popup: Are you sure?
When the user click on the button: Delete
Then the user observe header: Deleted!
When the user click on the menu item: Environments
And the user click on the icon: Destroy
Then the user observe popup: Are you sure?
When the user click on the button: Delete
Then the user observe text: Your environment start deleting!
When the user click on the button: OK
Then the user observe text: Your environment has been destroyed.
And the user observe text: No data available in table

