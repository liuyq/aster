#!/bin/zsh

ASTER=/home/kanru/space/0xlab/android/aster

java -cp $ASTER/dist/aster-`date +%Y%m%d`.jar:"$ASTER/libs/*" org.zeroxlab.aster.AsterMain
