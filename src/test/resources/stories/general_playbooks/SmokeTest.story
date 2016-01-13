Meta:

Narrative:
As a QA team
We want to verify Subutai web elements
So that We create smoke test for it

Scenario: The User should observe Subutai web elements
Given the first user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Monitoring
Then the user should observe web elements on: Monitoring page
When the user clicks on the menu item: Blueprints
Then the user should observe web elements on: Blueprints page
When the user clicks on the menu item: Environments
Then the user should observe web elements on: Environments page
When the user clicks on the menu item: Containers
Then the user should observe web elements on: Containers page
When the user clicks on the menu item: Console
And the user chooses: Management host
And the user enters console command: 'pwd'
Then the user should observe output of the pwd command
When the user chooses: Local host
And the user enters console command: 'pwd'
Then the user should observe output of the pwd command
When the user clicks on the menu item: User management
Then the user should observe web elements on: User management page
When the user clicks on the menu item: Role management
Then the user should observe web elements on: Role management page
When the user clicks on the menu item: Tokens
Then the user should observe web elements on: Tokens page
When the user clicks on the menu item: Peer Registration
Then the user should observe web elements on: Peer Registration page
When the user clicks on the menu item: Resource Nodes
Then the user should observe web elements on: Resource Nodes page
When the user clicks on the menu item: Tracker
Then the user should observe web elements on: Tracker page
When the user clicks on the menu item: Plugins
Then the user should observe web elements on: Plugins page