pipeline {
  agent any

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Unit Tests') {
      steps {
        dir('backend') {
          sh 'mvn clean test'
        }
      }
      post {
        always {
          junit 'backend/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Integration Tests') {
      steps {
        dir('backend') {
          sh 'mvn verify -DskipTests'
        }
      }
      post {
        always {
          junit 'backend/target/failsafe-reports/*.xml'
        }
      }
    }

    stage('Docker Up') {
      steps {
        sh 'docker compose up -d --build backend frontend'
      }
    }

    stage('E2E Tests') {
      steps {
        sh 'docker compose --profile e2e up --build --abort-on-container-exit --exit-code-from e2e'
      }
      post {
        always {
          junit 'e2e/target/surefire-reports/*.xml'
        }
      }
    }
  }

  post {
    always {
      sh 'docker compose down -v'
    }
  }
}
