Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: creates environment 'Cassandra' with 3 containers
Given the first user is on the Home page of Subutai
Then the user should observe web elements on: Login page
Given the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
And the user should find template: Cassandra
And the user clicks on temaplate: Cassandra
And the user clicks on temaplate: Cassandra
And the user clicks on temaplate: Cassandra
Then the user chooses the medium size of first template cassandra
Then the user chooses the medium size of second template cassandra
Then the user chooses the medium size of third template cassandra
Then the user should create an environment

Scenario: the user should install and work with Cassandra plugin
When the user clicks on the menu item: Bazaar
Then the user search plugin: 'Cassandra'
When the user should install plugin
When the user clicks on the menu item: Bazaar
Then the user search plugin: 'Generic'
When the user should install plugin
When the user clicks on the menu item: Bazaar
Then the user search plugin: 'Cassandra'
When the user clicks on Launch button

Scenario:
When the user input the cluster name: 'CasandraTest'
