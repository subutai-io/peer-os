Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: The User Add the role
Given the first user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: User Identity
And the user clicks on the menu item: Role management
And the user clicks on the button: Add role
And the user input the role name: 'iManagement'
And the user click on icon add role: idenity-management
And the user clicks on the button: Save
And the user should wait a few seconds
Then the user should delete the role: iManagement
