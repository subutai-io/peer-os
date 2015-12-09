Meta:

Narrative:
As a QA
I want to verify the PLAYBOOK SS-3578
So that I will create test scenarios

Scenario: Create Local Environment
Given the first AWS user is on the home page of Subutai
And the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user runs bash script aws
And the user clicks on the menu item: Environment
Then the user observes items of Environment menu
When the user clicks on the menu item: Blueprint
And the user clicks on the button: Create Blueprint
Then the user observes field: Enter blueprint name
When the user enters blueprint name: 'example'
And the user enters node name: 'aws'
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
When the user enters environment name: 'hadoop'
And the user selects peer: One
And the user selects Strategie: 'ROUND_ROBIN'
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

Scenario: Read output of the command from Peer in terminal
Given the first AWS user is on the home page of Subutai
When the user clicks on console link
And the user clicks on link: environment on the console page
And the user selects 'hadoop' environment in select menu
And the user selects container one in select menu
And the user enters a command 'ping management -c 3' to Command field and press enter
Then the user should observes output of the command three received

Scenario: Destroy Environment and Blueprint
Given the first AWS user is on the home page of Subutai
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Blueprint
And the user clicks on the icon: Remove
Then the user observes popup: Are you sure?
When the user clicks on the button: Delete
And the user clicks on the menu item: Environments
And the user clicks on the icon: Destroy
Then the user observes popup: Are you sure?
When the user clicks on the button: Delete
Then the user observes text: Your environment is being deleted!
When the user clicks on the button: OK
Then the user observes text: Your environment has been destroyed.
And the user observes text: No data available in table
