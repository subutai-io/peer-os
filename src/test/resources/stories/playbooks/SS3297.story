Meta:

Narrative:
As a QA
I want to verify the PLAYBOOK SS-3297
So that I will create test scenarios

Scenario: The user run  vagrant console commands using script
Given the first user is on the home page of Subutai
When the user run bash script vagrant

Scenario: Create Environment
Given the vagrant user is on the home page of Subutai
And the user enter login and password: 'admin', 'secret'
And the user click on the button: Login
When the user click on the menu item: Environment
When the user click on the menu item: Blueprint
And the user click on the button: Create Blueprint
Then the user observe field: Enter blueprint name
When the user enter blueprint name: 'example'
And the user enter node name: 'keshig'
And the user select template: 'hadoop'
And the user enter number of containers: '2'
And the user enter SSH group ID: '0'
And the user enter host Group ID: '0'
And the user select quota size: 'TINY'
And the user click on the button: Add to node list
And the user click on the button: Create
Then the user observe created blueprint
When the user click on the icon: Build
Then the user observe build environment
When the user enter environment name: 'hadoop'
And the user select peer: One
And the user select Strategie: 'ROUND_ROBIN'
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
When the user click on the menu item: Console
And the user click on the button: Environment
And the user select current environment
And the user select container one in select menu
And the user enter console command: 'ping management -c 3'
Then the user verify output console command and observe expected phrase: '3 packets transmitted, 3 received, 0% packet loss'
When the user select container two in select menu
And the user enter console command: 'ping management -c 3'
Then the user verify output console command and observe expected phrase: '3 packets transmitted, 3 received, 0% packet loss'

Scenario: Destroy Environment and Blueprint
Given the vagrant user is on the home page of Subutai
When the user click on the menu item: Environment
And the user click on the menu item: Blueprint
And the user click on the icon: Remove
Then the user observe popup: Are you sure?
When the user click on the button: Delete
Then the user observe header: Deleted
When the user click on the menu item: Environments
And the user click on the icon: Destroy
Then the user observe popup: Are you sure?
When the user click on the button: Delete
Then the user observe text: Your environment start deleting!
When the user click on the button: OK
Then the user observe text: Your environment has been destroyed.
And the user observe text: No data available in table
And the user run bash script: reset virtual box and delete vagrant directory