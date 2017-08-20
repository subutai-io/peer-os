#!groovy

// Configuring builder:
// Manage Jenkins -> Global Tool Configuration -> Maven installations -> Add Maven:
// Name - M3
// MAVEN_HOME - path to Maven3 home dir
//
// Manage Jenkins -> Configure System -> Environment variables
// SS_TEST_NODE_CORE16 - ip of SS node for smoke tests 
//
// Approve methods:
// in build job log you will see 
// Scripts not permitted to use new <method>
// Goto http://jenkins.domain/scriptApproval/
// and approve methods denied methods
//
// TODO:
// https://jenkins.io/doc/pipeline/steps/ssh-agent/#sshagent-ssh-agent

import groovy.json.JsonSlurperClassic

notifyBuildDetails = ""
hubIp = ""
cdnHost = ""

node() {
	// Send job started notifications
	try {
	notifyBuild('STARTED')

	def mvnHome = tool 'M3'
	def workspace = pwd()
	
	stage("Build management deb/template")
	// Use maven to to build deb and template files of management
	notifyBuildDetails = "\nFailed Step - Build management deb/template"

	checkout scm
	def artifactVersion = getVersion("management/pom.xml")
	String debFileName = "management-${env.BRANCH_NAME}.deb"
	String templateFileName = "management-subutai-template_${artifactVersion}-${env.BRANCH_NAME}_amd64.tar.gz"

	commitId = sh (script: "git rev-parse HEAD", returnStdout: true)
	String serenityReportDir = "/var/lib/jenkins/www/serenity/${commitId}"

	// declare hub address
	switch (env.BRANCH_NAME) {
		case ~/master/: hubIp = "stage.subut.ai"; break;
		case ~/dev/: hubIp = "dev.subut.ai"; break;
		default: hubIp = "hub.subut.ai"
	}

	// String url = "https://eu0.cdn.subut.ai:8338/kurjun/rest"
	switch (env.BRANCH_NAME) {
		case ~/master/: cdnHost = "stagecdn.subut.ai"; break;
		case ~/dev/: cdnHost = "devcdn.subut.ai"; break;
		default: cdnHost = "cdn.subut.ai"
	}

	// build deb
	sh """
		cd management
		export GIT_BRANCH=${env.BRANCH_NAME}
		sed 's/export HUB_IP=.*/export HUB_IP=${hubIp}/g' -i server/server-karaf/src/main/assembly/bin/setenv

		if [[ "${env.BRANCH_NAME}" == "dev" ]]; then
			${mvnHome}/bin/mvn clean install -P deb -Dgit.branch=${env.BRANCH_NAME} sonar:sonar -Dsonar.branch=${env.BRANCH_NAME}
		elif [[ "${env.BRANCH_NAME}" == "hotfix-"* ]]; then
			${mvnHome}/bin/mvn clean install -P deb -Dgit.branch=${env.BRANCH_NAME}
		else 
			${mvnHome}/bin/mvn clean install -Dmaven.test.skip=true -P deb -Dgit.branch=${env.BRANCH_NAME}
		fi		
		find ${workspace}/management/server/server-karaf/target/ -name *.deb | xargs -I {} mv {} ${workspace}/${debFileName}
	"""
	// Start MNG-RH Lock
	lock('rh-node') {
		// create management template
		sh """
			set +x
			ssh root@gw.intra.lan <<- EOF
			set -e
			
			/apps/bin/subutai destroy management
			/apps/bin/subutai clone openjre16 management
			/bin/sleep 20
			/bin/cp /mnt/lib/lxc/jenkins/${workspace}/${debFileName} /mnt/lib/lxc/management/rootfs/tmp/
			/apps/bin/lxc-attach -n management -- sh -c 'echo "deb http://${cdnHost}:8080/kurjun/rest/apt /" > /etc/apt/sources.list.d/subutai-repo.list'
			/apps/bin/lxc-attach -n management -- apt-get update
			/apps/bin/lxc-attach -n management -- sync
			/apps/bin/lxc-attach -n management -- apt-get -y --allow-unauthenticated install curl gorjun-local influxdb influxdb-certs
			/apps/bin/lxc-attach -n management -- wget -q 'https://cdn.subut.ai:8338/kurjun/rest/raw/get?owner=subutai&name=influxdb.conf' -O /etc/influxdb/influxdb.conf
			/apps/bin/lxc-attach -n management -- dpkg -i /tmp/${debFileName}
			/apps/bin/lxc-attach -n management -- mkdir -p /opt/gorjun/etc/
			/apps/bin/lxc-attach -n management -- sh -c 'echo "[CDN]\nnode = ${cdnHost}:8338" > /opt/gorjun/etc/gorjun.gcfg'
			/apps/bin/lxc-attach -n management -- systemctl stop management
			/apps/bin/lxc-attach -n management -- rm -rf /opt/subutai-mng/keystores/
			
			/apps/bin/lxc-attach -n management -- sync
			/bin/rm /mnt/lib/lxc/management/rootfs/tmp/${debFileName}
			/apps/bin/subutai export management -v ${artifactVersion}-${env.BRANCH_NAME}

			mv /mnt/lib/lxc/tmpdir/management-subutai-template_${artifactVersion}-${env.BRANCH_NAME}_amd64.tar.gz /mnt/lib/lxc/jenkins/${workspace}
		EOF"""
	}

	/* stash p2p binary to use it in next node() */
	stash includes: "management-*.deb", name: 'deb'
	stash includes: "management-subutai-template*", name: 'template'

	stage("Update management on test node")
	// Deploy built template to remore test-server
	notifyBuildDetails = "\nFailed on Stage - Update management on test node"

	// Start Test-Peer Lock
	if (env.BRANCH_NAME == 'dev' || env.BRANCH_NAME ==~ /hotfix-.*/ || env.BRANCH_NAME == 'jenkinsfile') {
		lock('test-node-core16') {
			// destroy existing management template on test node and install latest available snap
			sh """
				set +x
				ssh root@${env.SS_TEST_NODE_CORE16} <<- EOF
				set -e
				subutai-dev destroy everything
				if test -f /var/snap/subutai-dev/current/p2p.save; then rm /var/snap/subutai-dev/current/p2p.save; fi
				find /var/snap/subutai-dev/common/lxc/tmpdir/ -maxdepth 1 -type f -name 'management-subutai-template_*' -delete
				cd /tmp
				find /tmp -maxdepth 1 -type f -name 'subutai-dev_*' -delete
				snap download subutai-dev --beta
				snap install --dangerous --devmode /tmp/subutai-dev_*.snap
			EOF"""

			// copy generated management template on test node
			sh """
				set +x
				scp ${workspace}/management-subutai-template_${artifactVersion}-${env.BRANCH_NAME}_amd64.tar.gz root@${env.SS_TEST_NODE_CORE16}:/var/snap/subutai-dev/common/lxc/tmpdir
			"""

			// install generated management template
			sh """
				set +x
				ssh root@${env.SS_TEST_NODE_CORE16} <<- EOF
				set -e
				sed 's/branch = .*/branch = ${env.BRANCH_NAME}/g' -i /var/snap/subutai-dev/current/agent.gcfg
				sed 's/URL =.*/URL = cdn.local/g' -i /var/snap/subutai-dev/current/agent.gcfg
				echo y | subutai-dev import management
				sed 's/cdn.local/devcdn.subut.ai/g' -i /var/snap/subutai-dev/common/lxc/management/rootfs/etc/apt/sources.list.d/subutai-repo.list
				sed 's/URL =.*/URL = devcdn.subut.ai/g' -i /var/snap/subutai-dev/current/agent.gcfg
			EOF"""

			/* wait until SS starts */
			timeout(time: 5, unit: 'MINUTES') {
				sh """
					set +x
					echo "Waiting SS"
					while [ \$(curl -k -s -o /dev/null -w %{http_code} 'https://${env.SS_TEST_NODE_CORE16}:8443/rest/v1/peer/ready') != "200" ]; do
						sleep 5
					done
				"""
			}

			stage("Integration tests")
			deleteDir()

			// Run Serenity Tests
			notifyBuildDetails = "\nFailed on Stage - Integration tests\nSerenity Tests Results:\n${env.JENKINS_URL}serenity/${commitId}"

			git url: "https://github.com/subutai-io/playbooks.git"
			sh """
				set +e
				./run_tests_qa.sh -m ${env.SS_TEST_NODE_CORE16}
				./run_tests_qa.sh -s all
				${mvnHome}/bin/mvn integration-test -Dwebdriver.firefox.profile=src/test/resources/profilePgpFF
				OUT=\$?
				${mvnHome}/bin/mvn serenity:aggregate
				cp -rl target/site/serenity ${serenityReportDir}
				if [ \$OUT -ne 0 ];then
					exit 1
				fi
			"""
		}
	}

	if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'dev') {
		stage("Deploy artifacts on kurjun")
		deleteDir()

		unstash 'deb'
		unstash 'template'

		// Deploy built and tested artifacts to cdn
		notifyBuildDetails = "\nFailed on Stage - Deploy artifacts on kurjun"

		// cdn auth creadentials 
		String user = "jenkins"
		def authID = sh (script: """
			set +x
			curl -s -k https://${cdnHost}:8338/kurjun/rest/auth/token?user=${user} | gpg --clearsign --no-tty
			""", returnStdout: true)
		def token = sh (script: """
			set +x
			curl -s -k -Fmessage=\"${authID}\" -Fuser=${user} https://${cdnHost}:8338/kurjun/rest/auth/token
			""", returnStdout: true)

		// upload artifacts on cdn
		// upload deb
		String responseDeb = sh (script: """
			set +x
			curl -s -k https://${cdnHost}:8338/kurjun/rest/apt/info?name=${debFileName}
			""", returnStdout: true)
		sh """
			set +x
			curl -s -k -Ffile=@${debFileName} -Ftoken=${token} https://${cdnHost}:8338/kurjun/rest/apt/upload
		"""
		// def signatureDeb = sh (script: "curl -s -k -Ffile=@${workspace}/${debFileName} -Ftoken=${token} https://${cdnHost}:8338/kurjun/rest/apt/upload | gpg --clearsign --no-tty", returnStdout: true)
		// sh "curl -s -k -Ftoken=${token} -Fsignature=\"${signatureDeb}\" https://${cdnHost}:8338/kurjun/rest/auth/sign"

		// delete old deb
		if (responseDeb != "Not found") {
			def jsonDeb = jsonParse(responseDeb)	
			sh """
				set +x
				curl -s -k -X DELETE https://${cdnHost}:8338/kurjun/rest/apt/delete?id=${jsonDeb[0]["id"]}'&'token=${token}
			"""
		}

		// upload template
		String responseTemplate = sh (script: """
			set +x
			curl -s -k https://${cdnHost}:8338/kurjun/rest/template/info?name=management'&'version=${env.BRANCH_NAME}
			""", returnStdout: true)
		def signatureTemplate = sh (script: """
			set +x
			curl -s -k -Ffile=@${templateFileName} -Ftoken=${token} https://${cdnHost}:8338/kurjun/rest/template/upload | gpg --clearsign --no-tty
			""", returnStdout: true)
		sh """
			set +x
			curl -s -k -Ftoken=${token} -Fsignature=\"${signatureTemplate}\" https://${cdnHost}:8338/kurjun/rest/auth/sign
		"""

		// delete old template
		if (responseTemplate != "Not found") {
			def jsonTemplate = jsonParse(responseTemplate)
			sh """
				set +x
				curl -s -k -X DELETE https://${cdnHost}:8338/kurjun/rest/template/delete?id=${jsonTemplate[0]["id"]}'&'token=${token}
			"""
		}
	}
	} catch (e) { 
		currentBuild.result = "FAILED"
		throw e
	} finally {
		// Success or failure, always send notifications
		notifyBuild(currentBuild.result, notifyBuildDetails)
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
def notifyBuild(String buildStatus = 'STARTED', String details = '') {
  // build status of null means successful
  buildStatus = buildStatus ?: 'SUCCESSFUL'

  // Default values
  def colorName = 'RED'
  def colorCode = '#FF0000'
  def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"  	
  def summary = "${subject} (${env.BUILD_URL})"

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
	summary = "${subject} (${env.BUILD_URL})${details}"
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
