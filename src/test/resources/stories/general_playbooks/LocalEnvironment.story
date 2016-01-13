Meta:

Narrative:
As a QA
I want to verify a functional: Local Environment
So that I wrote acceptance test for it

Scenario: Create Local Environment
!--Given the first user is on the Home page of Subutai
!--And the user enters login and password: 'admin', 'secret'
!--And the user clicks on the button: Login
!--When the user clicks on the menu item: Environment
!--Then the user observes items of Environment menu
!--When the user clicks on the menu item: Blueprint
!--And the user clicks on the button: Create Blueprint
!--Then the user observes field: Enter blueprint name
!--When the user enters blueprint name: 'Sample blueprint'
!--And the user enters node name: 'Node'
!--And the user selects template: 'master'
!--And the user enters number of containers: '2'
!--And the user enters SSH group ID: '0'
!--And the user enters host Group ID: '0'
!--And the user selects quota size: 'TINY'
!--And the user clicks on the button: Add to node list
!--And the user clicks on the button: Create
!--Then the user observes created blueprint
!--When the user clicks on the icon: Build
!--Then the user observes build environment
!--When the user enters environment name: 'Local Environment'
!--And the user selects peer: One
!--And the user selects Strategie: 'DEFAULT-STRATEGY'
!--And the user clicks on the button: Place
!--Then the user observes icon: two containers
!--When the user clicks on the link: Environment Build List
!--Then the user observes popup: Build Environment
!--When the user clicks on the button: Build
!--Then the user observes header: Success!
!--And the user observes text: Your environment start creation.
!--When the user clicks on the button: OK
!--Then the user observes header: Success!
!--And the user observes text: Your environment has been created.

Scenario: Grow Local Environment
!--Given the first user is on the Home page of Subutai
!--When the user clicks on the menu item: Environment
!--And the user clicks on the menu item: Blueprint
!--And the user clicks on the icon: Grow
!--Then the user observes selector: Environment
!--When the user selects environment: Local Environment
!--And the user selects peer: One
!--And the user selects Strategie: 'DEFAULT-STRATEGY'
!--And the user clicks on the button: Place
!--Then the user observes icon: two containers
!--When the user clicks on the link: Environment Build List
!--Then the user observes popup: Build Environment
!--When the user clicks on the button: Build
!--Then the user observes header: Success!
!--And the user observes text: Your environment start growing.
!--When the user clicks on the button: OK
!--Then the user observes header: Success!
!--And the user observes text: You successfully grow environment.

Scenario: Destroy Environment and Blueprint
!--Given the first user is on the Home page of Subutai
!--When the user clicks on the menu item: Environment
!--And the user clicks on the menu item: Blueprint
!--And the user clicks on the icon: Remove
!--Then the user observes popup: Are you sure?
!--When the user clicks on the button: Delete
!--Then the user observes header: Deleted
!--When the user clicks on the menu item: Environments
!--And the user clicks on the icon: Destroy
!--Then the user observes popup: Are you sure?
!--When the user clicks on the button: Delete
!--Then the user observes text: Your environment is being deleted!
!--When the user clicks on the button: OK
!--Then the user observes text: Your environment has been destroyed.
!--And the user observes text: No data available in table