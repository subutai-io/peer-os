Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: the user should Upload template AppScale
Given the first user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Kurjun
And the user click on the button: Add Template
Then the user uploads template
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
And the user should find template: Cassandra