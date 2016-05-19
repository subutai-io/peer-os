Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: the user should create an Environment
Given the first user is on the Home page of Subutai
And the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Bazaar
Then the user search plugin: 'AppScale'
When the user clicks on Launch button
When the user clicks on the buton: Quick install
Then the user fills out Quick Install
When the user clicks on the title manage
Then the user should observe button: Console

Scenario: may be will use
!When the user enters profile Domain name

Then the user clicks on the buton: Console
Then the user clicks button: Quick install
When the user should install plugin
