pipeline {
    agent any
    environment {
        // stuff that should be changed for each project goes here
        PROJECT_NAME = 'saltmarsh'
        DOCKER_RUN_ARGUMENTS = '--env-file /home/ollie/Projects/Saltmarsh/env_file'
    }

    stages {
        stage('Test') {
            steps {
                echo "Running tests for ${env.PROJECT_NAME} on ${env.JENKINS_URL} ..."
            }
        }

        stage('Build') {
            steps {
                echo "Building ${env.PROJECT_NAME} on ${env.JENKINS_URL} ..."
                sh "docker build -t ${env.PROJECT_NAME} ."
            }
        }

        stage('Deploy') {
            environment {
                DEPLOY_SERVER_URL = "olliejonas.com"
                DEPLOY_SERVER_USER = "root"
            }
            steps {
                script {
                    env.DEPLOY_SERVER = "${env.DEPLOY_SERVER_USER}@${env.DEPLOY_SERVER_URL}"
                }
                echo "Deploying ${env.PROJECT_NAME} onto ${env.DEPLOY_SERVER_URL} ..."
                sh "docker save -o ${env.PROJECT_NAME}.tar ${env.PROJECT_NAME}:latest"

                sshagent(credentials: ['projects']) {
                    sh """
                        [ -d ~/.ssh ] || mkdir ~/.ssh && chmod 0700 ~/.ssh
                        ssh-keyscan -t rsa,dsa ${DEPLOY_SERVER_URL} >> ~/.ssh/known_hosts
                        ssh -t -t ${env.DEPLOY_SERVER} \"mkdir -p ${env.JOB_NAME}\"
                        scp ${env.PROJECT_NAME}.tar ${env.DEPLOY_SERVER}:~/${env.JOB_NAME}
                        ssh -t -t ${env.DEPLOY_SERVER} << EOF
                        cd ${env.JOB_NAME}
                        source /home/ollie/.bash_profile
                        docker kill ${env.PROJECT_NAME}
                        docker container prune --force
                        docker image rm ${env.PROJECT_NAME}
                        docker load --input ${env.PROJECT_NAME}.tar
                        rm ${env.PROJECT_NAME}.tar
                        docker run -d --name=${env.PROJECT_NAME} ${env.DOCKER_RUN_ARGUMENTS} ${env.PROJECT_NAME}
                        exit
                        EOF
                    """
                }
            }
        }
    }

    post {
        always {
            echo "Performing cleanup..."
            sh "docker image prune --force" // in case it created any dangling images
            sh "docker image rm ${env.PROJECT_NAME} --force" // dont want the image left on the agent

            // remove any zipped stuff
            sh "rm ${env.PROJECT_NAME}.tar"
            // sh "rm ${env.BUILD_TAG}.tar.gz"
        }
    }
}