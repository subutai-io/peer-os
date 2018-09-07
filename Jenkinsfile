#!groovy

notifyBuildDetails = ""
cdnHost = ""
jumpServer = ""
aptRepo = ""

try {
    notifyBuild('STARTED')
    String debFileName = "management-${env.BRANCH_NAME}.deb"
    
    switch (env.BRANCH_NAME) {
        case ~/master/: cdnHost = "masterbazaar.subutai.io"; break;
        case ~/dev/: cdnHost = "devbazaar.subutai.io"; break;
        case ~/sysnet/: cdnHost = "devbazaar.subutai.io"; break;
        default: cdnHost = "bazaar.subutai.io"
    }

    switch (env.BRANCH_NAME) {
        case ~/master/: jumpServer = "mastertun.subutai.io"; break;
        case ~/dev/: jumpServer = "devtun.subutai.io"; break;
        case ~/sysnet/: jumpServer = "devtun.subutai.io"; break;
        default: jumpServer = "tun.subutai.io"
    }

    switch (env.BRANCH_NAME) {
        case ~/master/: aptRepo = "master"; break;
        case ~/dev/: aptRepo = "dev"; break;
        case ~/sysnet/: aptRepo = "dev"; break;
        default: aptRepo = "prod"
    }

    node("management") {
        deleteDir()
        def mvnHome = tool 'M3'
        def workspace = pwd()

        stage("Build management deb package") 
        // Use maven to to build deb and template files of management
        notifyBuildDetails = "\nFailed Step - Build management deb package"

        checkout scm
        def artifactVersion = getVersion("management/pom.xml")

        // build deb
        sh """
		cd management
        git checkout ${env.BRANCH_NAME}
		sed 's/export BAZAAR_IP=.*/export BAZAAR_IP=${cdnHost}/g' -i server/server-karaf/src/main/assembly/bin/setenv
		if [[ "${env.BRANCH_NAME}" == "dev" ]]; then
			${mvnHome}/bin/mvn clean install -P deb -Dgit.branch=${env.BRANCH_NAME}
		else 
			${mvnHome}/bin/mvn clean install -Dmaven.test.skip=true -P deb -Dgit.branch=${env.BRANCH_NAME}
		fi		
        branch=`git symbolic-ref --short HEAD` && echo "Branch is \$branch"
        find ${workspace}/management/server/server-karaf/target/ -name *.deb | xargs -I {} cp {} ${workspace}/${debFileName}

        """
        stash includes: "management-*.deb", name: 'deb'

        // CDN auth credentials
        String user = "jenkins@optimal-dynamics.com"
        String fingerprint = "877B586E74F170BC4CF6ECABB971E2AC63D23DC9"
        def authId = sh(script: """
            curl -s https://${cdnHost}/rest/v1/cdn/token?fingerprint=${fingerprint}
            """, returnStdout: true)
        authId = authId.trim()
        def sign = sh(script: """
            echo ${authId} | gpg1 --clearsign -u ${user}
            """, returnStdout: true)
        sign = sign.trim()
        String token = sh(script: """
            curl -s --data-urlencode "request=${sign}"  https://${cdnHost}/rest/v1/cdn/token
            """, returnStdout: true)
        token = token.trim()     
       
        
        stage("Build management template")
        notifyBuildDetails = "\nFailed Step - Build management template"
                
        // create management template

            sh """
		   	set +x
            set -e
		    sudo sed 's/URL =.*/URL = ${cdnHost}/gI' -i /etc/subutai/agent.conf
            sudo sed 's/SshJumpServer =.*/SshJumpServer = ${jumpServer}/gI' -i /etc/subutai/agent.conf
            set +e
			sudo subutai destroy management
			set -e
            sudo subutai clone debian-stretch management
			/bin/sleep 20
			cp ${workspace}/${debFileName} /var/lib/lxc/management/rootfs/tmp/
			sudo subutai attach management "apt-get update && apt-get install dirmngr -y"
			sudo subutai attach management "apt-key adv --recv-keys --keyserver keyserver.ubuntu.com C6B2AC7FBEB649F1"
			sudo subutai attach management "echo 'deb http://deb.subutai.io/subutai ${aptRepo} main' > /etc/apt/sources.list.d/subutai-repo.list"
            sudo subutai attach management "apt-get update"
			sudo subutai attach management "sync"
			sudo subutai attach management "apt-get -y install curl influxdb influxdb-certs openjdk-8-jre"
			sudo cp ~/influxdb.conf /var/lib/lxc/management/rootfs/etc/influxdb/influxdb.conf
			sudo subutai attach management "dpkg -i /tmp/${debFileName}"
			sudo subutai attach management "systemctl stop management"
			sudo subutai attach management "rm -rf /opt/subutai-mng/keystores/"
            sudo subutai attach management "rm -rf /opt/subutai-mng/db"
			sudo subutai attach management "apt-get clean"
			sudo subutai attach management "sync"
            sudo subutai attach management "sed -i "s/weekly/dayly/g" /etc/logrotate.d/rsyslog"
            sudo subutai attach management "sed -i "/delaycompress/d" /etc/logrotate.d/rsyslog"
            sudo subutai attach management "sed -i "s/7/3/g" /etc/logrotate.d/rsyslog"
            sudo subutai attach management "sed -i "s/4/3/g" /etc/logrotate.d/rsyslog"
  			sudo rm /var/lib/lxc/management/rootfs/tmp/${debFileName}
            echo "Using CDN token ${token}"  
            echo "Template version is ${artifactVersion}"
            """
            //remove existing template metadata
            String OLD_ID = sh(script: """
            var=\$(curl -s https://${cdnHost}/rest/v1/cdn/template?name=management) ; if [[ \$var != "Template not found" ]]; then echo \$var | grep -Po '"id"\\s*:\\s*"\\K([a-zA-Z0-9]+)' ; else echo \$var; fi
            """, returnStdout: true)
            OLD_ID = OLD_ID.trim()

            sh """
            echo "OLD ID: ${OLD_ID}"
            if [[ "${OLD_ID}" != "Template not found" ]]; then
                curl -X DELETE "https://${cdnHost}/rest/v1/cdn/template?token=${token}&id=${OLD_ID}"
            fi
            """

            // Exporting template
            sh """
            set -e
			sudo subutai export management -r "${artifactVersion}" --local --token "${token}"
            """
                        
        stage("Upload management template to IPFS node")
        notifyBuildDetails = "\nFailed Step - Upload management template to IPFS node"

            //TODO upload to CDN

            sh """
            set +e
            cd /var/cache/subutai/
            curl -sk -H "token: ${token}" -Ffile=@management-subutai-template_${artifactVersion}_amd64.tar.gz -Ftoken=${token} -X POST "https://${cdnHost}/rest/v1/cdn/uploadTemplate"

            """

        if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'dev' || env.BRANCH_NAME == 'sysnet') {
            stage("Upload to REPO") {
            notifyBuildDetails = "\nFailed Step - Upload to Repo"
            deleteDir()

            unstash 'deb'

            //copy deb to repo
            sh """
            touch uploading_management
            scp uploading_management ${debFileName} dak@debup.subutai.io:incoming/${env.BRANCH_NAME}/
            ssh dak@debup.subutai.io sh /var/reprepro/scripts/scan-incoming.sh ${env.BRANCH_NAME} management
            """
            }
        }

    }
} catch (e) {
        currentBuild.result = "FAILED"
        throw e
    } finally {
        // Success or failure, always send notifications
        notifyBuild(currentBuild.result, notifyBuildDetails)
    }


def getVersionFromPom(pom) {
    def matcher = readFile(pom) =~ '<version>(.+)</version>'
    matcher ? matcher[1][1] : null
}

def String getVersion(pom) {
    def pomver = getVersionFromPom(pom)
    def ver = sh(script: "/bin/echo ${pomver} | cut -d '-' -f 1", returnStdout: true)
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
    def slackToken = getSlackToken('ss-bots')
    // Send notifications
    slackSend(color: colorCode, message: summary, teamDomain: 'optdyn', token: "${slackToken}")
}

// get slack token from global jenkins credentials store
@NonCPS
def getSlackToken(String slackCredentialsId) {
    // id is ID of creadentials
    def jenkins_creds = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0]

    String found_slack_token = jenkins_creds.getStore().getDomains().findResult { domain ->
        jenkins_creds.getCredentials(domain).findResult { credential ->
            if (slackCredentialsId.equals(credential.id)) {
                credential.getSecret()
            }
        }
    }
    return found_slack_token
}
