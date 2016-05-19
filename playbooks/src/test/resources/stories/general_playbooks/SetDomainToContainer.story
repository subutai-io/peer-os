Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: the user creates an environment with apache template
Given the first user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
And the user should find template: Apache
And the user creates environment using template: Apache
And the user clicks on the button: Configure
Then the user set domain

Scenario: the user add domain to container
When the user clicks on the menu item: Containers
And the user clicks on the button: Configure
And the user clicks on the checkbox: Add domain
And the user clicks on the button: Save

Scenario: the user test 'test.qa'
Given the second user open domain test
Then the user should observe header apache

Scenario: the user should destroy environment
Given the first user is on the Home page of Subutai
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
And the user should wait a few seconds
Then the user destroys created environment