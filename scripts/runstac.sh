if [ "$STACDIR" == "" ]; then
  . setupstac.sh
else
  . $STACDIR/scripts/setupstac.sh
fi
if [ "$1" == "" ]; then
  par=stac.gui.StacGui
else
  # stac server:
  # stac.core.SERVER.StacSERVER [-background] [tango/tine]
  par=$@
fi
echo $par
echo "START..."
cd $RUNDIR
java -Dj3d.sharedstereozbuffer=true -Dj3d.stereo=PREFERRED -DSTAC_LOG_DIR=$STAC_LOG_DIR -DSTAC_WORK_DIR=$STAC_WORK_DIR -DBCMDEF=$BCMDEF -DSPECDEF=$SPECDEF -DGNSDEF=$GNSDEF -DSTACDIR=$STACDIR -DSTAC_DEF_MOS_SETT=$STAC_DEF_MOS_SETT -DSTAC_DEF_MOS_MAT=$STAC_DEF_MOS_MAT -DSTAC_DEF_HKL=$STAC_DEF_HKL -DSTAC_DEF_XDS=$STAC_DEF_XDS $par
