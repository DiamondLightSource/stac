if [ "$STACDIR" == "" ]; then
  . setupstac.sh
else
  . $STACDIR/scripts/setupstac.sh
fi


if [ "$1" == "" ]; then
  par="V4L4J1 50 /dev/video0 320 240 0 0 90 TANGO 50 tango://deino:20000/id14/prosilica_minidiff/4 HTTP_test TANGO 50 tango://deino:20000/id14/prosilica_minidiff/1 HTTP 50 http://id141video1.esrf.fr/jpg/quad/image.jpg SWING_test"
elif [ "$1" == "video" ]; then
  par="HTTP 50 http://id144video1.esrf.fr/jpg/quad/image.jpg HTTP 50 http://id141video1.esrf.fr/jpg/quad/image.jpg HTTP 50 http://id231video1.esrf.fr/jpg/quad/image.jpg HTTP 50 http://id232video1.esrf.fr/jpg/quad/image.jpg HTTP 50 http://id29video1.esrf.fr/jpg/quad/image.jpg HTTP 50 http://160.103.134.12/jpg/1/image.jpg SWING_test"
elif [ "$1" == "onaxis" ]; then
  par="TANGO 50 tango://deino.esrf.fr:20000/id14/prosilica_minidiff/4 TANGO 50 tango://deino.esrf.fr:20000/id14/prosilica_minidiff/1 TANGO 50 tango://basil.esrf.fr:20000/id23/ge1350c/1 TANGO 50 tango://basil.esrf.fr:20000/id23/gc655c/1 TANGO 50 tango://p1-id29.esrf.fr:20000/id29/md2/1 TANGO 50 tango://cake.esrf.fr:20000/d14/microdiff/camera SWING_test"
elif [ "$1" == "onaxis_http" ]; then
  par="TANGO 50 tango://deino.esrf.fr:20000/id14/prosilica_minidiff/4 TANGO 50 tango://deino.esrf.fr:20000/id14/prosilica_minidiff/1 TANGO 50 tango://basil.esrf.fr:20000/id23/ge1350c/1 TANGO 50 tango://basil.esrf.fr:20000/id23/gc655c/1 TANGO 50 tango://p1-id29.esrf.fr:20000/id29/md2/1 TANGO 50 tango://cake.esrf.fr:20000/d14/microdiff/camera HTTPproxy_test"
else
  par=$@
fi
echo "PARAMETERS: "$par
echo "JAVA      : "`which java`
java -version

echo "START..."
cd $RUNDIR
java -Djava.library.path=$stacjavalibs -Dj3d.sharedstereozbuffer=true -Dj3d.stereo=PREFERRED -DSTAC_LOG_DIR=$STAC_LOG_DIR -DSTAC_WORK_DIR=$STAC_WORK_DIR -DBCMDEF=$BCMDEF -DSPECDEF=$SPECDEF -DGNSDEF=$GNSDEF -DSTACDIR=$STACDIR -DSTAC_DEF_MOS_SETT=$STAC_DEF_MOS_SETT -DSTAC_DEF_MOS_MAT=$STAC_DEF_MOS_MAT -DSTAC_DEF_HKL=$STAC_DEF_HKL -DSTAC_DEF_XDS=$STAC_DEF_XDS stac.videoView.VideoView $par
