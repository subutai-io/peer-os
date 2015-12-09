Meta:
Narrative:
As a user
I want to verify if Subutai AngularJS
So that I can achieve a business goal

Scenario: The user observe AngularJS elements
Given the first user is on the home page of Subutai
Then the user should observe 'ng-login', 'ng-password'
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Monitoring
Then the user should observe 'ng-click peer', 'ng-click environment'
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Blueprint
Then the user should observe 'ng-create blueprint'
When the user clicks on the button: Create Blueprint
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
When the user clicks on the button: OK
Then the user observe button 'ng-add', 'ng-remove'
When the user clicks on the menu item: Containers
Then the user observe 'ng-Environment selector', 'ng-Containers type selector'
When the user clicks on the menu item: Console
Then the user observe 'ng-Peer', 'ng-Select peer'
When the user clicks on the menu item: User Identity
When the user clicks on the menu item: User management
Then the user observes button: 'ng-Add User'
When the user clicks on the menu item: Roles management
Then the user observes button: 'ng-Add Role'
When the user clicks on the menu item: Tokens
Then the user observes button: 'ng-Add Token', name: 'ng-Token name'
When the user clicks on the menu item: Peer Registration
Then the user observes button: 'ng-Create Peer'
When the user clicks on the menu item: Tracker
Then the user observes 'ng-Source selector'

Scenario: The user check the UI weight
Given the first user is on the home page of Subutai
When the user clicks on the menu item: Console
And the user selects management host from select menu
And the user enters console command: 'du -sh /apps/subutai-mng/current/system/io/subutai/webui*'
Then the user gets weight of Web UI
When the user enters console command: 'clear'
When the user enters console command: 'du -sh /apps/subutai-mng/current/deploy/webui-4.0.0-RC4.war'
Then the user gets weight of Web UI .war
And the user gets weight of all files
And the user should observe that Web UI is less than 10Mb

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