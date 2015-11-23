### Needs packages: maven, java

### Commands for work with Serenity Test Framework:

All commands will need to run from playbooks directory!

All reports are stored in directory: playbooks/target

If you want see reports please open file target/site/serenity/index.html in any browser!

## Run tests using script:

    ./run_tests.sh -r
    
## Helper for bash script:
    
    ./run_tests.sh -h
    
    ======================================================================
    Script for run the acceptance tests!
    
    Parameters:
      -m          Management Host First:  IP
      -M          Management Host Second: IP
      -l          Observe List Playbooks
      -L          Observe List Playbooks for run
      -s          Choice of Playbooks for run.
        -s all    Start all Playbooks
        -s "playbook1 playbook2 ... " Start a few Playbooks
      -r          Start acceptance tests
      -h          Help
    ======================================================================


