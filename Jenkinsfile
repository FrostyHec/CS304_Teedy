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
                    // 修改点3：使用循环启动 3 个容器，端口分别为 8082, 8083, 8084
                    def ports = ['8082', '8083', '8084']
                    
                    for (String port : ports) {
                        def containerName = "teedy-container-${port}"
                        
                        // 如果同名容器已存在，先停止并删除
                        sh "docker stop ${containerName} || true"
                        sh "docker rm ${containerName} || true"

                        // 运行新的容器，映射对应的端口到 8080
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run(
                            "--name ${containerName} -d -p ${port}:8080"
                        )
                    }

                    // 列出运行中的 teedy-container 确认状态
                    sh 'docker ps --filter "name=teedy-container"'
                }
            }
        }
    }
}
