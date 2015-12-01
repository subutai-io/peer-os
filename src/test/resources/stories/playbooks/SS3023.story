Meta:

Narrative:
As a QA
I want to verify the PLAYBOOK SS-3023
So that I will create test scenarios

Scenario: Create Local Environment
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

Scenario: Read output of the command from RH in terminal
Given the first user is on the home page of Subutai
When the user click on console link
Then the user observe console module UI with select menu of available resource hosts
When the user select any available resource host from select menu
And the user enter a command 'echo test' to Command field and press enter
Then the user should observe output of the command

Scenario: Read output of the command from Peer in terminal
Given the first user is on the home page of Subutai
When the user click on console link
And the user click on link: environment on the console page
And the user select 'Local Environment' environment in select menu
And the user select container one in select menu
And the user enter a command 'echo test' to Command field and press enter
Then the user should observe output of the command

Scenario: Navigate to resource host pgp
Given the first user is on the home page of Subutai
When the user click on console link
And the user select any available resource host from select menu
And the user get resource host ID and go pgp key url
Then the user should observe public Key of the resource host in GPG Armored text

Scenario: Navigate to resource host container
Given the first user is on the home page of Subutai
When the user click on console link
And the user click on link: environment on the console page
And the user select 'Local Environment' environment in select menu
And the user select container one in select menu
And the user get container ID and go pgp key url
Then the user should observe public Key of the container in GPG Armored text

Scenario: Destroy Environment and Blueprint
Given the first user is on the home page of Subutai
When the user click on the menu item: Environment
And the user click on the menu item: Blueprint
And the user click on the icon: Remove
Then the user observe popup: Are you sure?
When the user click on the button: Delete
And the user click on the menu item: Environments
And the user click on the icon: Destroy
Then the user observe popup: Are you sure?
When the user click on the button: Delete
Then the user observe text: Your environment start deleting!
When the user click on the button: OK
Then the user observe text: Your environment has been destroyed.
And the user observe text: No data available in table