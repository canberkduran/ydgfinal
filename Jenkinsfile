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
        sh 'tar -C $WORKSPACE/backend -cf - . | DOCKER_BUILDKIT=0 docker build -t ydgfinal-backend:latest -f Dockerfile -'
        sh 'tar -C $WORKSPACE/frontend -cf - . | DOCKER_BUILDKIT=0 docker build -t ydgfinal-frontend:latest -f Dockerfile -'
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
          junit allowEmptyResults: true, testResults: 'backend/target/failsafe-reports/*.xml'
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
        sh 'docker network create ydgfinal_e2e || true'
        sh 'docker rm -f ydgfinal_backend ydgfinal_frontend ydgfinal_selenium || true'
        sh 'docker run -d --name ydgfinal_backend --network ydgfinal_e2e -p 8090:8090 ydgfinal-backend:latest'
        sh 'docker run -d --name ydgfinal_frontend --network ydgfinal_e2e -p 3000:80 ydgfinal-frontend:latest'
        sh 'docker run -d --name ydgfinal_selenium --network ydgfinal_e2e -p 4444:4444 selenium/standalone-chrome:4.21.0'
        sh 'docker run --rm --volumes-from $(hostname) --network ydgfinal_e2e -w $WORKSPACE/e2e maven:3.9.8-eclipse-temurin-21 mvn -q test'
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: 'e2e/target/surefire-reports/*.xml'
        }
      }
    }
  }

  post {
    always {
      sh 'docker rm -f ydgfinal_backend ydgfinal_frontend ydgfinal_selenium || true'
      sh 'docker network rm ydgfinal_e2e || true'
    }
  }
}
