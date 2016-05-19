Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: the user observe own pgp key
Given the first user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: User Identity
And the user clicks on the menu item: Tokens
Then the user gets token
And the user observes Local Peer ID
When the user gets Peer ID
Then the user observes Own PGP key
