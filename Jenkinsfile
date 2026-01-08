pipeline {
  agent {
    docker {
      image 'maven:3.9.8-eclipse-temurin-21'
      args '-v $HOME/.m2:/root/.m2'
    }
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build') {
      steps {
        dir('backend') {
          sh 'mvn -q -DskipTests=false clean package'
        }
      }
    }

    stage('Unit Tests') {
      steps {
        dir('backend') {
          sh 'mvn -q -Dtest=*Test test'
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
          sh 'mvn -q -DskipTests=true -DskipITs=false failsafe:integration-test failsafe:verify'
        }
      }
      post {
        always {
          junit 'backend/target/failsafe-reports/*.xml'
        }
      }
    }

    stage('Docker Up') {
      agent {
        docker {
          image 'docker:27-cli'
          args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
      }
      steps {
        sh 'docker compose up -d --build backend frontend'
      }
    }

    stage('E2E Tests') {
      agent {
        docker {
          image 'docker:27-cli'
          args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
      }
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
      sh 'docker compose down -v || true'
    }
  }
}
