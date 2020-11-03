@Library('pipeline-library') _

node("android-build-jdk8") {

    stage('Checkout SCM') { checkout scm }

    stage('Compile') {
        sh "./gradlew clean -PABIS=\"x86_64\""
        sh "./gradlew :library:testDebug -PABIS=\"x86_64\""
    }
}

