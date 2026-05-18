pipeline {
    agent any

    environment {
        // 定义环境变量
        DOCKER_HUB_CREDENTIALS = 'dockerhub-auth' 
        DOCKER_IMAGE = 'frosky/teedy-app' 
        DOCKER_TAG = "${env.BUILD_NUMBER}" 
    }

    stages {
        // 修改点1：将 'Build' 修改为 'Package' 以匹配图片要求
        stage('Package') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/master']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/FrostyHec/CS304_Teedy.git']]
                )
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage('Building image') {
            steps {
                script {
                    docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                }
            }
        }

        // 修改点2：将 'Upload image' 修改为 'Upload Image' (大写I)
        stage('Upload Image') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', DOCKER_HUB_CREDENTIALS) {
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push()
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push('latest')
                    }
                }
            }
        }

        stage('Run containers') {
            steps {
                script {
                    def ports = ['8082', '8083', '8084']
                    
                    for (String port : ports) {
                        def containerName = "teedy-container-${port}"
                        
                        // 停止并删除旧容器
                        sh "docker stop ${containerName} || true"
                        sh "docker rm ${containerName} || true"

                        // 【关键修改】：使用原生 sh 命令执行 docker run，而不是 docker.image().run()
                        sh "docker run --name ${containerName} -d -p ${port}:8080 ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
                    }

                    // 列出运行中的 teedy-container 确认状态
                    sh 'docker ps --filter "name=teedy-container"'
                }
            }
        }
    }
}
