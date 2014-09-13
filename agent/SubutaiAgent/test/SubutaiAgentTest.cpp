#include <iostream>
#include <stdio.h>
#include <string>
#include <list>
#include <cppunit/TestCase.h>
#include <cppunit/TestFixture.h>
#include <cppunit/ui/text/TextTestRunner.h>
#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/extensions/TestFactoryRegistry.h>
#include <cppunit/TestResult.h>
#include <cppunit/TestResultCollector.h>
#include <cppunit/TestRunner.h>
#include <cppunit/BriefTestProgressListener.h>
#include <cppunit/CompilerOutputter.h>
#include <cppunit/XmlOutputter.h>
#include <netinet/in.h>

#include "SubutaiCommand.h"
#include "SubutaiResponse.h"
#include "SubutaiLogger.h"
#include "SubutaiConnection.h"
#include "SubutaiResponsePack.h"
#include "SubutaiStreamReader.h"
#include "SubutaiThread.h"

using namespace CppUnit;
using namespace std;

class AgentTest : public CppUnit::TestFixture
{
	CPPUNIT_TEST_SUITE(AgentTest);
	CPPUNIT_TEST(testCommandDeserialize);
	CPPUNIT_TEST(testCommandClear);
	CPPUNIT_TEST(testResponseSerialize);
	CPPUNIT_TEST(testResponseSerializeDone);
	CPPUNIT_TEST(testResponseClear);
	CPPUNIT_TEST(testLoggerOpenLogFileWithName);
	CPPUNIT_TEST(testLoggerOpenLogFile);
	CPPUNIT_TEST(testLoggerWriteLog);
	CPPUNIT_TEST(testUserIDCheekRootUser);
	CPPUNIT_TEST(testResponsePackCreateResponse);
	CPPUNIT_TEST(testResponsePackCreateExit);
	CPPUNIT_TEST(testResponsePackCreateRegistration);
	CPPUNIT_TEST(testResponsePackCreateHeartbeat);
	CPPUNIT_TEST(testResponsePackCreateTerminate);
	CPPUNIT_TEST(testResponsePackCreateTerminateFail);
	CPPUNIT_TEST(testResponsePackCreateTimeout);
	CPPUNIT_TEST_SUITE_END();

public:
	void setUp(void);
	void tearDown(void);

protected:
	void testCommandDeserialize(void);
	void testCommandClear(void);
	void testResponseSerialize(void);
	void testResponseSerializeDone(void);
	void testResponseClear(void);
	void testLoggerOpenLogFileWithName(void);
	void testLoggerOpenLogFile(void);
	void testLoggerWriteLog(void);
	void testUserIDCheekRootUser(void);
	void testResponsePackCreateResponse(void);
	void testResponsePackCreateExit(void);
	void testResponsePackCreateRegistration(void);
	void testResponsePackCreateHeartbeat(void);
	void testResponsePackCreateTerminate(void);
	void testResponsePackCreateTerminateFail(void);
	void testResponsePackCreateTimeout(void);

private:

	SubutaiCommand *cmd;
	SubutaiResponse *resp;
	SubutaiResponsePack *pack;
	SubutaiThread *thr;
	SubutaiLogger *logger;
	SubutaiUserID *usr;
};

void AgentTest::setUp(void)
{
	cmd = new SubutaiCommand();
	resp = new SubutaiResponse();
	pack = new SubutaiResponsePack();
	thr = new SubutaiThread();
	logger = new SubutaiLogger();
	usr = new SubutaiUserID();
}

void AgentTest::tearDown(void)
{
	delete cmd;
	delete resp;
	delete pack;
	delete thr;
	delete logger;
	delete usr;
}
//SubutaiCommand-Deserialize
void AgentTest::testCommandDeserialize(void)
{
	//Test string for deserialization
	string input = "{\"command\":{\"source\":\"AsyncRunner\",\"type\": \"EXECUTE_REQUEST\","
			"\"uuid\": \"5373b7c4-a039-44a9-9270-9e0e45d549cf\",\"taskUuid\": \"a7349720-9e2f-11e3-b9d6-080027b00009\","
			"\"requestSequenceNumber\": 1,\"workingDirectory\": \"/home\",\"program\": \"ls\",\"stdOut\": \"RETURN\","
			"\"stdErr\": \"RETURN\",\"runAs\": \"root\",\"timeout\": 30}}";

	cmd->deserialize(input);

	CPPUNIT_ASSERT("AsyncRunner"== cmd->getSource());
	CPPUNIT_ASSERT("EXECUTE_REQUEST"== cmd->getType());
	CPPUNIT_ASSERT("5373b7c4-a039-44a9-9270-9e0e45d549cf"== cmd->getUuid());
	CPPUNIT_ASSERT("a7349720-9e2f-11e3-b9d6-080027b00009"== cmd->getTaskUuid());
	CPPUNIT_ASSERT(1 == cmd->getRequestSequenceNumber());
	CPPUNIT_ASSERT("/home"== cmd->getWorkingDirectory());
	CPPUNIT_ASSERT("ls"== cmd->getProgram());
	CPPUNIT_ASSERT("RETURN"== cmd->getStandardOutput());
	CPPUNIT_ASSERT("RETURN"== cmd->getStandardError());
	CPPUNIT_ASSERT("root"== cmd->getRunAs());
	CPPUNIT_ASSERT(30== cmd->getTimeout());
}
//SubutaiCommand-Clear
void AgentTest::testCommandClear(void)
{
	cmd->setType("123");
	cmd->setProgram("test");
	cmd->setWorkingDirectory("/");
	cmd->setUuid("12345");
	cmd->setPid(1522);
	cmd->setRequestSequenceNumber(31);
	cmd->setStandardError("/");
	cmd->setStandardErrPath("/");
	cmd->setStandardOutput("/");
	cmd->setStandardOutPath("/");
	cmd->setRunAs("root");
	cmd->setTimeout(45);
	cmd->getArguments().clear();
	cmd->getEnvironment().clear();
	cmd->setTaskUuid("12345");
	cmd->setMacAddress("aa:bb:cc:dd:ee:ff");
	cmd->setHostname("test");
	cmd->getIps().clear();
	cmd->setSource("test");

	cmd->clear();

	CPPUNIT_ASSERT(""== cmd->getSource());
	CPPUNIT_ASSERT(""== cmd->getType());
	CPPUNIT_ASSERT(""== cmd->getUuid());
	CPPUNIT_ASSERT(""== cmd->getTaskUuid());
	CPPUNIT_ASSERT(-1 == cmd->getRequestSequenceNumber());
	CPPUNIT_ASSERT(""== cmd->getWorkingDirectory());
	CPPUNIT_ASSERT(""== cmd->getProgram());
	CPPUNIT_ASSERT(""== cmd->getMacAddress());
	CPPUNIT_ASSERT(""== cmd->getHostname());
	CPPUNIT_ASSERT(""== cmd->getStandardOutput());
	CPPUNIT_ASSERT(""== cmd->getStandardOutputPath());
	CPPUNIT_ASSERT(""== cmd->getStandardError());
	CPPUNIT_ASSERT(""== cmd->getStandardErrPath());
	CPPUNIT_ASSERT(""== cmd->getRunAs());
	CPPUNIT_ASSERT(30== cmd->getTimeout());
	CPPUNIT_ASSERT(-1== cmd->getPid());
}
//SubutaiResponse-Serialize
void AgentTest::testResponseSerialize(void)
{
	//Test string for serialization
	string input =	"{\"response\":{\"hostname\":\"management\",\"ips\":[\"10.10.10.1\",\"172.16.11.4\",\"127.0.0.1\"],"
			"\"macAddress\":\"08:00:27:59:3b:2e\",\"responseSequenceNumber\":1,\"type\":\"HEARTBEAT_RESPONSE\","
			"\"uuid\":\"5373b7c4-a039-44a9-9270-9e0e45d549cf\"}}\n";

	string result;
	resp->setHostname("management");
	resp->setType("HEARTBEAT_RESPONSE");
	resp->getIps().push_back("10.10.10.1");
	resp->getIps().push_back("172.16.11.4");
	resp->getIps().push_back("127.0.0.1");
	resp->setMacAddress("08:00:27:59:3b:2e");;
	resp->setResponseSequenceNumber(1);
	resp->setUuid("5373b7c4-a039-44a9-9270-9e0e45d549cf");

	resp->serialize(result);

	CPPUNIT_ASSERT_EQUAL(input,result); //expected,actual
}
//SubutaiResponse-SerializeDone
void AgentTest::testResponseSerializeDone(void)
{
	//Test string for serialization
	string input = "{\"response\":{\"exitCode\":0,\"pid\":2584,\"requestSequenceNumber\":1,\"responseSequenceNumber\":1,"
			"\"source\":\"AsyncRunner\",\"taskUuid\":\"a7349720-9e2f-11e3-b9d6-080027b00009\","
			"\"type\":\"EXECUTE_RESPONSE_DONE\",\"uuid\":\"5373b7c4-a039-44a9-9270-9e0e45d549cf\"}}\n";

	string result;

	resp->setExitCode(0);
	resp->setPid(2584);
	resp->setRequestSequenceNumber(1);
	resp->setResponseSequenceNumber(1);
	resp->setSource("AsyncRunner");
	resp->setTaskUuid("a7349720-9e2f-11e3-b9d6-080027b00009");
	resp->setType("EXECUTE_RESPONSE_DONE");
	resp->setUuid("5373b7c4-a039-44a9-9270-9e0e45d549cf");

	resp->serializeDone(result);

	CPPUNIT_ASSERT_EQUAL(input,result); //expected,actual
}
//SubutaiResponse-Clear
void AgentTest::testResponseClear(void)
{
	resp->setType("123");
	resp->setUuid("12345");
	resp->setPid(1522);
	resp->setRequestSequenceNumber(31);
	resp->setResponseSequenceNumber(41);
	resp->setStandardError("/");
	resp->setStandardOutput("/");
	resp->setSource("test");
	resp->setTaskUuid("12345");
	resp->setMacAddress("aa:bb:cc:dd:ee:ff");
	resp->setHostname("test");

	resp->clear();

	CPPUNIT_ASSERT(""== resp->getSource());
	CPPUNIT_ASSERT(""== resp->getType());
	CPPUNIT_ASSERT(""== resp->getUuid());
	CPPUNIT_ASSERT(""== resp->getTaskUuid());
	CPPUNIT_ASSERT(-1 == resp->getRequestSequenceNumber());
	CPPUNIT_ASSERT(-1 == resp->getResponseSequenceNumber());
	CPPUNIT_ASSERT(""== resp->getMacAddress());
	CPPUNIT_ASSERT(""== resp->getHostname());
	CPPUNIT_ASSERT(""== resp->getStandardOutput());
	CPPUNIT_ASSERT(""== resp->getStandardError());
	CPPUNIT_ASSERT(-1== resp->getPid());
}
//SubutaiLogger-LoggingwithfileName
void AgentTest::testLoggerOpenLogFileWithName(void)
{
	string filename = "test";
	bool result = logger->openLogFileWithName(filename);

	if(result)
	{
		logger->closeLogFile();
		if(remove("/var/log/subutai-agent/test") != 0 )
			perror( "Error deleting file" );
	}

	CPPUNIT_ASSERT(true == result);
}
//SubutaiLogger-Loggingwithfile
void AgentTest::testLoggerOpenLogFile(void)
{

	bool result = logger->openLogFile(1000,31);
	if(result)
	{
		logger->closeLogFile();
	}
	CPPUNIT_ASSERT(true == result);

}
//SubutaiLogger-WriteLog
void AgentTest::testLoggerWriteLog(void)
{
	string filename = "test";
	logger->setLogLevel(7);
	bool result = logger->openLogFileWithName(filename);

	CPPUNIT_ASSERT(true == result);

	if(result == true)
	{
		string testlog = "This is a test log!!";
		logger->writeLog(5,testlog);

		string filename="/var/log/subutai-agent/test";
		FILE *p_file = NULL;
		p_file = fopen(filename.c_str(),"rb");
		fseek(p_file,0,SEEK_END);
		int size = 0;
		size = ftell(p_file);
		fclose(p_file);

		CPPUNIT_ASSERT(size > 0);

		logger->closeLogFile();
		if(remove("/var/log/subutai-agent/test") != 0 )
			perror( "Error deleting file" );
	}

}
//SubutaiUserID-CheekRootUser
void AgentTest::testUserIDCheekRootUser(void)
{
	CPPUNIT_ASSERT(true == usr->checkRootUser());
}

//SubutaiResponsePack-CreateResponseMessage
void AgentTest::testResponsePackCreateResponse(void)
{
	//Test string for serialization
	string input =	"{\"response\":{\"pid\":8762,\"requestSequenceNumber\":1,"
			"\"responseSequenceNumber\":2,\"source\":\"AsyncRunner\","
			"\"stdErr\":\"error\","
			"\"stdOut\":\"test\","
			"\"taskUuid\":\"9abddb80-9ee5-11e3-b9d6-080027b00009\","
			"\"type\":\"EXECUTE_RESPONSE\",\"uuid\":\"5373b7c4-a039-44a9-9270-9e0e45d549cf\"}}\n";

	string uuid = "5373b7c4-a039-44a9-9270-9e0e45d549cf" ;
	string type = "EXECUTE_RESPONSE" ;
	string taskuuid = "9abddb80-9ee5-11e3-b9d6-080027b00009";
	string stdOut = "test";
	string stdErr = "error";
	string source = "AsyncRunner";
	int pid = 8762;
	int reqnumber = 1;
	int respnumber = 2;


	string result = pack->createResponseMessage(uuid,pid,reqnumber,respnumber,stdErr,stdOut,source,taskuuid);

	CPPUNIT_ASSERT_EQUAL(input,result); //expected,actual
}
//SubutaiResponsePack-CreateExitMessage
void AgentTest::testResponsePackCreateExit(void)
{
	//Test string for serialization
	string input =	"{\"response\":{\"exitCode\":0,\"pid\":14601,\"requestSequenceNumber\":1,\"responseSequenceNumber\":1,"
			"\"source\":\"AsyncRunner\",\"taskUuid\":\"dca7e550-9f8e-11e3-b9d6-080027b00009\","
			"\"type\":\"EXECUTE_RESPONSE_DONE\",\"uuid\":\"5373b7c4-a039-44a9-9270-9e0e45d549cf\"}}\n";

	string uuid = "5373b7c4-a039-44a9-9270-9e0e45d549cf" ;
	string type = "EXECUTE_RESPONSE_DONE";
	string source = "AsyncRunner";
	string taskuuid= "dca7e550-9f8e-11e3-b9d6-080027b00009";
	int pid = 14601;
	int reqnumber = 1;
	int respnumber = 1;
	int exitcode = 0;

	string result = pack->createExitMessage(uuid,pid,reqnumber,respnumber,source,taskuuid,exitcode);

	CPPUNIT_ASSERT_EQUAL(input,result); //expected,actual
}
//SubutaiResponsePack-CreateRegisterMessage
void AgentTest::testResponsePackCreateRegistration(void)
{
	//Test string for serialization
	string input =	"{\"response\":{\"hostname\":\"management\",\"ips\":[\"10.10.10.1\",\"172.16.11.4\",\"127.0.0.1\"],"
			"\"macAddress\":\"08:00:27:59:3b:2e\","
			"\"type\":\"REGISTRATION_REQUEST\",\"uuid\":\"5373b7c4-a039-44a9-9270-9e0e45d549cf\"}}\n";

	vector<string> ipaddress;

	ipaddress.push_back("10.10.10.1");
	ipaddress.push_back("172.16.11.4");
	ipaddress.push_back("127.0.0.1");

	string macaddress = "08:00:27:59:3b:2e";
	string uuid = "5373b7c4-a039-44a9-9270-9e0e45d549cf" ;
	string type = "REGISTRATION_REQUEST";
	string hostname = "management";
	string parentHostName= "";
	pack->setIps(ipaddress);
        string envid = "6673b7c4-a079-77a9-7270-770e45d54sdf";
	
	string result = pack->createRegistrationMessage(uuid,macaddress,hostname,parentHostName,envid,ipaddress);
}
//SubutaiResponsePack-CreateHeartBeatMessage
void AgentTest::testResponsePackCreateHeartbeat(void)
{
	//Test string for serialization
	string input =  "{\"response\":{\"environmentId\":\"6673b7c4-a079-77a9-7270-770e45d54sdf\",\"hostname\":\"management\",\"ips\":[\"10.10.10.1\",\"172.16.11.4\",\"127.0.0.1\"],"
                        "\"macAddress\":\"08:00:27:59:3b:2e\",\"responseSequenceNumber\":1,\"taskUuid\":\"4573n9c4-a051-44a9-9660-9e0e45d54add\","
                        "\"type\":\"HEARTBEAT_RESPONSE\",\"uuid\":\"5373b7c4-a039-44a9-9270-9e0e45d549cf\"}}\n";

	string result;
	string hostname = "management";
	string parentHostName = "";
	string type = "HEARTBEAT_RESPONSE";
	vector<string> ipaddress;
	ipaddress.push_back("10.10.10.1");
	ipaddress.push_back("172.16.11.4");
	ipaddress.push_back("127.0.0.1");
	pack->setIps(ipaddress);

        
	string macAddress = "08:00:27:59:3b:2e";

	int reqnumber = -1;
	string uuid = "5373b7c4-a039-44a9-9270-9e0e45d549cf";

	string source = "";
	string taskuuid = "4573n9c4-a051-44a9-9660-9e0e45d54add";
        string envid = "6673b7c4-a079-77a9-7270-770e45d54sdf";

	result = pack->createHeartBeatMessage(uuid,reqnumber,envid,macAddress,hostname,parentHostName,source,taskuuid);

	CPPUNIT_ASSERT_EQUAL(input,result); //expected,actual
}
//SubutaiResponsePack-CreateTermiateMessage
void AgentTest::testResponsePackCreateTerminate(void)
{
	//Test string for serialization
	string input =	"{\"response\":{\"requestSequenceNumber\":1,\"responseSequenceNumber\":1,\"source\":\"AsyncRunner\","
			"\"taskUuid\":\"4573n9c4-a051-44a9-9660-9e0e45d54add\","
                        "\"type\":\"TERMINATE_RESPONSE_DONE\",\"uuid\":\"5373b7c4-a039-44a9-9270-9e0e45d549cf\"}}\n";

	string uuid = "5373b7c4-a039-44a9-9270-9e0e45d549cf";
	string source = "AsyncRunner";
	int reqnumber = 1;
        string taskuuid = "4573n9c4-a051-44a9-9660-9e0e45d54add";
	string result = pack->createTerminateMessage(uuid,reqnumber,source,taskuuid);

	CPPUNIT_ASSERT_EQUAL(input,result); //expected,actual
}
//SubutaiResponsePack-CreateTermiateFailMessage
void AgentTest::testResponsePackCreateTerminateFail(void)
{
	//Test string for serialization
	string input =	"{\"response\":{\"requestSequenceNumber\":1,\"responseSequenceNumber\":1,\"source\":\"AsyncRunner\","
			"\"taskUuid\":\"4573n9c4-a051-44a9-9660-9e0e45d54add\","		
			"\"type\":\"TERMINATE_RESPONSE_FAILED\",\"uuid\":\"5373b7c4-a039-44a9-9270-9e0e45d549cf\"}}\n";

	string uuid = "5373b7c4-a039-44a9-9270-9e0e45d549cf";
	string source = "AsyncRunner";
	int reqnumber = 1;
       	string taskuuid = "4573n9c4-a051-44a9-9660-9e0e45d54add";       
	string result = pack->createFailTerminateMessage(uuid,reqnumber,source,taskuuid);

	CPPUNIT_ASSERT_EQUAL(input,result); //expected,actual
}
//SubutaiResponsePack-CreateTimeoutMessage
void AgentTest::testResponsePackCreateTimeout(void)
{
	//Test string for serialization
	string input ="{\"response\":{\"pid\":15020,\"requestSequenceNumber\":1,\"responseSequenceNumber\":2,"
			"\"source\":\"AsyncRunner\",\"taskUuid\":\"58e7f9d0-9f98-11e3-b9d6-080027b00009\","
			"\"type\":\"EXECUTE_TIMEOUT\",\"uuid\":\"5373b7c4-a039-44a9-9270-9e0e45d549cf\"}}\n";

	string result;
	int pid = 15020;
	int reqnumber = 1;
	int resnumber = 2;
	string stdErr = "";
	string stdOut = "";
	string source = "AsyncRunner";
	string uuid = "5373b7c4-a039-44a9-9270-9e0e45d549cf";
	string type = "EXECUTE_TIMEOUT";
	string taskuuid = "58e7f9d0-9f98-11e3-b9d6-080027b00009";

	result = pack->createTimeoutMessage(uuid,pid,reqnumber,resnumber,stdOut,stdErr,source,taskuuid);

	CPPUNIT_ASSERT_EQUAL(input,result); //expected,actual
}



CPPUNIT_TEST_SUITE_REGISTRATION( AgentTest );
int main(int argc, char* argv[])
{
	// informs test-listener about testresults
	CPPUNIT_NS::TestResult testresult;
	// register listener for collecting the test-results
	CPPUNIT_NS::TestResultCollector collectedresults;
	testresult.addListener (&collectedresults);
	// register listener for per-test progress output
	CPPUNIT_NS::BriefTestProgressListener progress;
	testresult.addListener (&progress);
	// insert test-suite at test-runner by registry
	CPPUNIT_NS::TestRunner testrunner;
	testrunner.addTest (CPPUNIT_NS::TestFactoryRegistry::getRegistry().makeTest ());
	testrunner.run(testresult);
	// output results in compiler-format
	CPPUNIT_NS::CompilerOutputter compileroutputter(&collectedresults, std::cerr);
	compileroutputter.write ();
	// Output XML for Jenkins CPPunit plugin
	ofstream xmlFileOut("AgentUnitTestReport.xml");
	XmlOutputter xmlOut(&collectedresults, xmlFileOut);
	xmlOut.write();
	// return 0 if tests were successful
	return collectedresults.wasSuccessful() ? 0 : 1;
}



