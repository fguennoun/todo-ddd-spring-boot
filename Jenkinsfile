// =================================================================
// Jenkins Pipeline for Todo DDD Spring Boot Application
// Complete CI/CD pipeline with quality gates and security scans
// =================================================================

pipeline {
    agent any

    environment {
        // Application info
        APP_NAME = 'todo-ddd-app'
        APP_VERSION = "${BUILD_NUMBER}"

        // Docker configuration
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_REPOSITORY = 'todoapp/todo-ddd'
        DOCKER_IMAGE = "${DOCKER_REPOSITORY}:${APP_VERSION}"

        // SonarQube configuration
        SONAR_PROJECT_KEY = 'todo-ddd-springboot-app'
        SONAR_HOST_URL = 'http://sonarqube:9000'

        // Quality gates
        COVERAGE_THRESHOLD = '80'

        // Security scanning
        TRIVY_CACHE_DIR = "${WORKSPACE}/.trivy"

        // Maven configuration
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository -Xmx1024m'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 45, unit: 'MINUTES')
        timestamps()
    }

    stages {
        // =================================================================
        // Stage 1: Checkout & Environment Setup
        // =================================================================
        stage('📋 Checkout') {
            steps {
                echo '🔄 Checking out source code...'
                checkout scm

                script {
                    // Set build information
                    currentBuild.displayName = "#${BUILD_NUMBER} - ${GIT_BRANCH}"
                    currentBuild.description = "Commit: ${GIT_COMMIT[0..7]}"
                }
            }
        }

        // =================================================================
        // Stage 2: Unit Tests
        // =================================================================
        stage('🧪 Unit Tests') {
            steps {
                echo '🧪 Running unit tests...'
                script {
                    try {
                        sh '''
                            ./mvnw clean test \
                            -Dspring.profiles.active=test \
                            -Djacoco.skip=false \
                            -B
                        '''
                    } catch (Exception e) {
                        currentBuild.result = 'UNSTABLE'
                        error("Unit tests failed: ${e.getMessage()}")
                    }
                }
            }
            post {
                always {
                    // Publish test results
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'

                    // Publish JaCoCo coverage report
                    publishCoverage adapters: [
                        jacocoAdapter('target/site/jacoco/jacoco.xml')
                    ], sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
                }
            }
        }

        // =================================================================
        // Stage 3: Integration Tests with Testcontainers
        // =================================================================
        stage('🔧 Integration Tests') {
            steps {
                echo '🔧 Running integration tests with Testcontainers...'
                script {
                    try {
                        sh '''
                            ./mvnw verify \
                            -Dspring.profiles.active=test \
                            -DskipUnitTests=true \
                            -Dtestcontainers.reuse.enable=true \
                            -B
                        '''
                    } catch (Exception e) {
                        currentBuild.result = 'UNSTABLE'
                        error("Integration tests failed: ${e.getMessage()}")
                    }
                }
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/failsafe-reports/*.xml'
                }
            }
        }

        // =================================================================
        // Stage 4: Code Quality Analysis
        // =================================================================
        stage('📊 SonarQube Analysis') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    changeRequest()
                }
            }
            steps {
                echo '📊 Running SonarQube analysis...'
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        ./mvnw sonar:sonar \
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                        -Dsonar.host.url=${SONAR_HOST_URL} \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                        -B
                    '''
                }
            }
        }

        stage('🎯 Quality Gate') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    changeRequest()
                }
            }
            steps {
                echo '🎯 Waiting for SonarQube Quality Gate...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // =================================================================
        // Stage 5: Security Analysis
        // =================================================================
        stage('🔒 Security Analysis') {
            parallel {
                stage('OWASP Dependency Check') {
                    steps {
                        echo '🔍 Running OWASP Dependency Check...'
                        sh '''
                            ./mvnw org.owasp:dependency-check-maven:check \
                            -Dformat=XML \
                            -DfailBuildOnAnyVulnerability=false \
                            -B
                        '''
                    }
                    post {
                        always {
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'target',
                                reportFiles: 'dependency-check-report.html',
                                reportName: 'OWASP Dependency Check Report'
                            ])
                        }
                    }
                }

                stage('SpotBugs Analysis') {
                    steps {
                        echo '🐛 Running SpotBugs analysis...'
                        sh '''
                            ./mvnw com.github.spotbugs:spotbugs-maven-plugin:check \
                            -Dspotbugs.effort=Max \
                            -Dspotbugs.threshold=Medium \
                            -B
                        '''
                    }
                    post {
                        always {
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'target',
                                reportFiles: 'spotbugsXml.html',
                                reportName: 'SpotBugs Report'
                            ])
                        }
                    }
                }
            }
        }

        // =================================================================
        // Stage 6: Build Application
        // =================================================================
        stage('📦 Build JAR') {
            steps {
                echo '📦 Building application JAR...'
                sh '''
                    ./mvnw clean package \
                    -DskipTests=true \
                    -Dspring.profiles.active=prod \
                    -B
                '''
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: false
                }
            }
        }

        // =================================================================
        // Stage 7: Docker Build & Security Scan
        // =================================================================
        stage('🐳 Docker Build') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                echo '🐳 Building Docker image...'
                script {
                    docker.build("${DOCKER_IMAGE}")
                }
            }
        }

        stage('🛡️ Container Security Scan') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                echo '🛡️ Scanning Docker image with Trivy...'
                sh '''
                    # Create cache directory if it doesn't exist
                    mkdir -p ${TRIVY_CACHE_DIR}

                    # Run Trivy scan
                    trivy image \
                        --cache-dir ${TRIVY_CACHE_DIR} \
                        --format json \
                        --output trivy-report.json \
                        --severity HIGH,CRITICAL \
                        ${DOCKER_IMAGE}

                    # Also generate table format for readability
                    trivy image \
                        --cache-dir ${TRIVY_CACHE_DIR} \
                        --format table \
                        --severity HIGH,CRITICAL \
                        ${DOCKER_IMAGE}
                '''
            }
            post {
                always {
                    archiveArtifacts artifacts: 'trivy-report.json', allowEmptyArchive: true
                }
            }
        }

        // =================================================================
        // Stage 8: Push to Registry
        // =================================================================
        stage('📤 Push to Registry') {
            when {
                branch 'main'
            }
            steps {
                echo '📤 Pushing Docker image to registry...'
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-registry-credentials') {
                        docker.image("${DOCKER_IMAGE}").push()
                        docker.image("${DOCKER_IMAGE}").push('latest')
                    }
                }
            }
        }

        // =================================================================
        // Stage 9: Deploy to Staging
        // =================================================================
        stage('🚀 Deploy to Staging') {
            when {
                branch 'main'
            }
            steps {
                echo '🚀 Deploying to staging environment...'
                script {
                    // Deploy using Docker Compose or Kubernetes
                    sh '''
                        # Example deployment with Docker Compose
                        export APP_VERSION=${APP_VERSION}
                        docker-compose -f docker-compose.staging.yml up -d

                        # Wait for service to be healthy
                        timeout 300 bash -c 'until curl -f http://staging.todo-app.com/actuator/health; do sleep 5; done'
                    '''
                }
            }
        }

        // =================================================================
        // Stage 10: Smoke Tests
        // =================================================================
        stage('💨 Smoke Tests') {
            when {
                branch 'main'
            }
            steps {
                echo '💨 Running smoke tests on staging...'
                sh '''
                    # Basic health check
                    curl -f http://staging.todo-app.com/actuator/health

                    # API availability check
                    curl -f http://staging.todo-app.com/api/v1/todos -H "Authorization: Bearer test-token"
                '''
            }
        }
    }

    // =================================================================
    // Post Actions
    // =================================================================
    post {
        always {
            echo '🧹 Cleaning up workspace...'
            cleanWs()
        }

        success {
            echo '✅ Pipeline completed successfully!'
            slackSend(
                channel: '#deployments',
                color: 'good',
                message: "✅ ${APP_NAME} v${APP_VERSION} deployed successfully to staging"
            )
        }

        failure {
            echo '❌ Pipeline failed!'
            slackSend(
                channel: '#deployments',
                color: 'danger',
                message: "❌ ${APP_NAME} v${APP_VERSION} pipeline failed"
            )
        }

        unstable {
            echo '⚠️ Pipeline completed with issues!'
            slackSend(
                channel: '#deployments',
                color: 'warning',
                message: "⚠️ ${APP_NAME} v${APP_VERSION} pipeline completed with warnings"
            )
        }
    }
}
