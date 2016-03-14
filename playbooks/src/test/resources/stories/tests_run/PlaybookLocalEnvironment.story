Meta:

Narrative:
As a QA
I want to verify a functional: Local Environment
So that I wrote acceptance test for it

Scenario: Create Local Environment
Given the first user is on the Home page of Subutai
And the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
And the user configure pgp plugin
When the user clicks on the menu item: User Identity
When the user sets pgp Key
And the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
And the user creates environment using template: Mongo