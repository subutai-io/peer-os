Meta:

Narrative:
As a user
I want to create an envi with medium size
So that I can run this test

Scenario: creates environment 'Cassandra' with Medium size container
Given the first user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
And the user should find template: Cassandra
And the user clicks on temaplate: Cassandra
And the user clicks on temaplate: Cassandra
Then the user chooses the medium size of first template cassandra
Then the user chooses the medium size of second template cassandra
Then the user should create an environment

Scenario: the user should remove the environment
Then the user destroys created environment