Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: The User should register a new user
Given the first user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: User Identity
And the user clicks on the menu item: User management
Then the user should observe web elements on: User management page
And the user should register a new user: 'test', 'test', 'test@test.com', 'test123', 'test123'
And the user should observe a new user
And the user should delete the role: Internal-System
And the user should delete a new user


