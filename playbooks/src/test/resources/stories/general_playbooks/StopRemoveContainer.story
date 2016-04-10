Meta:

Narrative:
As a user
I want to verify stop remove containers
So that I can achieve it by using this test

Scenario: the user should create an Environment with 3 containers
Given the first user is on the Home page of Subutai
And the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
And the user should find template: Master
And the user creates environment with 3 containers using template: Master
Then the user should observe 3 containers

Scenario: the user should click on Check/Stop/Remove containers
When the user clicks on the menu item: Containers
And the user should do container: check
And the user should do container: stop
And the user should do container: start
And the user removes one container
And the user clicks on the menu item: Environments
Then the user should observe 2 containers

Scenario: the user should remove an Environment
Then the user destroys created environment
