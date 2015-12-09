Meta:

Narrative:
As a QA
I want to verify the PLAYBOOK SS-3038
So that I will create test scenarios

Scenario: Create Environment on the ARM
Given the ARM user is on the home page of Subutai
And the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Console
And the user selects any available resource host from select menu
And the user enters console command: 'cat /proc/cpuinfo'
Then the user verifies output console command and observe expected phrase: 'ARM'
When the user clicks on the menu item: Environment
Then the user observes items of Environment menu
When the user clicks on the menu item: Blueprint
And the user clicks on the button: Create Blueprint
Then the user observes field: Enter blueprint name
When the user enters blueprint name: 'Sample blueprint'
And the user enters node name: 'Node'
And the user selects template: 'master'
And the user enters number of containers: '2'
And the user enters SSH group ID: '0'
And the user enters host Group ID: '0'
And the user selects quota size: 'TINY'
And the user clicks on the button: Add to node list
And the user clicks on the button: Create
Then the user observes created blueprint
When the user clicks on the icon: Build
Then the user observes build environment
When the user enters environment name: 'Local Environment'
And the user selects peer: One
And the user selects Strategie: 'DEFAULT-STRATEGY'
And the user clicks on the button: Place
Then the user observes icon: two containers
When the user clicks on the link: Environment Build List
Then the user observes popup: Build Environment
When the user clicks on the button: Build
Then the user observes header: Success!
And the user observes text: Your environment start creation.
When the user clicks on the button: OK
Then the user observes header: Success!
And the user observes text: Your environment has been created.
When the user clicks on the button: OK
And the user clicks on the menu item: Console
And the user selects any available resource host from select menu
And the user enters console command: 'sudo subutai list -i'
Then the user verifies output console command and observe expected phrase: 'master'
And the user verifies output console command and observe expected phrase: 'RUNNING'
When the user selects management host from select menu
And the user enters console command: 'cat /proc/cpuinfo'
Then the user verifies output console command and observe expected phrase: 'Intel'

Scenario: Destroy Environment and Blueprint from ARM
Given the ARM user is on the home page of Subutai
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Blueprint
And the user clicks on the icon: Remove
Then the user observes popup: Are you sure?
When the user clicks on the button: Delete
Then the user observes header: Deleted
When the user clicks on the menu item: Environments
And the user clicks on the icon: Destroy
Then the user observes popup: Are you sure?
When the user clicks on the button: Delete
Then the user observes text: Your environment is being deleted!
When the user clicks on the button: OK
Then the user observes text: Your environment has been destroyed.
And the user observes text: No data available in table
