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
        sh 'tar -C $WORKSPACE -cf - backend | docker build -t ydgfinal-backend:latest -f backend/Dockerfile -'
        sh 'tar -C $WORKSPACE -cf - frontend | docker build -t ydgfinal-frontend:latest -f frontend/Dockerfile -'
        sh 'tar -C $WORKSPACE -cf - e2e | docker build -t ydgfinal-e2e:latest -f e2e/Dockerfile -'
        sh 'docker run --rm --volumes-from $(hostname) -w $WORKSPACE/backend maven:3.9.8-eclipse-temurin-21 mvn -q -DskipTests=false clean package'
        sh 'docker run --rm --volumes-from $(hostname) -w $WORKSPACE/frontend node:20-alpine sh -c "npm install && npm run build"'
      }
    }

    stage('Unit Tests') {
      steps {
        sh 'docker run --rm --volumes-from $(hostname) -w $WORKSPACE/backend maven:3.9.8-eclipse-temurin-21 mvn -q -Dtest=*Test test'
      }
      post {
        always {
          junit 'backend/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Integration Tests') {
      steps {
        sh 'docker run --rm --volumes-from $(hostname) -w $WORKSPACE/backend maven:3.9.8-eclipse-temurin-21 mvn -q -DskipTests=true -DskipITs=false failsafe:integration-test failsafe:verify'
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
