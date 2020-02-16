def call() {
	pipeline {
		agent none

		stages {
			stage('Prepare') {
				agent any

				steps {
					checkout scm: [$class: 'GitSCM', userRemoteConfigs: [[url: 'https://git.zx2c4.com/wireguard-tools']], branches: [[name: 'refs/tags/*']]], poll: true
					sh 'git reset --hard'
					sh 'git clean -fX'
				}
			}

			stage('Tools MIPS') {
				agent {
					docker {
						image 'lochnair/musl-buildenv:mips'
					}
				}

				steps {
					sh '''
						cd src
						CC="mips-linux-musl-gcc" LDLIBS="-static" make -j5
						mips-linux-musl-strip --strip-unneeded wg
						mv -v wg $WORKSPACE/wg-mips
						make clean
					'''
					archiveArtifacts artifacts: 'wg-mips', fingerprint: true, onlyIfSuccessful: true
				}
			}

			stage('Tools MIPSel') {
				agent {
					docker {
						image 'lochnair/musl-buildenv:mipsel'
					}
				}

				steps {
					sh '''
						cd src
						CC="mipsel-linux-musl-gcc" LDLIBS="-static" make -j5
						mipsel-linux-musl-strip --strip-unneeded wg
						mv -v wg $WORKSPACE/wg-mipsel
						make clean
					'''
					archiveArtifacts artifacts: 'wg-mipsel', fingerprint: true, onlyIfSuccessful: true
				}
			}
		}
	}
}
