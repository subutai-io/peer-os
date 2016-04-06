Meta:

Narrative:
As a QA team
We want to verify Subutai web elements
So that We create smoke test for it

Scenario: The User should observe Subutai web elements (part 1)
Given the first user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Monitoring
Then the user should observe web elements on: Monitoring page
When the user clicks on the upper menu item: Register Peer
Then the user should observe web elements on drop down menu: Register Peer
When the user click on the upper menu icon: Notification
Then the user should observe web elements on drop down menu: Notifications
And the user should observe user name: admin
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments

Scenario: the user should create the Local Environment
When the user should find template: Master
And the user creates environment using template: Master
Then the user destroys created environment
When the user clicks on the Environment's mode: Advanced
Then the user should observe web elements on: Advanced mode page

Scenario: the user should observe Subutai web Elements (part 2)
When the user clicks on the menu item: Containers
Then the user should observe web elements on: Containers page
When the user clicks on the menu item: Kurjun
Then the user should observe web elements on: Kurjun page
When the user clicks on the menu item: Console
And the user chooses: Management host
And the user enters console command: 'pwd'
Then the user should observe output of the pwd command

Scenario: the user should observe Web elements from User Identity
When the user clicks on the menu item: User Identity
And the user clicks on the menu item: User management
Then the user should observe web elements on: User management page
When the user clicks on the menu item: Role management
Then the user should observe web elements on: Role management page
When the user clicks on the menu item: Account Settings
Then the user should observe web elements on: Account Settings page
When the user clicks on the menu item: Tokens
Then the user should observe web elements on: Tokens page

Scenario: the user should observe Subutai web Elements (part 3)
When the user clicks on the menu item: Peer Registration
Then the user should observe web elements on: Peer Registration page
When the user clicks on the menu item: Resource Hosts
Then the user should observe web elements on: Resource Hosts page
When the user clicks on the menu item: Tracker
Then the user should observe web elements on: Tracker page
When the user clicks on the menu item: Bazaar
Then the user should observe web elements on: Bazaar page

Scenario: the user should observe Subutai web Elements from System Settings
When the user clicks on the menu item: System Settings
And the user clicks on the menu item: Peer Settings
Then the user should observe web elements on: Peer Settings page
When the user clicks on the menu item: Kurjun Settings
Then the user should observe web elements on: Kurjun Settings page
When the user clicks on the menu item: Network Settings
Then the user should observe web elements on: Network Settings page
When the user clicks on the menu item: Advanced
Then the user should observe web elements on: Advanced page
When the user clicks on the menu item: About
Then the user should observe web elements on: About page