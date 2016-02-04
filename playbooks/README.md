# Subutai-playbooks

Acceptance tests:
Use script run_test.sh for run the acceptance tests.

Parameters:
  -m          Management Host First:  IP
  -M          Management Host Second: IP
  -l              Observe List of All Playbooks
  -L              Observe List Playbooks for run
  -s              Choice of Playbooks for run
     -s all       Start all Playbooks
     -s "playbook1.story playbook2.* ... " Start a few Playbooks
  -r              Start acceptance tests
  -h              Help
  
  Example: ./run_tests.sh -s SmokeTest.sh -m 192.168.0.12 -r
