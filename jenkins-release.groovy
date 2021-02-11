@Library('pipeline-library')

extraBuildOptions = "" // ABIs can be specified using \"-PABIS=...;...\"

// Sets up the environment to build Android projects.
node("android-build-jdk8") {

    stage('Checkout SCM') { checkout scm }

    stage('Compile') {
        sh "./gradlew assembleRelease $extraBuildOptions"
    }

    stage('Upload AAR to Nexus') {
        if (env.BRANCH_NAME == "develop") {
            BUILD_TYPE = "SNAPSHOT"
        } else {
            BUILD_TYPE = "RELEASE"
        }
        echo "BUILD_TYPE=$BUILD_TYPE"

        echo 'Entering credentials context...'
        withCredentials([
                usernamePassword(credentialsId: 'nexusDeployerAccount',
                        passwordVariable: 'NEXUS_PASSWORD',
                        usernameVariable: 'NEXUS_USER')
        ]) {
            echo 'Now in credentials context.'
            sh "./gradlew -DNEXUS_PASSWORD=$NEXUS_PASSWORD -DNEXUS_USER=$NEXUS_USER -DBUILD=$BUILD_TYPE :library:uploadArchives $extraBuildOptions"
        }
    }

    stage('Archive AAR') {
        archiveArtifacts '**/*.aar'
    }
}
