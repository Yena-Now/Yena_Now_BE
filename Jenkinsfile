pipeline {
    agent any

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
                sh './gradlew clean build'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '3. Docker 이미지 빌드'
                sh "docker build -t ${env.DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER} ."
            }
        }

        stage('Deploy to Production') {
            when {
                expression { return env.gitlabTargetBranch == 'master' }
            }
            steps {
                echo '4. master 브랜치에 머지되었으므로 배포를 시작합니다.'
                sshagent(credentials: ['server-ssh-key']) {
                    script {
                        // 'APP_'로 시작하는 모든 환경 변수를 찾아 docker -e 옵션으로 자동 변환
                        def dockerEnvOpts = env.getEnvironment().findAll { key, value ->
                            key.startsWith('APP_')
                        }.collect { key, value ->
                            def containerEnvVar = key.substring(4)
                            "-e ${containerEnvVar}='${value}'"
                        }.join(' ')

                        dockerEnvOpts += " -e SPRING_PROFILES_ACTIVE=prod"

                        // 도커 이미지 저장 및 배포 서버로 전송
                        sh "docker save -o app-image.tar ${env.DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}"
                        sh "scp -o StrictHostKeyChecking=no app-image.tar ${env.DEPLOY_SERVER_USER}@${env.DEPLOY_SERVER_IP}:/tmp/app-image.tar"

                        // 배포 서버에 SSH로 접속하여 컨테이너 실행
                        sh '''
                            ssh -o StrictHostKeyChecking=no ${DEPLOY_SERVER_USER}@${DEPLOY_SERVER_IP} " \
                                docker load -i /tmp/app-image.tar && \
                                docker stop ${DOCKER_IMAGE_NAME} || true && \
                                docker rm ${DOCKER_IMAGE_NAME} || true && \
                                docker run -d --name ${DOCKER_IMAGE_NAME} -p 8080:8080 ''' + dockerEnvOpts + ''' ${DOCKER_IMAGE_NAME}:${BUILD_NUMBER}"
                        '''

                        // 임시 파일 삭제
                        sh "rm -f app-image.tar"
                    }
                }
            }
        }
    }
}