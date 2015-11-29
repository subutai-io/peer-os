Meta:

Narrative:
As a QA
I want to verify an Unprivileged containers PLAYBOOK SS-3285
So that I will create test scenarios


Scenario: The user verify work Subutai: Ubuntu on Snappy
Given the first user is on the home page of Subutai
And the user enter login and password: 'admin', 'secret'
And the user click on the button: Login
When the user click on the menu item: Console
And the user select management host from select menu
And the user enter console command: 'snappy info'
Then the user verify output console command and observe expected phrase: 'frameworks: subutai-mng.sideload'
When the user enter console command: 'clear'
And the user select any available resource host from select menu
And the user enter console command: 'snappy info'
Then the user verify output console command and observe expected phrase: 'frameworks: subutai.sideload'
When the user enter console command: 'ls /var/lib/apps/subutai/current/var/lib/lxc/lxc-data/tmpdir/'
Then the user verify output console command and observe expected phrase: 'master-subutai-template_2.1.1_amd64.tar.gz'
When the user enter console command: subutai import hadoop
Then the user verify output console command and observe expected phrase: 'hadoop-subutai-template_2.1.2_amd64.tar.gz'
And the user verify output console command and observe expected phrase: 'openjre7-subutai-template_2.1.1_amd64.tar.gz'
When the user click on the menu item: Peer Registration
Then the user observe button: Create Peer
When the user click on the link: Create Peer
Then the user observe field: Enter IP
And the user observe field: Key phrase
When the user enter peer ip: Second user
And the user enter peer key phrase: 'secret'
When the user click on the button: Create for peer
Then the user observe: Second user's IP
And the user observe button: Cancel

Scenario: The Second User Approve Request
Given the second user is on the home page of Subutai
And the user enter login and password: 'admin', 'secret'
And the user click on the button: Login
When the user click on the menu item: Peer Registration
Then the user observe: First user's IP
And the user observe button: Approve
And the user observe button: Reject
When the user click on the button: Approve
Then the user observe field: Approve Key phrase
When the user enter approve key phrase: 'secret'
And the user click on the button popup: Approve
Then the user observe button: Unregister
When the user click on the menu item: Environment
Then the user observe items of Environment menu
When the user click on the menu item: Blueprint
And the user click on the button: Create Blueprint
Then the user observe field: Enter blueprint name
When the user enter blueprint name: 'Sample blueprint'
And the user enter node name: 'Node'
And the user select template: 'hadoop'
And the user enter number of containers: '2'
And the user enter SSH group ID: '0'
And the user enter host Group ID: '0'
And the user select quota size: 'TINY'
And the user click on the button: Add to node list
And the user click on the button: Create
Then the user observe created blueprint
When the user click on the icon: Build
Then the user observe build environment
When the user enter environment name: 'Local Environment'
And the user select peer: Two
And the user select Strategie: 'DEFAULT-STRATEGY'
And the user click on the button: Place
Then the user observe icon: two containers
When the user click on the link: Environment Build List
Then the user observe popup: Build Environment
When the user click on the button: Build
Then the user observe header: Success!
And the user observe text: Your environment start creation.
When the user click on the button: OK
Then the user observe header: Success!
And the user observe text: Your environment has been created.

Scenario: Destroy Cross Peer Environment and Blueprint
Given the second user is on the home page of Subutai
When the user click on the menu item: Environment
And the user click on the menu item: Blueprint
And the user click on the icon: Remove
Then the user observe popup: Are you sure?
When the user click on the button: Delete
Then the user observe header: Deleted
When the user click on the menu item: Environments
And the user click on the icon: Destroy
Then the user observe popup: Are you sure?
When the user click on the button: Delete
Then the user observe text: Your environment start deleting!
When the user click on the button: OK
Then the user observe text: Your environment has been destroyed.
When the user click on the button: OK
And the user click on the menu item: Peer Registration
Then the user observe button: Unregister
When the user click on the button: Unregister
Then the user observe popup: Are you sure?
When the user click on the button: Confirm Unregister
Then the user observe header: Unregistered!
When the user click on the button: OK
Then the user observe text: No data available in table
