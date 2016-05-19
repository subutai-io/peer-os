Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: Create Local Environment
Given the first user is on the Home page of Subutai
And the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
And the user should find template: Master
And the user creates environment using template: Master

Scenario: the user observe own pgp key
When the user clicks on the menu item: User Identity
And the user clicks on the menu item: Tokens
Then the user gets token
And the user observes Local Peer ID
When the user gets Peer ID
Then the user observes Own PGP key

Scenario: The First User Sends Request
Given the first user is on the Home page of Subutai
When the user clicks on the menu item: User Identity
And the user clicks on the menu item: Tokens
Then the user gets token
And the user observes Environment data
When the user gets Environment ID
When the user should wait a few seconds
Then the user observes Environment PGP key
When the user should wait a few seconds

Scenario: the user should destroy an environment
Given the first user is on the Home page of Subutai
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
Then the user destroys created environment