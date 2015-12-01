#!/usr/bin/env bash

function print_help() {
    echo "======================================================================"
    echo "Script for run the acceptance tests!"
    echo
    echo "Parameters:"
    echo "  -m          Management Host First:  IP"
    echo "  -M          Management Host Second: IP"
    echo "  -l              Observe List of All Playbooks"
    echo "  -L              Observe List Playbooks for run"
    echo "  -s              Choice of Playbooks for run"
    echo "     -s all       Start all Playbooks"
    echo "     -s \"playbook1.story playbook2.* ... \" Start a few Playbooks"
    echo "  -r              Start acceptance tests"
    echo "  -h              Help"
    echo "======================================================================"
}

function choice_mngh1(){
    mh1=$OPTARG;
    ./node-approve $mh1;
    echo "$mh1" > src/test/resources/parameters/mng_h1
}

function choice_mngh2(){
    mh2=$OPTARG;
    ./node-approve $mh2;
    echo "$mh2" > src/test/resources/parameters/mng_h2
}

function list_stories(){
    echo "======================================================================"
    echo "LIST of the ALL PLAYBOOKS: "
    echo
    cd src/test/resources/stories/general_playbooks/
    find * -type f
    cd ..
    cd playbooks
    find * -type f
    echo "======================================================================"
}

function list_playbooks(){
    echo "======================================================================"
    echo "LIST of the RUN PLAYBOOKS: "
    echo
    DIR="src/test/resources/stories/tests_run/"
     if [[ -d "$DIR" && "$(ls -A $DIR)" ]]; then
        cd "$DIR"
        find * -type f
     else
        echo "  PLAYBOOKS for RUN - NOT FOUND!"
        echo "          ... ... ... "
        echo " Use key -s Example: ./run_tests.sh -s name_playbook.story"
    fi
    echo "======================================================================"
}

function choice_stories(){
    mvn clean;
    ns_path=$OPTARG;
    arr=($ns_path);
    DIR="src/test/resources/stories/tests_run"
    if [[ -d "$DIR" && "$(ls -A $DIR)" ]]; then
        rm -r ${DIR}/*
    fi
    if [[ $ns_path == "all" ]]; then
      cp -r src/test/resources/stories/general_playbooks src/test/resources/stories/tests_run
      cp -r src/test/resources/stories/playbooks src/test/resources/stories/tests_run

      echo "$ns_path" > src/test/resources/parameters/ns_path
      cat src/test/java/od/jbehave/AcceptanceTestSuite.java | while read i;
      do echo ${i//directory_stories=*/directory_stories=\"stories/tests_run/*/*\"\;};
      done > newfile;
      mv newfile src/test/java/od/jbehave/AcceptanceTestSuite.java;
      cd src/test/resources/stories/tests_run
      echo
      echo "PLAYBOOKS FOR RUN: "
      find * -type f
      echo

    else
      for ((i=0;i<"${#arr[@]}";i++))
     do
     s="`find -name ${arr[i]} -type f`"
        cp -fr "$s" src/test/resources/stories/tests_run
        echo "$s"
        mv src/test/resources/stories/tests_run/${arr[i]} src/test/resources/stories/tests_run/"Playbook${arr[i]}"
     done

      echo "$ns_path" > src/test/resources/parameters/ns_path
      cat src/test/java/od/jbehave/AcceptanceTestSuite.java | while read i;
      do echo ${i//directory_stories=*/directory_stories=\"stories/tests_run/*\"\;};
      done > newfile;
      mv newfile src/test/java/od/jbehave/AcceptanceTestSuite.java;
      cd src/test/resources/stories/tests_run
      echo
      echo "PLAYBOOKS FOR RUN: "
      find * -type f
      echo
    fi
}

function run_tests(){
    mvn clean; mvn integration-test; mvn serenity:aggregate;
}

if [ $# = 0 ]; then
    print_help
fi

while getopts "m:M:s:rLlh" opt;
do
    case $opt in
        m) choice_mngh1;
            ;;
        M) choice_mngh2;
            ;;
        l) list_stories;
            ;;
        L) list_playbooks;
            ;;
        s) choice_stories;
            ;;
        r) run_tests;
            ;;
        h) echo "Print Help Page"
           print_help;
            ;;
        esac
done
shift $(($OPTIND - 1))

