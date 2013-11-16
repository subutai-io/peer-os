#include "KALogger.h"
/**
 *  \details   Default constructor of the KALogger class.
 */
KALogger::KALogger()
{
	// TODO Auto-generated constructor stub
}
/**
 *  \details   Default destructor of the KALogger class.
 */
KALogger::~KALogger()
{
	// TODO Auto-generated destructor stub
}
/**
 *  \details   getting "loglevel" private variable of the KALogger instance.
 */
int KALogger::getLogLevel()
{
	return this->loglevel;
}
/**
 *  \details   setting "loglevel" private variable of the KALogger instance.
 *  		   This level indicates that the loglevel status.
 *  		   it should be between (0-7) -> (Emergency-Debug)
 */
void KALogger::setLogLevel(int loglevel)
{
	this->loglevel=loglevel;
}
/**
 *  \details   This method creates local time values as a string.
 *  		   The return value as dd-mm-yy hh:mm::ss
 */
string KALogger::getLocaltime()
{
	boost::posix_time::ptime now = boost::posix_time::second_clock::local_time();
	return toString(now.date().day().as_number()) +"-"+ toString(now.date().month().as_number())+"-" +toString(now.date().year()) +" "+ toString(now.time_of_day().hours())+":"+toString(now.time_of_day().minutes())+":"+toString(now.time_of_day().seconds());
}
/**
 *  \details   This method designed for Typically conversion from integer to string.
 */
string KALogger::toString(int intcont)
{		//integer to string conversion
	ostringstream dummy;
	dummy << intcont;
	return dummy.str();
}
/**
 *  \details   This method opens a log file.
 *  		   For name production, local time,process ID and Sequence Number are used.
 */
void KALogger::openLogFile(int pid,int requestSequenceNumber)
{
	boost::posix_time::ptime now = boost::posix_time::second_clock::local_time();
	string logFileName = "/var/log/KiskisAgent/" + toString(now.time_of_day().total_milliseconds()) + "-"+toString(pid)+"-"+toString(requestSequenceNumber);
	this->logFile = fopen(logFileName.c_str(),"a+");;
}
/**
 *  \details   This method opens a log file with given name.
 */
void KALogger::openLogFileWithName(string logfilename)
{
	logfilename = "/var/log/KiskisAgent/" + logfilename;
	this->logFile = fopen(logfilename.c_str(),"a+");;
}
/**
 *  \details   This method closed the log file.
 */
void KALogger::closeLogFile()
{
	fclose(logFile);
}
string KALogger::setLogData(string text,string param1,string value1,string param2,string value2)
{
	return text + " " + param1 + " " + value1 + " " + param2 + " " + value2;
}
/**
 *  \details   This method writes the logs to log files according to 8 log level.
 */
void KALogger::writeLog(int level,string log)
{
	switch(this->loglevel)
	{
	case 7:
		switch(level)
		{
		case 7:
			log = getLocaltime()+" <DEBUG>"+log + "\n";
			fprintf(logFile,log.c_str());
			break;
		case 6:
			log =getLocaltime()+" <INFO>"+log + "\n";
			fprintf(logFile,log.c_str());
			break;
		case 5:
			log = getLocaltime()+" <NOTICE>"+log + "\n";
			fprintf(logFile,log.c_str());
			break;
		case 4:
			log =getLocaltime()+" <WARNING>"+log + "\n";
			fprintf(logFile,log.c_str());
			break;
		case 3:
			log = getLocaltime()+" <ERROR>"+log + "\n";
			fprintf(logFile,log.c_str());
			break;
		case 2:
			log = getLocaltime()+" <CRITICAL>"+log + "\n";
			fprintf(logFile,log.c_str());
			break;
		case 1:
			log = getLocaltime()+" <ALERT>"+log + "\n";
			fprintf(logFile,log.c_str());
			break;
		case 0:
			log = getLocaltime()+" <EMERGENCY>"+log + "\n";
			fprintf(logFile,log.c_str());
			break;
		}
		break;
		case 6:
			switch(level)
			{
			case 6:
				log =getLocaltime()+" <INFO>"+log + "\n";
				fprintf(logFile,log.c_str());
				break;
			case 5:
				log = getLocaltime()+" <NOTICE>"+log + "\n";
				fprintf(logFile,log.c_str());
				break;
			case 4:
				log =getLocaltime()+" <WARNING>"+log + "\n";
				fprintf(logFile,log.c_str());
				break;
			case 3:
				log = getLocaltime()+" <ERROR>"+log + "\n";
				fprintf(logFile,log.c_str());
				break;
			case 2:
				log = getLocaltime()+" <CRITICAL>"+log + "\n";
				fprintf(logFile,log.c_str());
				break;
			case 1:
				log = getLocaltime()+" <ALERT>"+log + "\n";
				fprintf(logFile,log.c_str());
				break;
			case 0:
				log = getLocaltime()+" <EMERGENCY>"+log + "\n";
				fprintf(logFile,log.c_str());
				break;
			}
			break;
			case 5:
				switch(level)
				{
				case 5:
					log = getLocaltime()+" <NOTICE>"+log + "\n";
					fprintf(logFile,log.c_str());
					break;
				case 4:
					log =getLocaltime()+" <WARNING>"+log + "\n";
					fprintf(logFile,log.c_str());
					break;
				case 3:
					log = getLocaltime()+" <ERROR>"+log + "\n";
					fprintf(logFile,log.c_str());
					break;
				case 2:
					log = getLocaltime()+" <CRITICAL>"+log + "\n";
					fprintf(logFile,log.c_str());
					break;
				case 1:
					log = getLocaltime()+" <ALERT>"+log + "\n";
					fprintf(logFile,log.c_str());
					break;
				case 0:
					log = getLocaltime()+" <EMERGENCY>"+log + "\n";
					fprintf(logFile,log.c_str());
					break;
				}
				break;
				case 4:
					switch(level)
					{
					case 4:
						log =getLocaltime()+" <WARNING>"+log + "\n";
						fprintf(logFile,log.c_str());
						break;
					case 3:
						log = getLocaltime()+" <ERROR>"+log + "\n";
						fprintf(logFile,log.c_str());
						break;
					case 2:
						log = getLocaltime()+" <CRITICAL>"+log + "\n";
						fprintf(logFile,log.c_str());
						break;
					case 1:
						log = getLocaltime()+" <ALERT>"+log + "\n";
						fprintf(logFile,log.c_str());
						break;
					case 0:
						log = getLocaltime()+" <EMERGENCY>"+log + "\n";
						fprintf(logFile,log.c_str());
						break;
					}
					break;
					case 3:
						switch(level)
						{
						case 3:
							log = getLocaltime()+" <ERROR>"+log + "\n";
							fprintf(logFile,log.c_str());
							break;
						case 2:
							log = getLocaltime()+" <CRITICAL>"+log + "\n";
							fprintf(logFile,log.c_str());
							break;
						case 1:
							log = getLocaltime()+" <ALERT>"+log + "\n";
							fprintf(logFile,log.c_str());
							break;
						case 0:
							log = getLocaltime()+" <EMERGENCY>"+log + "\n";
							fprintf(logFile,log.c_str());
							break;
						}
						break;
						case 2:
							switch(level)
							{
							case 2:
								log = getLocaltime()+" <CRITICAL>"+log + "\n";
								fprintf(logFile,log.c_str());
								break;
							case 1:
								log = getLocaltime()+" <ALERT>"+log + "\n";
								fprintf(logFile,log.c_str());
								break;
							case 0:
								log = getLocaltime()+" <EMERGENCY>"+log + "\n";
								fprintf(logFile,log.c_str());
								break;
							}
							break;
							case 1:
								switch(level)
								{
								case 1:
									log = getLocaltime()+" <ALERT>"+log + "\n";
									fprintf(logFile,log.c_str());
									break;
								case 0:
									log = getLocaltime()+" <EMERGENCY>"+log + "\n";
									fprintf(logFile,log.c_str());
									break;
								}
								break;
								case 0:
									switch(level)
									{
									case 0:
										log = getLocaltime()+" <EMERGENCY>"+log + "\n";
										fprintf(logFile,log.c_str());
										break;
									}
									break;
	}
	fflush(logFile);
}
