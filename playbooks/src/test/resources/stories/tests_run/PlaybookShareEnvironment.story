Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: scenario description
Given the first user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: User Identity
And the user clicks on the menu item: User management
Then the user should register a new user: 'test', 'test', 'test@test.com', 'test123', 'test123'

Scenario: the user Creates and share the environment with created user
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
And the user should find template: Master
And the user creates environment using template: Master
And the user clicks on the button: share
And the user shares the environment with user: test
And the user removes the delete role from the user
And the user clicks on the button: Save
Then the user clicks on the button: OK
And the user should click on admin icon

Scenario: the test user should observe and try to delete the environment
Given the user enters login and password: 'test', 'test123'
And the user clicks on the button: Login
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
And the user clicks on the icon: delete
And the user clicks on the button: delete
Then the user clicks on the button: OK
And the user should click on test icon

Scenario: the admin user remove the environment and test user
Given the first user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
Then the user destroys created environment
When the user clicks on the menu item: User Identity
And the user clicks on the menu item: User management
Then the user should delete a new user