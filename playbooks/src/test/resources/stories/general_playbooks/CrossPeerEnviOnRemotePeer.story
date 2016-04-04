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

And the user clicks on the button: Сreate peer
Then the user should create a peer request with: '158.181.133.65', 'test'
And the user should observe button: Cancel

Scenario: The Second User Approve Request
Given the second user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Peer Registration
Then the user should approve the peer with: 'test'

Scenario: Grow Cross Peer Environment on Remove Peer and destroy it
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
And the user should find template: Master
And the user creates environment using template: Master
Then the user destroys created environment

Scenario: the user should unregister Peer
When the user clicks on the menu item: Peer Registration
Then the user unregister peer