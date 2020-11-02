@Library('pipeline-library') _

import com.softbankrobotics.pipeline.ArchiveType
import com.softbankrobotics.pipeline.VersionType

final def MASTER_BRANCH = 'master'

node("android-build-jdk8") {

    stage('Checkout SCM') { checkout scm }

    stage('Compile') {
        sh "./gradlew clean -PABIS=armeabi-v7a"
        sh "./gradlew :library:assembleDebug -PABIS=armeabi-v7a"
        sh "./gradlew :library:assembleRelease -PABIS=armeabi-v7a"
    }

    stage('Archive AAR') { archiveArtifacts '**/*.aar' }
}
