Meta:

Narrative:
As a QA
I want to verify an Unprivileged containers PLAYBOOK SS-3028
So that I will create test scenarios

Scenario: Create Environment with 2 containers
Given the first user is on the home page of Subutai
And the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Environment
Then the user observes items of Environment menu
When the user clicks on the menu item: Blueprint
And the user clicks on the button: Create Blueprint
Then the user observes field: Enter blueprint name
When the user enters blueprint name: 'Sample blueprint'
And the user enters node name: 'Node'
And the user selects template: 'master'
And the user enters number of containers: '2'
And the user enters SSH group ID: '0'
And the user enters host Group ID: '0'
And the user selects quota size: 'TINY'
And the user clicks on the button: Add to node list
And the user clicks on the button: Create
Then the user observes created blueprint
When the user clicks on the icon: Build
Then the user observes build environment
When the user enters environment name: 'Local Environment'
And the user selects peer: One
And the user selects Strategie: 'DEFAULT-STRATEGY'
And the user clicks on the button: Place
Then the user observes icon: two containers
When the user clicks on the link: Environment Build List
Then the user observes popup: Build Environment
When the user clicks on the button: Build
Then the user observes header: Success!
And the user observes text: Your environment start creation.
When the user clicks on the button: OK
Then the user observes header: Success!
And the user observes text: Your environment has been created.

Scenario: Verify unprivileged containers
Given the first user is on the home page of Subutai
When the user clicks on the menu item: Console
And the user selects any available resource host from select menu
And the user enters console command: 'sudo subutai list'
Then the user verifies output console command and observe expected phrase: 'master'
And the user verifies output console command and observe expected phrase: 'CONT/TEMP'
When the user enters console command: 'clear'
And the user enters console command: 'sudo subutai list -c'
And the user enters console command: sudo lxc-info -Ssip -n for Container One
Then the user verifies output console command and observe expected phrase: 'PID:'
When the user enters console command: ps -ef and grep PID
Then the user verifies output console command and observe expected phrase: 'upstart-udev-bridge --daemon'
When the user enters console command: 'clear'
And the user enters console command: 'sudo subutai list -c'
And the user enters console command: sudo lxc-info -Ssip -n for Container Two
Then the user verifies output console command and observe expected phrase: 'PID:'
When the user enters console command: ps -ef and grep PID
Then the user verifies output console command and observe expected phrase: 'upstart-udev-bridge --daemon'
When the user clicks on the button: Environment
And the user selects current environment
And the user selects first container
And the user enters console command: 'mknod /dev/tmp c 12 12'
Then the user verifies output console command and observe expected phrase: 'mknod: ‘/dev/tmp’: Operation not permitted'
When the user enters console command: 'ping 10.10.10.1 -c 2'
Then the user verifies output console command and observe expected phrase: '2 packets transmitted, 2 received, 0% packet loss'
When the user clicks on the button: Peer
And the user selects again one resource host
When the user enters console command: 'clear'
And the user enters console command: 'sudo subutai list -c'
And the user enters console command: ls -l /var/lib/apps/subutai/current/var/lib/lxc/ContainerName/rootfs
Then the user verifies output console command and observe expected phrase: 'total 0'

Scenario: Destroy Environment and Blueprint
Given the first user is on the home page of Subutai
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Blueprint
And the user clicks on the icon: Remove
Then the user observes popup: Are you sure?
When the user clicks on the button: Delete
Then the user observes header: Deleted
When the user clicks on the menu item: Environments
And the user clicks on the icon: Destroy
Then the user observes popup: Are you sure?
When the user clicks on the button: Delete
Then the user observes text: Your environment is being deleted!
When the user clicks on the button: OK
Then the user observes text: Your environment has been destroyed.
And the user observes text: No data available in table

