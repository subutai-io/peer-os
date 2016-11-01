#!groovy

// Cofiguring builder:
// Manage Jenkins -> Global Tool Configuration -> Maven installations -> Add Maven:
// Name - M3
// MAVEN_HOME - path to Maven3 home dir
//
// Manage Jenkins -> Configure System -> Environment variables
// SS_TEST_NODE - ip of SS node for smoke tests 
//
// Approve methods:
// in build job log you will see 
// Scripts not permitted to use new <method>
// Goto http://jenkins.domain/scriptApproval/
// and approve methods denied methods
//
// TODO:
// - refactor getVersion function on native groovy
// - Stash and unstash for builded artifacts (?)

import groovy.json.JsonSlurperClassic

node() {
	// Send job started notifications
	try {
	notifyBuild('STARTED')

	def mvnHome = tool 'M3'
	def workspace = pwd() 
	def artifactVersion = getVersion("${workspace}@script/management/pom.xml")
	String artifactDir = "/tmp/jenkins/${env.JOB_NAME}"
	String debFileName = "management-${env.BRANCH_NAME}.deb"
	String templateFileName = "management-subutai-template_${artifactVersion}-${env.BRANCH_NAME}_amd64.tar.gz"

	
	stage("Build management deb/template")
	// Use maven to to build deb and template files of management

	checkout scm

	// create dir for artifacts
	sh """
		if test ! -d ${artifactDir}; then mkdir -p ${artifactDir}; fi
	"""

	// build deb
	sh """
		cd management
		${mvnHome}/bin/mvn clean install -Dmaven.test.skip=true -P deb -Dgit.branch=${env.BRANCH_NAME}
		find ${workspace}/management/server/server-karaf/target/ -name *.deb | xargs -I {} mv {} ${artifactDir}/${debFileName}
	"""

	// create management template
	sh """
		set +x
		ssh root@gw.intra.lan <<- EOF
		set -e
		
		/apps/bin/subutai destroy management
		/apps/bin/subutai clone openjre8 management
		/bin/sleep 5
		/bin/cp /mnt/lib/lxc/jenkins/rootfs/${artifactDir}/${debFileName} /mnt/lib/lxc/management/rootfs/tmp/
		/apps/bin/lxc-attach -n management -- apt-get update
		/apps/bin/lxc-attach -n management -- sync
		/apps/bin/lxc-attach -n management -- apt-get -y --force-yes install --only-upgrade procps
		/apps/bin/lxc-attach -n management -- apt-get -y --force-yes install --only-upgrade udev
		/apps/bin/lxc-attach -n management -- apt-get -y --force-yes install subutai-dnsmasq subutai-influxdb curl gorjun
		/apps/bin/lxc-attach -n management -- dpkg -i /tmp/${debFileName}
		/apps/bin/lxc-attach -n management -- sync
		/bin/rm /mnt/lib/lxc/management/rootfs/tmp/${debFileName}
		/apps/bin/subutai export management -v ${artifactVersion}-${env.BRANCH_NAME}

		mv /mnt/lib/lxc/tmpdir/management-subutai-template_${artifactVersion}-${env.BRANCH_NAME}_amd64.tar.gz /mnt/lib/lxc/jenkins/rootfs/${artifactDir}
	EOF"""

	stage("Update management on test node")
	// Deploy builded template to remore test-server

	// destroy existing management template on test node
	sh """
		set +x
		ssh root@${env.SS_TEST_NODE} <<- EOF
		set -e
		subutai destroy management
		rm /mnt/lib/lxc/tmpdir/management-subutai-template_*
	EOF"""

	// copy generated management template on test node
	sh """
		set +x
		scp ${artifactDir}/management-subutai-template_${artifactVersion}-${env.BRANCH_NAME}_amd64.tar.gz root@${env.SS_TEST_NODE}:/mnt/lib/lxc/tmpdir
	"""

	// install genetared management template
	sh """
		set +x
		ssh root@${env.SS_TEST_NODE} <<- EOF
		set -e
		if [[ "\$(subutai update rh -c || true)" == '*No update is available*' ]]; then subutai update rh; fi
		echo -e '[template]\nbranch = ${env.BRANCH_NAME}' > /var/lib/apps/subutai/current/agent.gcfg
		echo -e '[cdn]\nbranch = cdn.local' >> /var/lib/apps/subutai/current/agent.gcfg
		echo y | subutai import management
		sed -i -e 's/cdn.local/cdn.subut.ai/g' /mnt/lib/lxc/management/rootfs/etc/apt/sources.list.d/subutai-repo.list
	EOF"""

	// wait until SS starts
	sh """
		set +x
		echo "Waiting SS"
		while [ \$(curl -k -s -o /dev/null -w %{http_code} 'https://${env.SS_TEST_NODE}:8443/rest/v1/peer/ready') != "200" ]; do
			sleep 5
		done
	"""


	stage("Integration tests")
	// Run Serenity Tests

	git url: "https://github.com/subutai-io/playbooks.git"
	sh """
		set +x
		./run_tests_qa.sh -m ${env.SS_TEST_NODE}
		./run_tests_qa.sh -s all
		${mvnHome}/bin/mvn integration-test -Dwebdriver.firefox.profile=src/test/resources/profilePgpFF
		${mvnHome}/bin/mvn serenity:aggregate
	"""

	stage("Deploy artifacts on kurjun")
	// Deploy builded and tested artifacts to cdn

	// cdn auth creadentials 
	String url = "https://eu0.cdn.subut.ai:8338/kurjun/rest"
	String user = "jenkins"
	def authID = sh (script: """
		set +x
		curl -s -k ${url}/auth/token?user=${user} | gpg --clearsign --no-tty
		""", returnStdout: true)
	def token = sh (script: """
		set +x
		curl -s -k -Fmessage=\"${authID}\" -Fuser=${user} ${url}/auth/token
		""", returnStdout: true)

	// upload artifacts on cdn
	// upload deb
	String responseDeb = sh (script: """
		set +x
		curl -s -k https://eu0.cdn.subut.ai:8338/kurjun/rest/apt/info?name=${debFileName}
		""", returnStdout: true)
	sh """
		set +x
		curl -s -k -Ffile=@${artifactDir}/${debFileName} -Ftoken=${token} ${url}/apt/upload
	"""
	// def signatureDeb = sh (script: "curl -s -k -Ffile=@${artifactDir}/${debFileName} -Ftoken=${token} ${url}/apt/upload | gpg --clearsign --no-tty", returnStdout: true)
	// sh "curl -s -k -Ftoken=${token} -Fsignature=\"${signatureDeb}\" ${url}/auth/sign"

	// delete old deb
	if (responseDeb != "Not found") {
		def jsonDeb = jsonParse(responseDeb)	
		sh """
			set +x
			curl -s -k -X DELETE ${url}/apt/delete?id=${jsonDeb["id"]}'&'token=${token}
		"""
	}

	// upload template
	String responseTemplate = sh (script: """
		set +x
		curl -s -k https://eu0.cdn.subut.ai:8338/kurjun/rest/template/info?name=${templateFileName}
		""", returnStdout: true)
	def signatureTemplate = sh (script: """
		set +x
		curl -s -k -Ffile=@${artifactDir}/${templateFileName} -Ftoken=${token} ${url}/template/upload | gpg --clearsign --no-tty
		""", returnStdout: true)
	sh """
		set +x
		curl -s -k -Ftoken=${token} -Fsignature=\"${signatureTemplate}\" ${url}/auth/sign
	"""

	// delete old template
	if (responseTemplate != "Not found") {
		def jsonTemplate = jsonParse(responseTemplate)
		sh """
			set +x
			curl -s -k -X DELETE ${url}/template/delete?id=${jsonTemplate["id"]}'&'token=${token}
		"""
	}
	} catch (e) { 
		currentBuild.result = "FAILED"
		throw e
	} finally {
		// Success or failure, always send notifications
		notifyBuild(currentBuild.result)
	}
}

def getVersionFromPom(pom) {
	def matcher = readFile(pom) =~ '<version>(.+)</version>'
	matcher ? matcher[1][1] : null
}

def String getVersion(pom) {
	def pomver = getVersionFromPom(pom)
	def ver = sh (script: "/bin/echo ${pomver} | cut -d '-' -f 1", returnStdout: true)
	return "${ver}".trim()
}

@NonCPS
def jsonParse(def json) {
    new groovy.json.JsonSlurperClassic().parseText(json)
}

// https://jenkins.io/blog/2016/07/18/pipline-notifications/
def notifyBuild(String buildStatus = 'STARTED') {
  // build status of null means successful
  buildStatus =  buildStatus ?: 'SUCCESSFUL'

  // Default values
  def colorName = 'RED'
  def colorCode = '#FF0000'
  def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
  def summary = "${subject} (${env.BUILD_URL})"
  def details = """<p>STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
    <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>"""

  // Override default values based on build status
  if (buildStatus == 'STARTED') {
    color = 'YELLOW'
    colorCode = '#FFFF00'
  } else if (buildStatus == 'SUCCESSFUL') {
    color = 'GREEN'
    colorCode = '#00FF00'
  } else {
    color = 'RED'
    colorCode = '#FF0000'
  }
  // Get token
  def slackToken = getSlackToken('ss-bots-slack-token')
  // Send notifications
  slackSend (color: colorCode, message: summary, teamDomain: 'subutai-io', token: "${slackToken}")
}

// get slack token from global jenkins credentials store
@NonCPS
def getSlackToken(String slackCredentialsId){
	// id is ID of creadentials
	def jenkins_creds = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0]

	String found_slack_token = jenkins_creds.getStore().getDomains().findResult { domain ->
	  jenkins_creds.getCredentials(domain).findResult { credential ->
	    if(slackCredentialsId.equals(credential.id)) {
	      credential.getSecret()
	    }
	  }
	}
	return found_slack_token
}