@Library('pipeline-library') _

abis = ['armeabi-v7a']

// Sets up the environment to build Android projects.
node("android-build-jdk8") {

    stage('Checkout SCM') { checkout scm }

    stage('Compile Library') {
        sh './gradlew :library:assembleRelease'
        archiveArtifacts '**/*.aar'
    }

    stage('Compile Application') {
        withCredentials([
                file(credentialsId: 'keystoreFileAndroid', variable: 'KEYSTORE_FILE'),
                string(credentialsId: 'keystorePasswordAndroid', variable: 'KEYSTORE_PASSWORD'),
                string(credentialsId: 'keystoreAliasSample', variable: 'KEYSTORE_ALIAS'),
                string(credentialsId: 'aliasPasswordSample', variable: 'ALIAS_PASSWORD'),
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
