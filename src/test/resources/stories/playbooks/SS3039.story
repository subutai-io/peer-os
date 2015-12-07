Meta:
Narrative:
As a user
I want to verify if Subutai AngularJS
So that I can achieve a business goal

Scenario: The user observe AngularJS elements
Given the first user is on the home page of Subutai
Then the user should observe 'ng-login', 'ng-password'
Given the user enter login and password: 'admin', 'secret'
And the user click on the button: Login
When the user click on the menu item: Monitoring
Then the user should observe 'ng-click peer', 'ng-click environment'
When the user click on the menu item: Environment
And the user click on the menu item: Blueprint
Then the user should observe 'ng-create blueprint'
When the user click on the button: Create Blueprint
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
When the user click on the button: OK
Then the user observe button 'ng-add', 'ng-remove'
When the user click on the menu item: Containers
Then the user observe 'ng-Environment selector', 'ng-Containers type selector'
When the user click on the menu item: Console
Then the user observe 'ng-Peer', 'ng-Select peer'
When the user click on the menu item: User Identity
When the user click on the menu item: User management
Then the user observe button: 'ng-Add User'
When the user click on the menu item: Roles management
Then the user observe button: 'ng-Add Role'
When the user click on the menu item: Tokens
Then the user observe button: 'ng-Add Token', name: 'ng-Token name'
When the user click on the menu item: Peer Registration
Then the user observe button: 'ng-Create Peer'
When the user click on the menu item: Tracker
Then the user observe 'ng-Source selector'

Scenario: The user check the UI weight
Given the first user is on the home page of Subutai
When the user click on the menu item: Console
And the user select management host from select menu
And the user enter console command: 'du -sh /apps/subutai-mng/current/system/io/subutai/webui*'
Then the user get weight of Web UI
When the user enter console command: 'clear'
When the user enter console command: 'du -sh /apps/subutai-mng/current/deploy/webui-4.0.0-RC4.war'
Then the user get weight of Web UI .war
And the user get weight of all files
And the user should observe that Web UI is less than 10Mb

Scenario: Destroy Environment and Blueprint
Given the first user is on the home page of Subutai
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