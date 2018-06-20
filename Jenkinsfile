#!groovy

notifyBuildDetails = ""
cdnHost = ""
aptHost = ""

node() {
    // Send job started notifications
    try {
        notifyBuild('STARTED')
        deleteDir()
        def mvnHome = tool 'M3'
        def workspace = pwd()

        stage("Build management deb package")
        // Use maven to to build deb and template files of management
        notifyBuildDetails = "\nFailed Step - Build management deb package"

        checkout scm
        def artifactVersion = getVersion("management/pom.xml")
        String debFileName = "management-${env.BRANCH_NAME}.deb"

        // declare hub address
        switch (env.BRANCH_NAME) {
            case ~/master/: cdnHost = "masterbazaar.subutai.io"; break;
            case ~/dev/: cdnHost = "devbazaar.subutai.io"; break;
            case ~/sysnet/: cdnHost = "devbazaar.subutai.io"; break;
            default: cdnHost = "bazaar.subutai.io"
        }

        switch (env.BRANCH_NAME) {
            case ~/master/: aptHost = "mastercdn.subutai.io"; break;
            case ~/dev/: aptHost = "devcdn.subutai.io"; break;
            case ~/sysnet/: aptHost = "sysnetcdn.subutai.io"; break;
            default: aptHost = "cdn.subutai.io"
        }

        // build deb
        sh """
		cd management
		sed 's/export HUB_IP=.*/export HUB_IP=${cdnHost}/g' -i server/server-karaf/src/main/assembly/bin/setenv
        git checkout ${env.BRANCH_NAME}
		if [[ "${env.BRANCH_NAME}" == "dev" ]] || [[ "${env.BRANCH_NAME}" == "hotfix-"* ]]; then
			${mvnHome}/bin/mvn clean install -P deb -Dgit.branch=${env.BRANCH_NAME}
		else 
			${mvnHome}/bin/mvn clean install -Dmaven.test.skip=true -P deb -Dgit.branch=${env.BRANCH_NAME}
		fi		
        branch=`git symbolic-ref --short HEAD` && echo "Branch is \$branch"
        find ${workspace}/management/server/server-karaf/target/ -name *.deb | xargs -I {} cp {} ${workspace}/${debFileName}
	    """

        // CDN auth credentials
        String user = "jenkins@optimal-dynamics.com"
        String fingerprint = "877B586E74F170BC4CF6ECABB971E2AC63D23DC9"
        def authId = sh(script: """
            curl -s https://${cdnHost}/rest/v1/cdn/token?fingerprint=${fingerprint}
            """, returnStdout: true)
        authId = authId.trim()
        def sign = sh(script: """
            echo ${authId} | gpg --clearsign -u ${user}
            """, returnStdout: true)
        sign = sign.trim()
        def token = sh(script: """
            curl -s --data-urlencode "request=${sign}"  https://${cdnHost}/rest/v1/cdn/token
            """, returnStdout: true)
        token = token.trim()
        stage("Build management template")
        notifyBuildDetails = "\nFailed Step - Build management template"

        // Start MNG-RH Lock
        lock('deb') {

            // create management template
            sh """
			set +x
            ssh admin@172.31.0.253 <<- EOF
			set -e
		    echo ${token}
            sudo sed 's/APT =.*/APT = ${aptHost}/gI' -i /etc/subutai/agent.conf
            sudo sed 's/URL =.*/URL = ${cdnHost}/gI' -i /etc/subutai/agent.conf
            sudo sed 's/SshJumpServer =.*/SshJumpServer = ${aptHost}/gI' -i /etc/subutai/agent.conf
			sudo subutai destroy management
            sudo subutai clone debian-stretch management
			/bin/sleep 20
			scp ubuntu@${env.master_rh}:/mnt/lib/lxc/jenkins${workspace}/${debFileName} /var/lib/lxc/management/rootfs/tmp/
			sudo subutai attach management "apt-get update && apt-get install dirmngr -y"
            sudo cp /opt/key/cdn-pub.key /var/lib/lxc/management/rootfs/tmp/
            sudo subutai attach management "gpg --import /tmp/cdn-pub.key"
            sudo subutai attach management "gpg --export --armor 80260C65A4D79BC8 | apt-key add"
			sudo subutai attach management "echo 'deb http://${aptHost}:8080/kurjun/rest/apt /' > /etc/apt/sources.list.d/subutai-repo.list"
            sudo subutai attach management "apt-get update"
			sudo subutai attach management "sync"
			sudo subutai attach management "apt-get -y install curl influxdb influxdb-certs openjdk-8-jre"
			sudo subutai attach management "wget -q 'https://${aptHost}:8338/kurjun/rest/raw/get?owner=subutai&name=influxdb.conf' -O /etc/influxdb/influxdb.conf"
			sudo subutai attach management "dpkg -i /tmp/${debFileName}"
			sudo subutai attach management "systemctl stop management"
			sudo subutai attach management "rm -rf /opt/subutai-mng/keystores/"
			sudo subutai attach management "apt-get clean"
			sudo subutai attach management "sync"
            sudo subutai attach management "sed -i "s/weekly/dayly/g" /etc/logrotate.d/rsyslog"
            sudo subutai attach management "sed -i "/delaycompress/d" /etc/logrotate.d/rsyslog"
            sudo subutai attach management "sed -i "s/7/3/g" /etc/logrotate.d/rsyslog"
            sudo subutai attach management "sed -i "s/4/3/g" /etc/logrotate.d/rsyslog"
  			sudo rm /var/lib/lxc/management/rootfs/tmp/${debFileName}
            echo "Using CDN token ${token}"  
            echo "Template version is ${artifactVersion}"
			sudo subutai export management -v ${artifactVersion} --local -t ${token} |  grep -Po "{.*}" | tr -d '\\\\' > /tmp/template.json
            scp /tmp/template.json ipfs-kg:/tmp
            scp /var/cache/subutai/management-subutai-template_${artifactVersion}_amd64.tar.gz ipfs-kg:/tmp
			EOF"""

            sh """
            ssh ipfs-kg "ipfs add -Q /tmp/management-subutai-template_${artifactVersion}_amd64.tar.gz > /tmp/ipfs.hash"
            """

            sh """
            scp ipfs-kg:/tmp/ipfs.hash /tmp/
            scp ipfs-kg:/tmp/template.json /tmp/
            """

            String NEW_ID = sh(script: """
            cat /tmp/ipfs.hash
            """, returnStdout: true)
            NEW_ID = NEW_ID.trim()

            //remove existing template metadata
            String OLD_ID = sh(script: """
            var=\$(curl -s https://${cdnHost}/rest/v1/cdn/template?name=management&verified=true) ; if [[ \$var != "Template not found" ]]; then echo \$var | grep -Po '"id":"\\K([a-zA-Z0-9]+)' ; else echo \$var; fi
            """, returnStdout: true)
            OLD_ID = OLD_ID.trim()

            sh """
            echo "OLD ID: ${OLD_ID}"
            if [[ "${OLD_ID}" != "Template not found" ]]; then
                curl -X DELETE "https://${cdnHost}/rest/v1/cdn/template?token=${token}&id=${OLD_ID}"
            fi
            """

            sh """
            echo "NEW ID: ${NEW_ID}"
            sed -i 's/"id":""/"id":"${NEW_ID}"/g' /tmp/template.json
            template=`cat /tmp/template.json` && curl -d "token=${token}&template=\$template" https://${cdnHost}/rest/v1/cdn/templates
            """
        }

        stash includes: "management-*.deb", name: 'deb'

        if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'dev' || env.BRANCH_NAME == 'sysnet') {
            stage("Upload to CDN")
            notifyBuildDetails = "\nFailed Step - Upload to CDN"
            deleteDir()

            unstash 'deb'

            // CDN auth credentials
            String kurjunUser = "jenkins"
            String kurjunUserEmail = "jenkins@subut.ai"
            def kurjunID = sh(script: """
			set +x
			curl -s -k https://${aptHost}:8338/kurjun/rest/auth/token?user=${kurjunUser} | gpg --clearsign --no-tty -u ${kurjunUserEmail}
			""", returnStdout: true)
            def kurjunToken = sh(script: """
			set +x
			curl -s -k -Fmessage=\"${kurjunID}\" -Fuser=${kurjunUser} https://${aptHost}:8338/kurjun/rest/auth/token
			""", returnStdout: true)


            String responseDeb = sh(script: """
		    set +x
		    curl -s -k https://${aptHost}:8338/kurjun/rest/apt/info?name=${debFileName}
		    """, returnStdout: true)

            // delete old deb
            if (responseDeb != "Not found") {
                def jsonDeb = jsonParse(responseDeb)
                sh """
			    set +x
			    curl -s -k -X DELETE https://${aptHost}:8338/kurjun/rest/apt/delete?id=${jsonDeb[0]["id"]}'&'token=${kurjunToken}
		        """
            }


            sh """
            echo "Uploading file ${debFileName}"
            """

            sh """
			set +x
            echo "${token} and ${aptHost} and ${debFileName}"
			curl -sk -H "token: ${kurjunToken}" -Ffile=@${debFileName} -Ftoken=${kurjunToken} "https://${aptHost}:8338/kurjun/rest/apt/upload"
            """
            sh """
			set +x
            curl -k -H "token: ${kurjunToken}" "https://${aptHost}:8338/kurjun/rest/apt/generate" 
		    """
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
