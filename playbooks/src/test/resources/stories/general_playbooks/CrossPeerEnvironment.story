Meta:

Narrative:
As a QA
I want to verify a functional: Cross Peer Environment
So that I wrote acceptance test for it

Scenario: The First User Sends Request
Given the first user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Peer Registration
And the user should wait a few seconds
And the user clicks on the button: Сreate peer
Then the user should create a peer request with: '31.29.5.194', 'test'
And the user should observe button: Cancel

Scenario: The Second User Approve Request
Given the second user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Peer Registration
Then the user should approve the peer with: 'test'

Scenario: Grow Cross Peer Environment on Remove
Given the first user is on the Home page of Subutai
When the user should wait a few seconds
And the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
When the user clicks on the Environment's mode: Advanced

Scenario: Destroy Cross Peer Environment and Blueprint
Given To Do!
When To Do!
Then To Do!