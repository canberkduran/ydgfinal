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
        sh 'docker-compose run --rm -w $WORKSPACE/backend maven mvn -q -DskipTests=false clean package'
        sh 'docker-compose run --rm -w $WORKSPACE/frontend node sh -c "npm install && npm run build"'
      }
    }

    stage('Unit Tests') {
      steps {
        sh 'docker-compose run --rm -w $WORKSPACE/backend maven mvn -q -Dtest=*Test test'
      }
      post {
        always {
          junit 'backend/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Integration Tests') {
      steps {
        sh 'docker-compose run --rm -w $WORKSPACE/backend maven mvn -q -DskipTests=true -DskipITs=false failsafe:integration-test failsafe:verify'
      }
      post {
        always {
          junit 'backend/target/failsafe-reports/*.xml'
        }
      }
    }

    stage('Docker Up') {
      steps {
        sh 'docker-compose up -d --build backend frontend'
      }
    }

    stage('E2E Tests') {
      steps {
        sh 'docker-compose --profile e2e up --build --abort-on-container-exit --exit-code-from e2e'
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
      sh 'docker-compose down --remove-orphans'
    }
  }
}
