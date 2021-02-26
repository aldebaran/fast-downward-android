@Library('pipeline-library') _

abis = ['armeabi-v7a', 'x86', 'x86_64']

// Sets up the environment to build Android projects.
node("android-build-jdk8") {

    stage('Checkout SCM') { checkout scm }

    stage('Compile Library') {
        sh './gradlew :library:assembleRelease'
        archiveArtifacts '**/*.aar'
        withCredentials([
                usernamePassword(credentialsId: 'nexusDeployerAccount',
                        passwordVariable: 'NEXUS_PASSWORD',
                        usernameVariable: 'NEXUS_USER')
        ]) {
            if (env.BRANCH_NAME == "develop") {
                BUILD_TYPE = "SNAPSHOT"
            } else {
                BUILD_TYPE = "RELEASE"
            }
            sh "./gradlew -DNEXUS_PASSWORD=$NEXUS_PASSWORD -DNEXUS_USER=$NEXUS_USER -DBUILD=$BUILD_TYPE :library:uploadArchives"
        }
    }

    stage('Compile Application') {
        withCredentials([
                file(credentialsId: 'keystoreFileAndroid', variable: 'KEYSTORE_FILE'),
                string(credentialsId: 'keystorePasswordAndroid', variable: 'KEYSTORE_PASSWORD'),
                string(credentialsId: 'keystoreAliasSample', variable: 'KEYSTORE_ALIAS'),
                string(credentialsId: 'aliasPasswordSample', variable: 'ALIAS_PASSWORD'),

                string(credentialsId: 'fastDownwardFirebaseProjectID', variable: 'fastDownwardFirebaseProjectID'),
                string(credentialsId: 'fastDownwardFirebaseGoogleAppID', variable: 'fastDownwardFirebaseGoogleAppID'),
                string(credentialsId: 'fastDownwardFirebaseWebClientID', variable: 'fastDownwardFirebaseWebClientID'),
                string(credentialsId: 'fastDownwardFirebaseGCMSenderID', variable: 'fastDownwardFirebaseGCMSenderID'),
                string(credentialsId: 'fastDownwardFirebaseGoogleAPIKey', variable: 'fastDownwardFirebaseGoogleAPIKey'),
                string(credentialsId: 'fastDownwardFirebaseCrashlyticsAPIKey', variable: 'fastDownwardFirebaseCrashlyticsAPIKey'),
        ]) {
            for (abi in abis) {
                echo "Compiling app for ($abi)..."
                sh "./gradlew :app:assembleRelease -PABIS=$abi"

                // KLUDGE: by building it twice, we make sure the asset python-modules.zip is found
                echo "Recompiling app for ($abi)..."
                sh "./gradlew :app:assembleRelease -PABIS=$abi"

                echo "Archiving app for ($abi)..."
                archiveArtifacts "**/*-$abi-*.apk"
            }
        }
    }
}
