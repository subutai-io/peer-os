/**
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    @copyright 2014 Safehaus.org
 */
/**
 *  @brief     SubutaiContainer.cpp
 *  @class     SubutaiContainer.cpp
 *  @details   SubutaiContainer Class is designed for getting and setting container variables and special informations.
 *  		   This class's instance can get get useful container specific informations
 *  		   such as IPs, UUID, hostname, macID, parentHostname, etc..
 *  @author    Mikhail Savochkin
 *  @author    Ozlem Ceren Sahin
 *  @version   1.1.0
 *  @date      Oct 31, 2014
 */
#include "SubutaiContainer.h"

using namespace std;
/**
 *  \details   Default constructor of SubutaiContainer class.
 */
SubutaiContainer::SubutaiContainer(SubutaiLogger* logger, lxc_container* cont) : _arch("")
{
    this->container = cont;
    this->containerLogger = logger;
    _logEntry = "<SubutaiContainer>";
}

/**
 *  \details   Default destructor of SubutaiContainer class.
 */
SubutaiContainer::~SubutaiContainer()
{
    // TODO Auto-generated destructor stub
}

/**
 *  \details   Clear id, mac address and ip adresses.
 */
void SubutaiContainer::clear()
{
    id = "";
    interfaces.clear();
}

/**
 * Run program given as parameter 'program' with arguments 'params'
 * Return stdout if success or stderr if fails
 */
string SubutaiContainer::RunProgram(string program, vector<string> params) 
{
    ExecutionResult result = RunProgram(program, params, true, LXC_ATTACH_OPTIONS_DEFAULT);
    if (result.exit_code == 0) {
        return result.out;
    } else {
        return result.err;
    }
}

/**
 * Run program given as parameter 'program' with arguments 'params' using lxc attach options 'opts'
 * Returns ExecutionResult object including exit_code and stdout if success or stderr if fails.
 *
 */
ExecutionResult SubutaiContainer::RunProgram(string program, vector<string> params, bool return_result, lxc_attach_options_t opts, bool captureOutput) 
{
    if (this->getState() != "RUNNING") throw new SubutaiException("Execution command on a not running container", 101);
    containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "Running program: ", program));
    // get arguments list of the command which will be run on lxc

    char* _params[params.size() + 2];
    _params[0] = const_cast<char*>(program.c_str());
    vector<string>::iterator it;
    int i = 1;
    for (it = params.begin(); it != params.end(); it++) {
        _params[i] = const_cast<char*>((*it).c_str());
        i++;
    }
    _params[i] = NULL;

    ExecutionResult result;
    int outfd[2];
    if (captureOutput) {
        pipe(outfd);
        opts.stdout_fd = outfd[1];
    }
    pid_t pid;
    try {
        lxc_attach_command_t cmd = {_params[0], _params};
        result.exit_code = this->container->attach(this->container, lxc_attach_run_command, &cmd, &opts, &pid);
    } catch (std::exception e) {
        containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "Execution failed (LXC): " + string(e.what())));
    }
    if (captureOutput) {
        char outbuf[4000];
        size_t size = read(outfd[0], outbuf, sizeof(outbuf) - 1);
        if (size > 0) {
            outbuf[size] = '\0';
            result.out.append(outbuf);
        }
        close(outfd[0]);
        close(outfd[1]);
    }
    containerLogger->writeLog(1, containerLogger->setLogData(
                _logEntry,
                program + " executed. Exit code: " + _helper.toString(result.exit_code)
                + ", out stream: " + result.out + ", err stream: " + result.err));
    return result;
}


/**
 *  \details   get the users defined on LXC
 */
void SubutaiContainer::UpdateUsersList() 
{ 
    if (getState() != "RUNNING") return ;
    containerLogger->writeLog(7, containerLogger->setLogData(_logEntry, "Updating user list"));
    _users.clear();
    vector<string> params;
    params.push_back("/etc/passwd");
    string passwd = RunProgram("/bin/cat", params, true, LXC_ATTACH_OPTIONS_DEFAULT, true).out;

    stringstream ss(passwd);
    string line;
    while (getline(ss, line, '\n')) {
        int uid;
        string uname;
        std::size_t found_first  = line.find(":");
        std::size_t found_second = line.find(":", found_first + 1);
        std::size_t found_third  = line.find(":", found_second + 1);
        uname = line.substr(0, found_first);
        uid   = atoi(line.substr(found_second+1, found_third).c_str());
        _users.insert(make_pair(uid, uname));
        containerLogger->writeLog(7, containerLogger->setLogData(_logEntry, "Adding user: " + _helper.toString(uid) + " " + uname));
    }
}
/**
 *  \details   ID of the Subutai Container is fetched from statically using this function.
 *  		   Example id:"ff28d7c7-54b4-4291-b246-faf3dd493544"
 */
bool SubutaiContainer::getContainerId()
{
    try
    {
        containerLogger->writeLog(7, containerLogger->setLogData(_logEntry, "Get container id"));
        containerLogger->writeLog(7, containerLogger->setLogData(_logEntry, "Check uuid.txt.."));
        string path = "/var/lib/lxc/" + this->hostname + "/rootfs/etc/subutai-agent/";
        string uuidFile = path + "uuid.txt";
        ifstream file(uuidFile.c_str());	//opening uuid.txt
        getline(file, this->id);
        file.close();

        if (this->id.empty())		//if uuid is null or not reading successfully
        {
            containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "uuid.txt is empty. Generate uuid.."));
            boost::uuids::random_generator gen;
            boost::uuids::uuid u = gen();
            const std::string tmp = boost::lexical_cast<std::string>(u);
            this->id = tmp;

            struct stat sb;
            if (!(stat(path.c_str(), &sb) == 0 && S_ISDIR(sb.st_mode)))
            {
                containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "create directory: subutai-agent.."));
                int status = mkdir(path.c_str(), S_IRWXU | S_IRWXG | S_IROTH);
                if (status != 0) containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "\"subutai-agent\" folder cannot be created under /etc/"));
            }

            containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "write generated uuid into uuid.txt.."));
            ofstream file(uuidFile.c_str());
            file << this->id;
            file.close();

            containerLogger->writeLog(1,containerLogger->setLogData(_logEntry,"Subutai Agent UUID: ",this->id));
            return false;
        }
        return true;
    } catch(const std::exception& error) {
        cout << error.what()<< endl;
    }
    return false;
}

/**
 *  \details   get mac ids of the Subutai Container is fetched from statically.
 */
bool SubutaiContainer::getContainerInterfaces()
{
    interfaces.clear();
    containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "Interfaces"));
    containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "State " + getState()));

    if (getState() != "RUNNING") return false;

    vector<string> v;
    containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "Run ifconfig on LXC"));
    string result = RunProgram("/sbin/ifconfig", v);
    vector<string> lines = _helper.splitResult(result, "\n");
    string nic = "", address = "", ip = ""; bool found_name=false, found_mac = false, found_ip = false;
    for (vector<string>::iterator it = lines.begin(); it != lines.end(); it++)
    {
        vector<string> splitted = _helper.splitResult((*it), " ");
        if ((*it).at(0) != ' ')
        {
            found_name = true; found_mac = false; found_ip = false;
            nic = splitted[0];
        }
        if (splitted.size() > 0)
        {
            bool found_m = false, found_i = false;

            for (vector<string>::iterator it_s = splitted.begin(); it_s != splitted.end(); it_s++)
            {
                if (found_m)
                {
                    found_mac = true;
                    address = *it_s;
                    found_m = false;
                }
                if (!strcmp((*it_s).c_str(), "HWaddr")) found_m = true;
                if (found_i)
                {
                    found_ip = true;
                    // TODO: This is unsafe
                    ip = _helper.splitResult((*it_s), " ")[1];
                    if (_helper.splitResult(ip, ":").size() > 1) {
                        ip = _helper.splitResult(ip, ":")[1];
                    }
                    found_i = false;
                }
                if (!strcmp((*it_s).c_str(), "inet")) found_i = true;
            }

            if(found_mac && found_name && found_ip)
            {
                struct Interface interface_n;
                interface_n.name = nic; interface_n.mac = address; interface_n.ip = ip;
                interfaces.push_back(interface_n);
                containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "Adding interface: " + nic + " " + address + " " + ip));
                found_mac = false; found_ip = false; found_name = false; nic = ""; address = ""; ip = "";
            }
        }
    }

    return true;
}


/**
 *  \details   set the hostname of Subutai Container.
 */
void SubutaiContainer::setContainerHostname(string hostname)
{
    this->hostname = hostname;
    _logEntry = "<SubutaiContainer::" + this->hostname + ">";
}

/**
 *  \details   Retrieve container architecture
 */
string SubutaiContainer::getContainerArch() 
{
    if (_arch == "" && getState() == "RUNNING") {
        try {
            vector<string> args;
            args.push_back("-m");
            ExecutionResult ex = RunProgram("uname", args, true);
            _arch = ex.out;
            size_t found = _arch.find("\n");
            if (found != string::npos) {
                _arch = _arch.substr(0, found);
            }
        } catch (std::exception e) {
            containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "Exception: " + string(e.what())));
        }
    }
    return _arch;
}

/**
 *  \details   get the status of Subutai Container.
 */
string SubutaiContainer::getContainerStatus()
{
    return this->container->state(this->container);
}

/**
 *  \details   set the status of Subutai Container.
 */
void SubutaiContainer::setContainerStatus(containerStatus status)
{
    this->status = status;
}

void SubutaiContainer::write()
{
    cout << hostname << " " << id << endl;
}

/**
 *  \details   getting SubutaiContainer uuid value.
 */
string SubutaiContainer::getContainerIdValue()
{
    return id;
}

/**
 *  \details   getting SubutaiContainer hostname value.
 */
string SubutaiContainer::getContainerHostnameValue()
{
    return hostname;
}

/**
 *  \details   getting SubutaiContainer lxc container value.
 */
lxc_container* SubutaiContainer::getLxcContainerValue()
{
    return container;
}

/**
 *  \details   getting SubutaiContainer macaddress value for a given interface.
 */
vector<Interface> SubutaiContainer::getContainerInterfaceValues()
{
    return interfaces;
}

/**
 *  \details   update all field of Subutai Container
 */
void SubutaiContainer::getContainerAllFields()
{
    containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "get all container fields"));
    clear();
    getContainerId();
    getContainerInterfaces();
    UpdateUsersList();
}

/*
 * \details Executes a command received from server.
 *          Method prepares received command from server: collects arguments and env. variables
 *          and passes them to RunProgram method 
 */
ExecutionResult SubutaiContainer::RunCommand(SubutaiCommand* command) 
{
    containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "Running command.. "));
    // set default lxc attach options
    lxc_attach_options_t opts = LXC_ATTACH_OPTIONS_DEFAULT;

    // set working directory for lxc_attach
    if (command->getWorkingDirectory() != "" && checkCWD(command->getWorkingDirectory())) {
        containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "change working directory.."));
        opts.initial_cwd = const_cast<char*>(command->getWorkingDirectory().c_str());
    }

    // set run as parameter
    if (command->getRunAs() != "" && checkUser(command->getRunAs())) {
        containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "change user running command by.."));
        opts.uid = getRunAsUserId(command->getRunAs());
    }

    // Settings env variables
    list< pair<string, string> >::iterator it;
    int i = 0;
    for (it = command->getEnvironment().begin(); it != command->getEnvironment().end(); it++, i++) {
        stringstream ss;
        ss << it->first << "=" << it->second;
        strcpy(opts.extra_env_vars[i], ss.str().c_str());
        containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "set environment " + ss.str()));
    }

    // divide program and arguments if all arguments are given in program field of command
    string program;
    vector<string> args;
    if (hasSubCommand(command)) {
        vector<string> pr = ExplodeCommandArguments(command);
        program = "sh";
        args.push_back("-c");
        string cmdline = "echo -e \"$(";
        for (vector<string>::iterator it = pr.begin(); it != pr.end(); it++) {
            cmdline.append((*it));
            cmdline.append(" ");
        }
        cmdline.append(")\"");
        args.push_back(cmdline);
    } else {
        vector<string> pr = ExplodeCommandArguments(command);
        program = pr[0];
        bool requireShell = false;
        for (vector<string>::iterator it = pr.begin()+1; it != pr.end(); it++) {
            args.push_back((*it));
        }
    }

    // execute program on LXC
    ExecutionResult res = RunProgram(program, args, true, opts, false);

    return res;
}

/*
 * \details Runs a daemon within a container
 *          Methods prepares command to be executed, parses it and executes lxc api function
 *          attach()
 */
ExecutionResult SubutaiContainer::RunDaemon(SubutaiCommand* command) {
    if (getState() != "RUNNING") throw new SubutaiException("Trying to run daemon on a non running container", 100);

    string programName;
    int outFd[2];
    int errFd[2];
    pid_t pid;
    lxc_attach_options_t opts = LXC_ATTACH_OPTIONS_DEFAULT;
    containerLogger->writeLog(6, containerLogger->setLogData(_logEntry, "Running daemon. "));

    // set working directory for lxc_attach
    if (command->getWorkingDirectory() != "" && checkCWD(command->getWorkingDirectory())) {
        containerLogger->writeLog(6, containerLogger->setLogData(_logEntry, "change working directory.."));
        opts.initial_cwd = const_cast<char*>(command->getWorkingDirectory().c_str());
    }

    // set run as parameter
    if (command->getRunAs() != "" && checkUser(command->getRunAs())) {
        containerLogger->writeLog(6, containerLogger->setLogData(_logEntry, "change user running command by.."));
        opts.uid = getRunAsUserId(command->getRunAs());
    }

    // Parsing arguments
    vector<string> pr = ExplodeCommandArguments(command);
    int ret;
    SubutaiHelper h;
    if (hasSubCommand(command)) {
        char* args[3];
        args[0] = "sh";
        args[1] = "-c";
        string cmdLine = "echo -e \"$(";
        for (vector<string>::iterator it = pr.begin(); it != pr.end(); it++) {
            cmdLine.append((*it));
            cmdLine.append(" ");
        }
        cmdLine.append(")\"");
        args[2] = const_cast<char*>(cmdLine.c_str());
        args[3] = NULL;
        lxc_attach_command_t cmd = {args[0], args};
        try {
            ret = this->container->attach(this->container, lxc_attach_run_command, &cmd, &opts, &pid);
        } catch (std::exception e) {
            containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "Daemon Exception: ", string(e.what())));
        }
    } else {
        char* args[pr.size() + 1];
        int i = 0;
        for (vector<string>::iterator it = pr.begin(); it != pr.end(); it++, i++) {
            if (it == pr.begin()) {
                // This is a first argument which is actually a program name
                programName = (*it); 
                args[i] = const_cast<char*>((*it).c_str());
                continue;
            }
            args[i] = const_cast<char*>((*it).c_str());
        }
        args[i] = NULL;
        lxc_attach_command_t cmd = {const_cast<char*>(programName.c_str()), args};
        try {
            ret = this->container->attach(this->container, lxc_attach_run_command, &cmd, &opts, &pid);
        } catch (std::exception e) {
            containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "Daemon Exception: ", string(e.what())));
        }
    }
    containerLogger->writeLog(6, containerLogger->setLogData(_logEntry, "Daemon pid: ", h.toString(pid)));
    containerLogger->writeLog(6, containerLogger->setLogData(_logEntry, "Exit code: ", h.toString(ret)));
    ExecutionResult res;
    res.pid = pid;
    return res;
}

// We need to check if CWD is exist because in LXC API - if cwd does not
// exist CWD will become root directory
bool SubutaiContainer::checkCWD(string cwd) 
{
    if (getState() != "RUNNING") return false;
    vector<string> params;
    params.push_back(cwd);
    ExecutionResult result = RunProgram("ls", params, false, LXC_ATTACH_OPTIONS_DEFAULT);    
    if (result.exit_code == 0) { 
        return true;
    } else {
        containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "working directory not found: "+ cwd + "on " + this->hostname));
        return false;
    }
}

/*
 * /details     Runs throught the list of userid:username pairs
 *              and check user existence
 */
bool SubutaiContainer::checkUser(string username) 
{
    if (getState() != "RUNNING") return false;
    if (_users.empty()) {
        UpdateUsersList();
    }
    for (user_it it = _users.begin(); it != _users.end(); it++) {
        if ((*it).second.compare(username) == 0) {
            return true;
        }
    } 
    containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "user not found: "+ username + "on " + this->hostname));
    return false;
}


/*
 * /details     Runs through the list of userid:username pairs
 *              and returns user id if username was found
 */
int SubutaiContainer::getRunAsUserId(string username) 
{
    if (_users.empty()) {
        UpdateUsersList();
    }
    for (user_it it = _users.begin(); it != _users.end(); it++) {
        if ((*it).second.compare(username) == 0) {
            return (*it).first;
        }
    } 
    containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "user not found: "+ username + "on " + this->hostname));
    return -1;
}

/**
 * \details		Write info into a file on LXC
 */
void SubutaiContainer::PutToFile(string filename, string text) {
    vector<string> args;
    args.push_back("-c");
    args.push_back("'/bin/echo");
    args.push_back(text);
    args.push_back(">");
    args.push_back(filename);
    args.push_back("'");
    containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "Echo "+ text + "in " + filename));
    RunProgram("/bin/bash", args);
}

/**
 * \details		Get the full path for a given program
 */
string SubutaiContainer::findFullProgramPath(string program_name) 
{
    vector<string> args;
    args.push_back(program_name);
    string locations = RunProgram("whereis", args);
    return _helper.splitResult(program_name, "\n")[1];
}

/**
 * \details 	run ps command on LXC.
 */
string SubutaiContainer::RunPsCommand() {
    vector<string> args;
    containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, "Running ps command.."));
    return RunProgram("/opt/psrun", args);
}

/**
 * \details 	check and divide command and arguments if necessary.
 */
vector<string> SubutaiContainer::ExplodeCommandArguments(SubutaiCommand* command) 
{
    vector<string> result;
    size_t p = 0;
    size_t n = 0;
    while ((n = command->getCommand().find_first_of(" ", p)) != string::npos) {
        if (n - p != 0) {
            result.push_back(command->getCommand().substr(p, n - p));
        }
        p = n + 1;
    } 
    if (p < command->getCommand().size()) {
        result.push_back(command->getCommand().substr(p));
    }
    for(unsigned int i = 0; i < command->getArguments().size(); i++)
        result.push_back(command->getArguments()[i]);

    return result;
}

/**
 * For testing purpose
 *
 * Test if long commands with && can run or not:
 * It waits until all the commands run to return.
 */
void SubutaiContainer::tryLongCommand() 
{
    vector<string> args;
    args.push_back("-c");
    args.push_back("ls -la && ls && ls -la && ls && sleep 2 && ls && ls -la && ls && ls -la && ls && ls && sleep 2 && ls && ls -la && ls && ls -la && ls && ls && sleep 2 && ls && ls -la && ls && ls -la && ls && ls && sleep 2 && ls && ls -la && ls && ls -la && ls && ls && sleep 2 && ls && ls -la && ls && ls -la && ls && ls && sleep 2 && ls && ls -la && ls && ls -la && ls && ls && sleep 2 && ls && ls -la && ls && ls -la && ls && ls && sleep 2 && ls && ls -la && ls && ls -la && ls");
    cout << RunProgram("/bin/bash", args) << endl;
}

/*
 * \details Method invokes state() function from lxc api, which returns state of current container
 */
string SubutaiContainer::getState() 
{
    try {
        return string(this->container->state(this->container));
    } catch (std::exception e) {
        containerLogger->writeLog(1, containerLogger->setLogData(_logEntry, string(e.what())));
    }
}


/*
 * \details check if this command has |, > or >>
 */
bool SubutaiContainer::hasSubCommand(SubutaiCommand* command) {
    vector<string> args = ExplodeCommandArguments(command);
    for (vector<string>::iterator it = args.begin(); it != args.end(); it++) {
        if (it->compare("|") == 0 || it->compare(">") == 0 || it->compare(">>") == 0 || it->compare(";") || it->compare("&") || it->compare("&&") || it->compare("<")) {
            return true;
        }
    }
    return false;
}
