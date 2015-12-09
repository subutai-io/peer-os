Meta:

Narrative:
As a QA
I want to verify an Ubuntu on Snappy PLAYBOOK SS-3285
So that I will create test scenarios

Scenario: The user verify work Subutai: Ubuntu on Snappy
Given the first user is on the home page of Subutai
And the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Console
And the user selects management host from select menu
And the user enters console command: 'snappy info'
Then the user verifies output console command and observe expected phrase: 'frameworks: subutai-mng.sideload'
When the user enters console command: 'clear'
And the user selects any available resource host from select menu
And the user enters console command: 'snappy info'
Then the user verifies output console command and observe expected phrase: 'frameworks: subutai.sideload'
When the user enters console command: 'ls /var/lib/apps/subutai/current/var/lib/lxc/lxc-data/tmpdir/'
And the user enters console command: subutai import hadoop
And the user enters console command: 'clear'
And the user enters console command: 'ls /var/lib/apps/subutai/current/var/lib/lxc/lxc-data/tmpdir/'
Then the user verifies output console command and observe expected phrase: 'hadoop-subutai-template_'
And the user verifies output console command and observe expected phrase: 'openjre7-subutai-template_'
When the user clicks on the menu item: Peer Registration
Then the user observes button: Create Peer
When the user clicks on the link: Create Peer
Then the user observes field: Enter IP
And the user observes field: Key phrase
When the user enters peer ip: Second user
And the user enters peer key phrase: 'secret'
When the user clicks on the button: Create for peer
Then the user observes: Second user's IP
And the user observes button: Cancel

Scenario: The Second User Approve Request
Given the second user is on the home page of Subutai
And the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Peer Registration
Then the user observes: First user's IP
And the user observes button: Approve
And the user observes button: Reject
When the user clicks on the button: Approve
Then the user observes field: Approve Key phrase
When the user enters approve key phrase: 'secret'
And the user clicks on the button popup: Approve
Then the user observes button: Unregister
When the user clicks on the menu item: Environment
Then the user observes items of Environment menu
When the user clicks on the menu item: Blueprint
And the user clicks on the button: Create Blueprint
Then the user observes field: Enter blueprint name
When the user enters blueprint name: 'Sample blueprint'
And the user enters node name: 'Node'
And the user selects template: 'hadoop'
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
And the user selects peer: Two
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

Scenario: Destroy Cross Peer Environment and Blueprint
Given the second user is on the home page of Subutai
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
When the user clicks on the button: OK
And the user clicks on the menu item: Peer Registration
Then the user observes button: Unregister
When the user clicks on the button: Unregister
Then the user observes popup: Are you sure?
When the user clicks on the button: Confirm Unregister
Then the user observes header: Unregistered!
When the user clicks on the button: OK
Then the user observes text: No data available in table
