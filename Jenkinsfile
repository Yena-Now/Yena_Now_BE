pipeline {
    agent any

    tools {
        jdk 'jdk17'
    }

    environment {
        DOCKER_IMAGE_NAME = "yena_now_be"
        DEPLOY_SERVER = "${DEPLOY_SERVER_USER}@${DEPLOY_SERVER_IP}"
    }

    stages {
        stage('Checkout') {
            steps {
                echo '1. GitLab에서 소스 코드 가져오기'
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                echo '2. Gradle로 프로젝트 빌드 및 테스트'
                sh './gradlew clean build -x test'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '3. Docker 이미지 빌드'
                sh "docker build -t ${DOCKER_IMAGE_NAME}:${BUILD_NUMBER} ."
            }
        }

        stage('Deploy to Production') {
            steps {
                script {
                    sh """
