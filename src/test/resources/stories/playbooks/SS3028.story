Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: scenario description
Given the first user is on the home page of Subutai
And the user enter login and password: 'admin', 'secret'
And the user click on the button: Login
When the user click on the menu item: Console
And the user select any available resource host from select menu
And the user enter console command: 'sudo subutai list'
Then the user verify output console command and observe expected phrase: 'master'
And the user verify output console command and observe expected phrase: 'CONT/TEMP'
When the user enter console command: 'clear'
And the user enter console command: 'sudo subutai list -c'
And the user enter console command: sudo lxc-info -Ssip -n for Container One
Then the user verify output console command and observe expected phrase: 'STOPPED'
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

