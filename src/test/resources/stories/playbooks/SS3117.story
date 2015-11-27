Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: Create Local Environment
!--Given the first user is on the home page of Subutai
!--And the user enter login and password: 'admin', 'secret'
!--And the user click on the button: Login
!--When the user click on the menu item: Environment
!--Then the user observe items of Environment menu
!--When the user click on the menu item: Blueprint
!--Then the user observe button: Create Blueprint
!--When the user click on the button: Create Blueprint
!--Then the user observe field: Enter blueprint name
!--When the user enter blueprint name: 'Webdemo'
!--And the user enter node name: 'Node'
!--And the user select template: 'webdemo'
!--And the user enter number of containers: '3'
!--And the user enter SSH group ID: '0'
!--And the user enter host Group ID: '0'
!--And the user select quota size: 'TINY'
!--And the user click on the button: Add to node list
!--And the user click on the button: Create
!--Then the user observe created blueprint
!--When the user click on the icon: Build
!--Then the user observe build environment
!--When the user enter environment name: 'Local Environment'
!--And the user select peer: One
!--And the user select Strategie: 'ROUND-ROBIN'
!--And the user click on the button: Place
!--Then the user observe icon: two containers
!--When the user click on the link: Environment Build List
!--Then the user observe popup: Build Environment
!--When the user click on the button: Build
!--Then the user observe header: Success!
!--And the user observe text: Your environment start creation.
!--When the user click on the button: OK
!--Then the user observe header: Success!
!--And the user observe text: Your environment has been created.

Scenario: Add domain balance polici and SSL sertificate
!--Given the first user is on the home page of Subutai
!--And the user enter login and password: '$login', '$password'
!--And the user click on the button: Login
!--When the user click on the button Environment
!--And the user click on the tab: Environments
!--And the user click on Domain button of test environment
!--Then the user should observe empty Environment domain
!--When the user insert domain 'subut.ai' in input field
!--And the user choose Round-Robin balance policy
!--And the user add PEM certificate from file
!--And the user press Upload button
!--And the user press Assign button
!--And the user press Yes in confirmation window
!--Then the user observe domain name assigned to environment
!--And the user observe balance policy set to round-robin

Scenario: Conect container to domain first
!--Given the first user is on the home page of Subutai
!--And the user enter login and password: '$login', '$password'
!--And the user click on the button: Login
!--When the user click on the button Environment
!--And the user click on the tab: Environments
!--And the user click on the button: Containers
!--And the user click on Domain button in first container line
!--And the user click on ceckbox
!--Then the user check Is container in domain check-box

Scenario: Conect container to domain Second
!--Given the first user is on the home page of Subutai
!--And the user enter login and password: '$login', '$password'
!--And the user click on the button: Login
!--When the user click on the button Environment
!--And the user click on the tab: Environments
!--And the user click on the button: Containers
!--And the user click on Domain button in second container line
!--And the user click on ceckbox
!--Then the user check Is container in domain check-box

Scenario: Conect container to domain Third
!--Given the first user is on the home page of Subutai
!--And the user enter login and password: '$login', '$password'
!--And the user click on the button: Login
!--When the user click on the button Environment
!--And the user click on the tab: Environments
!--And the user click on the button: Containers
!--And the user click on Domain button in third container line
!--And the user click on ceckbox
!--Then the user check Is container in domain check-box

Scenario: Obserwe domain's page with container's IP
!--Given the user is on subut.ai page

!--Then the user observe page with IP that received request appear

Scenario: Compare ip
!--Given the user is on subut.ai page
!--When the user compare IP
!--Then the user should see diferents IP

Scenario: container stopped
!--Given the first user is on the home page of Subutai
!--And the user enter login and password: '$login', '$password'
!--And the user click on the button: Login
!--When the user click on the button Environment
!--And the user click on the tab: Environments
!--And the user click on the button: Containers
!--And the user stop some container by pressing Stop button
!--And the user get ip of stopped container
!--And the user click on window close button
!--And the user click on the button: modules
!--And the user click on the button: Peer
!--And the user click on the button: info
!--Then the user should see that container stopped

Scenario: should not observe stopped container
!--Given the user is on subut.ai page
!--When the user press F5 several times
!--Then the user should not observe stopped container IP

Scenario: check checkbox is container in domain
!--Given the first user is on the home page of Subutai
!--And the user enter login and password: '$login', '$password'
!--And the user click on the button: Login
!--When the user click on the button Environment
!--And the user click on the tab: Environments
!--And the user click on the button: Containers
!--And the user click on Domain button in first container line
!--Then the user should window with checked Is container in domain appear

Scenario: container removed from domain
!--Given the first user is on the home page of Subutai
!--And the user enter login and password: '$login', '$password'
!--And the user click on the button: Login
!--When the user click on the button Environment
!--And the user click on the tab: Environments
!--And the user click on the button: Containers
!--And the user get ip of disabled container
!--And the user click on Domain button in second container line
!--And the user uncheck Is container in domain
!--Then the user observe container removed from subut.ai domain

Scenario: disabled container IP address won't appear
!--Given the user is on subut.ai page
!--When the user press F5 several times
!--Then the user should not observe disabled container IP

Scenario: domain unassigned from environment
!--Given the first user is on the home page of Subutai
!--And the user enter login and password: '$login', '$password'
!--And the user click on the button: Login
!--When the user click on the button Environment
!--And the user click on the tab: Environments
!--And the user click on Domain button of test environment
!--And the user insert empty domain in input field
!--And the user press Assign button
!--And the user press Yes in confirmation window
!--Then the user observe domain name empty to environment

Scenario: 404 - page not found message appear
!--Given the user is on subut.ai page
!--When the user press F5 several times
!--Then the user should observe web page with container IP won't be available

Scenario: Destroy Local Environment
!--Given the first user is on the home page of Subutai
!--And the user enter login and password: '$login', '$password'
!--And the user click on the button: Login
!--When the user click on the button Environment
!--And the user observe tab: Blueprints
!--And the user click on the button: Delete
!--And the user click on the tab: Environments
!--And the user observe Environment: Sample blueprint
!--And the user click on the button: Destroy
!--And the user observe popup: Do you really want to destroy this environment?
!--And the user click on the button: Yes
!--Then the user observe indicator: Progress
!--And the user don't observe environment: Sample blueprint