pipeline {
    agent any

    tools {
        jdk 'jdk17'
    }

    environment {
        DOCKER_IMAGE_NAME = "yena_now_be"
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
                sh "docker build -t ${env.DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER} ."
            }
        }

        stage('Deploy to Production') {
            steps {
                echo '4. master 브랜치에 머지되었거나, 수동 실행으로 인해 배포를 시작합니다.'
                sshagent(credentials: ['server-ssh-key']) {
                    script {
                        def dockerEnvOpts = []
                        env.getEnvironment().each { key, value ->
                            if (key.startsWith('APP_')) {
                                def containerEnvVar = key.substring(4)
                                dockerEnvOpts.add("-e ${containerEnvVar}=${value}")
                            }
                        }
                        dockerEnvOpts.add("-e SPRING_PROFILES_ACTIVE=prod")
                        def envOptionString = dockerEnvOpts.join(' ')

                        echo "=== Docker 전달 환경 변수 옵션 ==="
                        dockerEnvOpts.each { opt -> echo "${opt}" }

                        sh "docker save -o app-image.tar ${env.DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}"
                        sh "scp -o StrictHostKeyChecking=no app-image.tar ${DEPLOY_SERVER_USER}@${DEPLOY_SERVER_IP}:/tmp/app-image.tar"

                        // ssh 원격 명령어의 따옴표를 수정하여 모든 변수가 올바르게 적용되도록 변경
                        sh """
                            ssh -o StrictHostKeyChecking=no ${DEPLOY_SERVER_USER}@${DEPLOY_SERVER_IP} " \
                                docker load -i /tmp/app-image.tar && \
                                docker stop ${DOCKER_IMAGE_NAME} || true && \
                                docker rm ${DOCKER_IMAGE_NAME} || true && \
                                docker run -d --name ${DOCKER_IMAGE_NAME} -p 8080:8080 ${envOptionString} ${DOCKER_IMAGE_NAME}:${BUILD_NUMBER}"
                        """

                        sh "rm -f app-image.tar"
                    }
                }
            }
        }
    }
}
