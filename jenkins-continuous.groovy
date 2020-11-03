@Library('pipeline-library') _

node("android-build-jdk8") {

    stage('Checkout SCM') { checkout scm }

    stage('Compile') {
        sh "./gradlew clean -PABIS=\"armeabi-v7a\""
        sh "./gradlew :library:assembleRelease -PABIS=\"armeabi-v7a\""
    }

    stage('Archive AAR') { archiveArtifacts '**/*.aar' }
}
