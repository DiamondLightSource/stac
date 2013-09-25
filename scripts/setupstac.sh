
#install
if [ "$STACDIR" == "" ]; then
  export STACDIR=/scratch/STAC
  export BCMDEF=$STACDIR/config/BCM.dat
  export RUNDIR=$STACDIR/
fi
#export JDK=`which java`
#eh4
#export STACDIR=/users/opid14/STAC
#export BCMDEF=$STACDIR/config/BCM.dat
#export RUNDIR=$STACDIR/run
#export JDK=/opt/pxsoft/STAC/java
### common
echo "Starting Paramteres:"
echo "STACDIR  =" $STACDIR
echo "JDK      ="$JDK
#export PATH=$STACDIR/strategy/:$PATH
echo "PATH     =" $PATH

. $STACDIR/scripts/dnasetupstac.sh

#dna conflicting classes
#export stacjars="$STACDIR/thirdparty/jars/dna.jar $STACDIR/thirdparty/jars/castor-0.9.3.19-dna.jar $STACDIR/thirdparty/jars/gnu-regexp-1.1.4.jar $STACDIR/thirdparty/jars/xerces.jar"

#for stacjar in $stacjars; do
#    if [ -r $stacjar ]; then
#      if [ -z $CLASSPATH ]; then
#        export CLASSPATH=$stacjar
#      else
#        export CLASSPATH=$stacjar:$CLASSPATH
#      fi
#    fi
#done

