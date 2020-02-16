def call() {
	pipeline {
		agent none

		stages {
			stage('Prepare') {
				agent any

				steps {
					checkout scm: [$class: 'GitSCM', userRemoteConfigs: [[url: 'https://git.zx2c4.com/wireguard-linux-compat']], branches: [[name: 'refs/tags/*']]], poll: true
					sh 'git reset --hard'
					sh 'git clean -fX'
					sh 'curl -L https://gist.githubusercontent.com/Lochnair/805bf9ab96742d0fe1c25e4130268307/raw/29e37d43a5247a3f2584ef0d6d553ee9a4532e12/only-use-__vmalloc-for-now.patch | patch -p1'
					sh 'sed -i \'s/ --dirty//g\' src/Makefile'

					copyArtifacts filter: 'e50-ksrc.tar.bz2', fingerprintArtifacts: true, projectName: 'ubiquiti/kernel_e50/v2.0.8%2Fmaster', selector: lastSuccessful()
					copyArtifacts filter: 'e100-ksrc.tar.bz2', fingerprintArtifacts: true, projectName: 'ubiquiti/kernel_e100/v2.0.8%2Fmaster', selector: lastSuccessful()
					copyArtifacts filter: 'e200-ksrc.tar.bz2', fingerprintArtifacts: true, projectName: 'ubiquiti/kernel_e200/v2.0.8%2Fmaster', selector: lastSuccessful()
					copyArtifacts filter: 'e300-ksrc.tar.bz2', fingerprintArtifacts: true, projectName: 'ubiquiti/kernel_e300/v2.0.8%2Fmaster', selector: lastSuccessful()
					copyArtifacts filter: 'e1000-ksrc.tar.bz2', fingerprintArtifacts: true, projectName: 'ubiquiti/kernel_e1000/v2.0.8%2Fmaster', selector: lastSuccessful()

					sh '''
					mkdir -p e50-ksrc e100-ksrc e200-ksrc e300-ksrc e1000-ksrc ugw3-ksrc ugw4-ksrc ugwxg-ksrc
					tar -xjf e50-ksrc.tar.bz2 -C e50-ksrc --checkpoint=1000 --checkpoint-action=echo="#%u: %T"
					tar -xjf e100-ksrc.tar.bz2 -C e100-ksrc --checkpoint=1000 --checkpoint-action=echo="#%u: %T"
					tar -xjf e200-ksrc.tar.bz2 -C e200-ksrc --checkpoint=1000 --checkpoint-action=echo="#%u: %T"
					tar -xjf e300-ksrc.tar.bz2 -C e300-ksrc --checkpoint=1000 --checkpoint-action=echo="#%u: %T"
					tar -xjf e1000-ksrc.tar.bz2 -C e1000-ksrc --checkpoint=1000 --checkpoint-action=echo="#%u: %T"
					'''
				}
			}

			stage('E50 module') {
				agent {
					docker {
						image 'lochnair/mtk-buildenv:latest'
					}
				}

				steps {
					sh '''
					cd src
					make -j5 ARCH=mips CROSS_COMPILE=mipsel-mtk-linux- KERNELDIR=$WORKSPACE/e50-ksrc module
					mipsel-mtk-linux-strip --strip-debug wireguard.ko
					mv wireguard.ko $WORKSPACE/wireguard-e50.ko
					make ARCH=mips CROSS_COMPILE=mipsel-mtk-linux- KERNELDIR=$WORKSPACE/e50-ksrc clean
					'''

					archiveArtifacts artifacts: 'wireguard-e50.ko', fingerprint: true, onlyIfSuccessful: true
				}
			}

			stage('E100 module') {
				agent {
					docker {
						image 'lochnair/octeon-buildenv:latest'
					}
				}

				steps {
					sh '''
					cd src
					make -j5 ARCH=mips CROSS_COMPILE=mips64-octeon-linux- KERNELDIR=$WORKSPACE/e100-ksrc module
					mips64-octeon-linux-strip --strip-debug wireguard.ko
					mv wireguard.ko $WORKSPACE/wireguard-e100.ko
					make ARCH=mips CROSS_COMPILE=mips64-octeon-linux- KERNELDIR=$WORKSPACE/e100-ksrc clean
					'''

					archiveArtifacts artifacts: 'wireguard-e100.ko', fingerprint: true, onlyIfSuccessful: true
				}
			}

			stage('E200 module') {
				agent {
					docker {
						image 'lochnair/octeon-buildenv:latest'
					}
				}

				steps {
					sh '''
					cd src
					make -j5 ARCH=mips CROSS_COMPILE=mips64-octeon-linux- KERNELDIR=$WORKSPACE/e200-ksrc module
					mips64-octeon-linux-strip --strip-debug wireguard.ko
					mv wireguard.ko $WORKSPACE/wireguard-e200.ko
					make ARCH=mips CROSS_COMPILE=mips64-octeon-linux- KERNELDIR=$WORKSPACE/e200-ksrc clean
					'''

					archiveArtifacts artifacts: 'wireguard-e200.ko', fingerprint: true, onlyIfSuccessful: true
				}
			}

			stage('E300 module') {
				agent {
					docker {
						image 'lochnair/octeon-buildenv:latest'
					}
				}

				steps {
					sh '''
					cd src
					make -j5 ARCH=mips CROSS_COMPILE=mips64-octeon-linux- KERNELDIR=$WORKSPACE/e300-ksrc module
					mips64-octeon-linux-strip --strip-debug wireguard.ko
					mv wireguard.ko $WORKSPACE/wireguard-e300.ko
					make ARCH=mips CROSS_COMPILE=mips64-octeon-linux- KERNELDIR=$WORKSPACE/e300-ksrc clean
					'''

					archiveArtifacts artifacts: 'wireguard-e300.ko', fingerprint: true, onlyIfSuccessful: true
				}
			}

			stage('E1000 module') {
				agent {
					docker {
						image 'lochnair/octeon-buildenv:latest'
					}
				}

				steps {
					sh '''
					cd src
					make -j5 ARCH=mips CROSS_COMPILE=mips64-octeon-linux- KERNELDIR=$WORKSPACE/e1000-ksrc module
					mips64-octeon-linux-strip --strip-debug wireguard.ko
					mv wireguard.ko $WORKSPACE/wireguard-e1000.ko
					make ARCH=mips CROSS_COMPILE=mips64-octeon-linux- KERNELDIR=$WORKSPACE/e1000-ksrc clean
					'''

					archiveArtifacts artifacts: 'wireguard-e1000.ko', fingerprint: true, onlyIfSuccessful: true
				}
			}

			stage('Clean') {
				agent any

				steps {
					sh 'git reset --hard HEAD'
				}
			}
		}
	}
}
