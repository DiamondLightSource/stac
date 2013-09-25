call setupstac.bat

set server=
set vbs=

:loop
  IF "%~1"=="-server" GOTO server
  IF "%~1"=="" GOTO embl
  set vbs=%1
  GOTO tovabb
:server
  set server=-server
  shift
  GOTO loop
:embl
  set vbs=vBCM.dat_esrf
:tovabb

echo "START..."
cd %RUNDIR%
java -Dj3d.sharedstereozbuffer=true -Dj3d.stereo=PREFERRED -Xmx256M -DSTAC_LOG_DIR=$STAC_LOG_DIR -DSTAC_WORK_DIR=$STAC_WORK_DIR -DBCMDEF=%BCMDEF% -DSTACDIR=%STACDIR% -DSTAC_DEF_MOS_SETT=%STAC_DEF_MOS_SETT% -DSTAC_DEF_MOS_MAT=%STAC_DEF_MOS_MAT% -DSTAC_DEF_HKL=%STAC_DEF_HKL% -DSTAC_DEF_XDS=%STAC_DEF_XDS% stac.vbcm.tango.vbcm_server %server% %vbs%
