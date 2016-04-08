Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: the user should create an Environment
Given the first user is on the Home page of Subutai
And the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login


Scenario: the user should install and work with Generic plugin
When the user clicks on the menu item: Bazaar
Then the user search plugin: 'Generic'
When the user should install plugin
And the user clicks on Launch button
Then the user creates profile name
When the user clicks on the buton: Configure operations
And the user clicks on the buton: Add operation
Then the user should add an operation
When the user moves to page Manage
Then the user should execute the ls opeartion

Scenario: the user should uninstall: operation
When the user moves to page Create
Then the user should delete a profile
When the user clicks on the menu item: Bazaar
Then the user search plugin: 'Generic'
And the user uninstall plugin
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
Then the user destroys created environment