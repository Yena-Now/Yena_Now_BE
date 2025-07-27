pipeline {
    agent any

    when {
        expression { return env.gitlabTargetBranch == 'master' }
    }


    environment {
        DOCKER_IMAGE_NAME = "yena_now_be"
    }

    stages {
        stage('Checkout') {
            steps {
                echo '1. GitLab에서 소스 코드 가져오기'
                git branch: 'master', url: 'https://lab.ssafy.com/common-pjt-e203/yena_now_be.git', credentialsId: 'gitlab-access-token'
            }
        }

        stage('Build') {
            steps {
                echo '2. Gradle로 프로젝트 빌드'
                sh './gradlew clean build'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '3. Docker 이미지 빌드'
                sh "docker build -t ${env.DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER} ."
            }
        }

        stage('Deploy') {
            steps {
                echo '4. 배포 서버에 애플리케이션 배포'
                sshagent(credentials: ['server-ssh-key']) {
                    script {
                        // 'APP_'로 시작하는 모든 환경 변수를 찾아 docker -e 옵션으로 자동 변환
                        def dockerEnvOpts = env.getEnvironment().findAll { key, value ->
                            key.startsWith('APP_')
                        }.collect { key, value ->
                            // 'APP_' 접두사를 제거하고 -e 'KEY=VALUE' 형식의 문자열 생성
                            def containerEnvVar = key.substring(4) // "APP_".length()
                            "-e ${containerEnvVar}='${value}'"
                        }.join(' ')

                        // 기본 프로필 활성화 옵션 추가
                        dockerEnvOpts += " -e SPRING_PROFILES_ACTIVE=prod"

                        // 도커 이미지 저장 및 배포 서버로 전송
                        sh "docker save -o app-image.tar ${env.DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}"
                        sh "scp -o StrictHostKeyChecking=no app-image.tar ${env.DEPLOY_SERVER_USER}@${env.DEPLOY_SERVER_IP}:/tmp/app-image.tar"

                        // 배포 서버에 SSH로 접속하여 컨테이너 실행
                        // env.DEPLOY_SERVER_USER와 env.DEPLOY_SERVER_IP는 GitLab에서 자동으로 주입됨
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