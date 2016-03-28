Meta:

Narrative:
As a user of SS
I want to register peer on the HUB
So that I wrote this playbook

Scenario: Peer Registration on the HUB
Given the first user is on the Home page of Subutai
And the user enters login and password: 'admin', 'secret'
And the user clicks on the button: Login
When the user clicks on the upper menu item: Register Peer
And the user enters login for Peer Registration on the Hub: 'oleg2007_1988@mail.ru'
And the user enters password for Peer Registration on the Hub: 't19a@9v!6s'
And the user clicks on the button: Register
Then the user should observe button: Go To HUB Green
When user user clicks on the button: Close
And the user clicks on the button: Peer Registration Online
Then the user should observe button: Send Heartbeat
When the user clicks on the button: Send Heartbeat
And the user clicks on the button: OK
And the user clicks on the button: Peer Registration Online
And the user click on the buton: Go to HUB White

