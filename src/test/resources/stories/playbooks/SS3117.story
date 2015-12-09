Meta:

Narrative:
As a QA
I want to verify the PLAYBOOK SS-3117
So that I will create test scenarios

Scenario: Create Local Environment
Given the first user is on the home page of Subutai
And the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the menu item: Environment
Then the user observes items of Environment menu
When the user clicks on the menu item: Blueprint
And the user clicks on the button: Create Blueprint
Then the user observes field: Enter blueprint name
When the user enters blueprint name: 'Sample blueprint'
And the user enters node name: 'Node'
And the user selects template: 'webdemo'
And the user enters number of containers: '3'
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
And the user selects Strategie: 'ROUND_ROBIN'
And the user clicks on the button: Place
Then the user observes icon: three containers
When the user clicks on the link: Environment Build List
Then the user observes popup: Build Environment
When the user clicks on the button: Build
Then the user observes header: Success!
And the user observes text: Your environment start creation.
When the user clicks on the button: OK
Then the user observes header: Success!
And the user observes text: Your environment has been created.

Scenario: Add domain balance polici and SSL sertificate
Given the first user is on the home page of Subutai
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
And the user clicks on the button: configure
Then the user observes empty Environment domain
When the user inserts domain 'subut.ai' in input field
And the user selects domain strategy 'ROUND_ROBIN'
And the user adds PEM certificate from file
And the user presses on the button: save
Then the user should observe success message
When the user clicks on the button: OK
Then the user observes domain name 'subut.ai' assigned to environment
And the user observes domain strtegy set to round-robin

Scenario: Conect container to domain first
Given the first user is on the home page of Subutai
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Containers
And the user clicks on the first container button: configure
And the user clicks on ceckbox
And the user clicks on the button: save
And the user clicks on the first container button: configure
Then the user checks Is container in domain check-box

Scenario: Conect container to domain Second
Given the first user is on the home page of Subutai
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Containers
And the user clicks on the second container button: configure
And the user clicks on ceckbox
And the user clicks on the button: save
And the user clicks on the second container button: configure
Then the user checks Is container in domain check-box

Scenario: Conect container to domain Third
Given the first user is on the home page of Subutai
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Containers
And the user clicks on the third container button: configure
And the user clicks on ceckbox
And the user clicks on the button: save
And the user clicks on the third container button: configure
Then the user checks Is container in domain check-box

Scenario: Obserwe domain's page with container's IP
Given the user is on subut.ai page
Then the user observes page with IP that received request appear

Scenario: Compare ip
Given the user is on subut.ai page
When the user compares IP
Then the user should see diferents IP

Scenario: container stopped
Given the first user is on the home page of Subutai
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Containers
And the user stops some container by pressing Stop button
And the user gets ip of stopped container
Then the user should see that container stopped

Scenario: should not observe stopped container
Given the user is on subut.ai page
When the user presses F5 several times
Then the user should not observe stopped container IP

Scenario: check checkbox is container in domain
Given the first user is on the home page of Subutai
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Containers
And the user clicks on the first container button: configure
Then the user checks Is container in domain check-box

Scenario: container removed from domain
Given the first user is on the home page of Subutai
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Containers
And the user clicks on the second container button: configure
And the user clicks on ceckbox
And the user clicks on the button: save
And the user gets ip of disabled container
And the user clicks on the second container button: configure
Then the user observes container removed from subut.ai domain

Scenario: disabled container IP address won't appear
Given the user is on subut.ai page
When the user presses F5 several times
Then the user should not observe disabled container IP

Scenario: domain unassigned from environment
Given the first user is on the home page of Subutai
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Environments
And the user clicks on the button: configure
And the user clicks on the button: remove domain
And the user clicks on the button: Delete
And the user clicks on the button: OK
And the user clicks on the button: configure
Then the user observes domain name empty to environment

Scenario: 404 - page not found message appear
Given the user is on subut.ai page
When the user presses F5 several times
Then the user should observe web page with container IP won't be available

Scenario: Destroy Environment and Blueprint
Given the first user is on the home page of Subutai
When the user clicks on the menu item: Environment
And the user clicks on the menu item: Blueprint
And the user clicks on the icon: Remove
Then the user observes popup: Are you sure?
When the user clicks on the button: Delete
And the user clicks on the menu item: Environments
And the user clicks on the icon: Destroy
Then the user observes popup: Are you sure?
When the user clicks on the button: Delete
Then the user observes text: Your environment is being deleted!
When the user clicks on the button: OK
Then the user observes text: Your environment has been destroyed.
And the user observes text: No data available in table
