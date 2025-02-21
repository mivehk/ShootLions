# Shooting Lions Game

This 2D game was adopted from a scene in the TV Show 1923 Two characters were on top of a tree and defending themselves against hungry lions game uses keyboard arrow keys to rotate the character clockwise(VK-right) and counter-clockwise(VK-left) VK-space is used for shooting bullets to lions, which randomly appear on the panel from different angles

![Alt text](images/ScS2025-02-20.png)


## Here is the [Java Documentation](https://github.com/mivehk/ShootLions/tree/Initial_H/doc).
## You can compile and package the game with this Jenkins pipeline:

```bash
pipeline {
    agent any

    stages {
        stage('Clone Repository') {
            steps {
                sh 'rm -rf *'
                sh 'git init'
                sh '''
                if git remote | grep -q ShootLions; then
                    echo "Remote repo ShootLions alreaddy Exists"
                else    
                    git remote add ShootLions https://github.com/mivehk/ShootLions
                fi
                '''
                sh 'git fetch --all'
                    sh '''
                    if git show-ref --verify --quiet refs/heads/Initial_H; then
                        git checkout Initial_H
                        git reset --hard ShootLions/Initial_H
                        git pull ShootLions Initial_H:Initial_H
                    else
                        git checkout -b Initial_H ShootLions/Initial_H
                        git pull ShootLions Initial_H:Initial_H
                    fi
                    '''
            }
        }

        stage('Compile') {
            steps {
                // Compile Java files in the src directory
                sh 'mkdir -p out' // Creates an output directory if not present
                sh 'javac -d out $(find src -name "*.java")'
            }
        }

        stage('Package') {
            steps {
                sh '''
                echo "Manifest-Version: 1.0" > MANIFEST.MF 
                echo "Main-Class: ShootLions" >> MANIFEST.MF
                echo "" >> MANIFEST.MF

                cp -r src/resources/*.png out/

                '''
                // Package compiled files into a JAR
                sh 'jar cvfm shootlions2d-game.jar MANIFEST.MF -C out .'
            }
        }

        stage('Archive Artifacts') {
            steps {
                // Archive the JAR file
                archiveArtifacts artifacts: 'shootlions2d-game.jar', allowEmptyArchive: true
            }
        }
    }
}
```






