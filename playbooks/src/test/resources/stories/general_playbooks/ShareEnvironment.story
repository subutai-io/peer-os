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