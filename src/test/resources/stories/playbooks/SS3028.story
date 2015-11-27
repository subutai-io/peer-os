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
Then the user verify output console command and observe expected phrase: ' '
When the user enter console command: 'clear'
And the user enter console command: 'sudo subutai list -c'
And the user enter console command: sudo lxc-info -Ssip -n for Container Two
Then the user verify output console command and observe expected phrase: 'PID:'
When the user enter console command: ps -ef and grep PID
Then the user look all data