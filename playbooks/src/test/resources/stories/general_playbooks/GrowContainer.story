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
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
And the user should find template: Master
And the user creates environment using template: Master
And the user clicks on icon edit
And the user should find template: Master
And the user grows environment using template: Master
Then the user should observe 2 containers

Scenario: the user should remove an Environment
Then the user destroys created environment