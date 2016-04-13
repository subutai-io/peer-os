Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: The First User Sends Request
Given the first user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Peer Registration
And the user clicks on the button: Сreate peer
Then the user should create a peer request with: '158.181.147.66', 'test'
And the user should observe button: Cancel

Scenario: The Second User Approve Request
Given the second user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Peer Registration
Then the user should approve the peer with: 'test'

Scenario: The First User Create an Environment with two RH
Given the first user is on the Home page of Subutai
When the user should wait a few seconds
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
When the user clicks on the Environment's mode: Advanced
Then the user should put the First RH in workspace
And the user should put the Second RH in workspace
When the user clicks on templates
And the user should find template: Master
Then the user should drag and drop template to RH1: Master
Then the user should drag and drop template to RH2: Master
And the user should create an environment
And the user destroys created environment

Scenario: the user should unregister Peer
When the user clicks on the menu item: Peer Registration
Then the user unregister peer
