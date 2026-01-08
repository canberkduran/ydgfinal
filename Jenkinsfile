pipeline {
  agent any

  options {
    skipDefaultCheckout(true)
  }

  stages {

    stage('Checkout') {
      steps {
        deleteDir()
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
        sh 'export COMPOSE_PROJECT_NAME=ydgfinal; docker-compose down --remove-orphans || true'
        sh 'export COMPOSE_PROJECT_NAME=ydgfinal; docker-compose up -d --build backend frontend'
      }
    }

    stage('E2E Tests') {
      steps {
        sh 'docker rm -f ydgfinal_selenium || true'
        sh 'docker run -d --name ydgfinal_selenium --network ydgfinal_default --network-alias selenium selenium/standalone-chrome:4.21.0'
        sh 'docker run --rm --network ydgfinal_default curlimages/curl:8.7.1 sh -c "for i in 1 2 3 4 5 6 7 8 9 10; do curl -sf http://selenium:4444/wd/hub/status && exit 0; sleep 2; done; exit 1"'
        sh 'docker run --rm --volumes-from $(hostname) --network ydgfinal_default -e SELENIUM_URL=http://selenium:4444/wd/hub -e FRONTEND_URL=http://frontend -w $WORKSPACE/e2e maven:3.9.8-eclipse-temurin-21 mvn -q test'
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
      sh 'docker rm -f ydgfinal_selenium || true'
      sh 'docker-compose down --remove-orphans'
    }
  }
}

