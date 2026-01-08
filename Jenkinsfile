pipeline {
  agent any

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build') {
      steps {
        sh 'docker compose run --rm maven mvn clean package -DskipTests=false'
      }
    }

    stage('Unit Tests') {
      steps {
        sh 'docker compose run --rm maven mvn test'
      }
      post {
        always {
          junit 'backend/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Integration Tests') {
      steps {
        sh 'docker compose run --rm maven mvn verify -DskipTests=true'
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
