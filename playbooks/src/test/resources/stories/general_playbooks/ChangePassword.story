Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: The User should register a new user and change password
Given the first user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: User Identity
And the user clicks on the menu item: User management
Then the user should observe web elements on: User management page
And the user should register a new user: 'test', 'test', 'test@test.com', 'test123', 'test123'
And the user should click on admin icon

Scenario: the user login under testUser account
Given the user enters login and password: 'test', 'test123'
And the user clicks on the button: Login
When the user clicks on the menu item: User Identity
When the user clicks on the menu item: Account Settings
And the user should click on title: Change password
Then the user should change password: 'test123', 'test222', 'test222'
And the user should click on test icon

Scenario: the user login with the new password
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: User Identity
And the user clicks on the menu item: User management

Scenario: the user delete himself
Then the user should delete a new user
