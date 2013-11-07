ii  libopencv-core-dev                        2.4.2+dfsg-0exp2ubuntu1                    amd64        development files for libopencv-core
ii  libopencv-core2.4                         2.4.2+dfsg-0exp2ubuntu1                    amd64        computer vision core library
ii  libopencv-highgui-dev                     2.4.2+dfsg-0exp2ubuntu1                    amd64        development files for libopencv-highgui
ii  libopencv-highgui2.4                      2.4.2+dfsg-0exp2ubuntu1                    amd64        computer vision High-level GUI and Media I/O library
ii  libopencv-imgproc-dev                     2.4.2+dfsg-0exp2ubuntu1                    amd64        development files for libopencv-imgproc
ii  libopencv-imgproc2.4                      2.4.2+dfsg-0exp2ubuntu1                    amd64        computer vision Image Processing library

how to compile the android jars

NOTE: the instructions are valid for GNU/Linux at the moment.
Prerequisites for Building ASTER

    specify environment variable JAVA_HOME, for example:

    echo 'export JAVA_HOME=/usr/lib/jvm/java-6-sun' >> ~/.bashrc

    ASTER is built upon MonkeyRunner, which is shipped in Android Open Source Project. To get the latest MonkeyRunner, please follow the instructions: Downloading the Source Tree.
    Assume $HOME/AOSP is the the directory containing all the files obtained through 'repo sync'
    You need OpenCV 2.3 

Get Source

    The version control system is GIT:

    git clone http://code.google.com/p/aster

Build

    Prepare MonkeyRunner for the first time

    cd $HOME/AOSP
    TARGET_PRODUCT=generic make monkeyrunner

        Alternatively, if you already configured/build Android by 'source build/envsetup.sh', use the faster method

        cd $HOME/AOSP/sdk/monkeyrunner && mm

    Speficy framework path into local.properties for the first time

    cd aster
    echo 'framework.dir=$HOME/AOSP/out/host/linux-x86/framework' > local.properties

    Finally, build it:

    ant compile

Run ASTER

ant dist
dist/aster


