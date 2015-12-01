Meta:

Narrative:
As a QA
I want to verify the PLAYBOOK SS-3117
So that I will create test scenarios

Scenario: Create Local Environment
Given the first user is on the home page of Subutai
And the user enter login and password: 'admin', 'secret'
And the user click on the button: Login
When the user click on the menu item: Environment
Then the user observe items of Environment menu
When the user click on the menu item: Blueprint
And the user click on the button: Create Blueprint
Then the user observe field: Enter blueprint name
When the user enter blueprint name: 'Sample blueprint'
And the user enter node name: 'Node'
And the user select template: 'webdemo'
And the user enter number of containers: '3'
And the user enter SSH group ID: '0'
And the user enter host Group ID: '0'
And the user select quota size: 'TINY'
And the user click on the button: Add to node list
And the user click on the button: Create
Then the user observe created blueprint
When the user click on the icon: Build
Then the user observe build environment
When the user enter environment name: 'Local Environment'
And the user select peer: One
And the user select Strategie: 'ROUND_ROBIN'
And the user click on the button: Place
Then the user observe icon: three containers
When the user click on the link: Environment Build List
Then the user observe popup: Build Environment
When the user click on the button: Build
Then the user observe header: Success!
And the user observe text: Your environment start creation.
When the user click on the button: OK
Then the user observe header: Success!
And the user observe text: Your environment has been created.

Scenario: Add domain balance polici and SSL sertificate
Given the first user is on the home page of Subutai
When the user click on the menu item: Environment
And the user click on the menu item: Environments
And the user click on the button: configure
Then the user observe empty Environment domain
When the user insert domain 'subut.ai' in input field
And the user select domain strategy 'ROUND_ROBIN'
And the user add PEM certificate from file
And the user press on the button: save
Then the user should observe success message
When the user click on the button: OK
Then the user observe domain name 'subut.ai' assigned to environment
And the user observe domain strtegy set to round-robin

Scenario: Conect container to domain first
Given the first user is on the home page of Subutai
When the user click on the menu item: Environment
And the user click on the menu item: Containers
And the user click on the first container button: configure
And the user click on ceckbox
And the user click on the button: save
And the user click on the first container button: configure
Then the user check Is container in domain check-box

Scenario: Conect container to domain Second
Given the first user is on the home page of Subutai
When the user click on the menu item: Environment
And the user click on the menu item: Containers
And the user click on the second container button: configure
And the user click on ceckbox
And the user click on the button: save
And the user click on the second container button: configure
Then the user check Is container in domain check-box

Scenario: Conect container to domain Third
Given the first user is on the home page of Subutai
When the user click on the menu item: Environment
And the user click on the menu item: Containers
And the user click on the third container button: configure
And the user click on ceckbox
And the user click on the button: save
And the user click on the third container button: configure
Then the user check Is container in domain check-box

Scenario: Obserwe domain's page with container's IP
Given the user is on subut.ai page

Then the user observe page with IP that received request appear

Scenario: Compare ip
Given the user is on subut.ai page
When the user compare IP
Then the user should see diferents IP

Scenario: container stopped
Given the first user is on the home page of Subutai
When the user click on the menu item: Environment
And the user click on the menu item: Containers
And the user stop some container by pressing Stop button
And the user get ip of stopped container
Then the user should see that container stopped

Scenario: should not observe stopped container
Given the user is on subut.ai page
When the user press F5 several times
Then the user should not observe stopped container IP

Scenario: check checkbox is container in domain
Given the first user is on the home page of Subutai
When the user click on the menu item: Environment
And the user click on the menu item: Containers
And the user click on the first container button: configure
Then the user check Is container in domain check-box

Scenario: container removed from domain
Given the first user is on the home page of Subutai
When the user click on the menu item: Environment
And the user click on the menu item: Containers
And the user click on the second container button: configure
And the user click on ceckbox
And the user click on the button: save
And the user get ip of disabled container
And the user click on the second container button: configure
Then the user observe container removed from subut.ai domain

Scenario: disabled container IP address won't appear
Given the user is on subut.ai page
When the user press F5 several times
Then the user should not observe disabled container IP

Scenario: domain unassigned from environment
Given the first user is on the home page of Subutai
When the user click on the menu item: Environment
And the user click on the menu item: Environments
And the user click on the button: configure
And the user click on the button: remove domain
And the user click on the button: Delete
And the user click on the button: OK
And the user click on the button: configure
Then the user observe domain name empty to environment

Scenario: 404 - page not found message appear
Given the user is on subut.ai page
When the user press F5 several times
Then the user should observe web page with container IP won't be available

Scenario: Destroy Environment and Blueprint
Given the first user is on the home page of Subutai
When the user click on the menu item: Environment
And the user click on the menu item: Blueprint
And the user click on the icon: Remove
Then the user observe popup: Are you sure?
When the user click on the button: Delete
And the user click on the menu item: Environments
And the user click on the icon: Destroy
Then the user observe popup: Are you sure?
When the user click on the button: Delete
Then the user observe text: Your environment start deleting!
When the user click on the button: OK
Then the user observe text: Your environment has been destroyed.
And the user observe text: No data available in table
