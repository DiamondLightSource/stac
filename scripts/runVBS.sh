if [ "$STACDIR" == "" ]; then
  . setupstac.sh
else
  . $STACDIR/scripts/setupstac.sh
fi
if [ "$1" == "-server" ]; then
  server="-server"
  shift
else
  server=""
fi
if [ "$1" == "" ]; then
  vbs=vBCM.dat_esrf
else
  vbs=$1
fi
echo "START..."
cd $RUNDIR
java -Dj3d.sharedstereozbuffer=true -Dj3d.stereo=PREFERRED -DBCMDEF=$BCMDEF -DSPECDEF=$SPECDEF -DGNSDEF=$GNSDEF -DSTACDIR=$STACDIR -DSTAC_DEF_MOS_SETT=$STAC_DEF_MOS_SETT -DSTAC_DEF_MOS_MAT=$STAC_DEF_MOS_MAT -DSTAC_DEF_HKL=$STAC_DEF_HKL -DSTAC_DEF_XDS=$STAC_DEF_XDS stac.vbcm.tango.vbcm_server $server $vbs
