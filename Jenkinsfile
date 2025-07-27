pipeline {
    agent any

    tools {
        jdk 'jdk17'
    }

    environment {
        DOCKER_IMAGE_NAME = "yena_now_be"
        DEPLOY_SERVER = "ubuntu@i13e203.p.ssafy.io"
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
                echo '4. master 브랜치 배포 시작'
                sshagent(credentials: ['server-ssh-key']) {
                    script {
                        // 이미지 tar 파일 생성 & 전송
                        sh "docker save -o app-image.tar ${DOCKER_IMAGE_NAME}:${BUILD_NUMBER}"
                        sh "scp -o StrictHostKeyChecking=no app-image.tar ${DEPLOY_SERVER}:/tmp/app-image.tar"

                        // 원격 서버에 배포 스크립트 생성
                        writeFile file: 'deploy.sh', text: """
#!/bin/bash
set -e
docker load -i /tmp/app-image.tar
docker stop ${DOCKER_IMAGE_NAME} || true
docker rm ${DOCKER_IMAGE_NAME} || true
docker run -d --name ${DOCKER_IMAGE_NAME} -p 8080:8080 \\
  -e DB_PASSWORD=${DB_PASSWORD} \\
  -e DB_URL=${DB_URL} \\
  -e DB_USERNAME=${DB_USERNAME} \\
  -e SPRING_PROFILES_ACTIVE=prod \\
  ${DOCKER_IMAGE_NAME}:${BUILD_NUMBER}
"""
                        sh "chmod +x deploy.sh"
                        sh "scp -o StrictHostKeyChecking=no deploy.sh ${DEPLOY_SERVER}:/tmp/deploy.sh"
                        sh "ssh -o StrictHostKeyChecking=no ${DEPLOY_SERVER} 'bash /tmp/deploy.sh'"
                    }
                }
            }
        }
    }
}
