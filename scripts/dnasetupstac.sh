#stac settings
export STAC_DEF_MOS_SETT=$STACDIR/test/dataset1/mosflm/mosflm.inp
export STAC_DEF_MOS_MAT=$STACDIR/test/dataset1/mosflm/bestfile.par
export STAC_DEF_HKL=$STACDIR/test/dataset1/denzo/Peb3_dc_1_001.x
export STAC_DEF_XDS=$STACDIR/test/dataset1/xds/CORRECT.LP



#main classes
export stacjars="$STACDIR/plugins/ $STACDIR/bin/"
#thirdparty
export stacjars="`ls $STACDIR/thirdparty/jars/*.jar` $stacjars"
export stacjars="`ls $STACDIR/thirdparty/java3d/lib/ext/*.jar` $stacjars"
#ecpics plugin
export stacjars="$STACDIR/thirdparty/EpicsClient/linux-x86/jca2.1.2/jca.jar $stacjars"
#tango plugin
export stacjars="$STACDIR/thirdparty/TangoClient/TangORB-5.1.0.jar $STACDIR/thirdparty/TangoClient/jive-5.0.1.jar $stacjars"
#tine plugin
export stacjars="$STACDIR/thirdparty/TineClient/tine.jar $stacjars"
#xj3d
#export stacjars="$STACDIR/thirdparty/xj3dm10/jars/xj3dm10.jar $stacjars"
export stacjars="$STACDIR/thirdparty/xj3dm10/jars/linux.jar $STACDIR/thirdparty/xj3dm10/jars/dom4j.jar $STACDIR/thirdparty/xj3dm10/jars/HIDWrapper $STACDIR/thirdparty/xj3dm10/jars/vecmath.jar $STACDIR/thirdparty/xj3dm10/jars/gt2-main.jar $STACDIR/thirdparty/xj3dm10/jars/opengis.jar $STACDIR/thirdparty/xj3dm10/jars/units-0.01.jar $STACDIR/thirdparty/xj3dm10/jars/aviatrix3d-all.jar $STACDIR/thirdparty/xj3dm10/jars/dis.jar $STACDIR/thirdparty/xj3dm10/jars/gnu-regexp-1.0.8.jar $STACDIR/thirdparty/xj3dm10/jars/httpclient.jar $STACDIR/thirdparty/xj3dm10/jars/j3d-org-images.jar $STACDIR/thirdparty/xj3dm10/jars/j3d-org.jar $STACDIR/thirdparty/xj3dm10/jars/jinput.jar $STACDIR/thirdparty/xj3dm10/jars/jogl.jar $STACDIR/thirdparty/xj3dm10/jars/joal.jar $STACDIR/thirdparty/xj3dm10/jars/js.jar $STACDIR/thirdparty/xj3dm10/jars/dxinput.jar $STACDIR/thirdparty/xj3dm10/jars/jutils.jar $STACDIR/thirdparty/xj3dm10/jars/odejava.jar $STACDIR/thirdparty/xj3dm10/jars/uri.jar $STACDIR/thirdparty/xj3dm10/jars/vlc_uri.jar $stacjars"
export stacjars="$STACDIR/thirdparty/xj3dm10/jars/xj3d-common.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-core.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-eai.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-ecmascript.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-external-sai.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-j3d.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-java-sai.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-jaxp.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-jsai.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-net.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-norender.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-ogl.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-parser.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-render.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-runtime.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-sai.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-sav.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-script-base.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-xml-util.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-images.jar $STACDIR/thirdparty/xj3dm10/jars/xj3d-xml.jar $STACDIR/thirdparty/xj3dm10/jars/log4j.jar $stacjars"
#v4l4j
export stacjars="$STACDIR/thirdparty/jars/v4l4j/v4l4j.jar $stacjars"
#jogl
export stacjars="$STACDIR/thirdparty/jars/jogl/jogl.jar $STACDIR/thirdparty/jars/jogl/gluegen-rt $stacjars"
#jmf
export stacjars="$STACDIR/thirdparty/jars/jmf/jmf.jar $STACDIR/thirdparty/jars/jmf/fobs4jmf.jar $stacjars"

for stacjar in $stacjars; do
    if [ -r $stacjar ]; then
      if [ -z $CLASSPATH ]; then
        export CLASSPATH=$stacjar
      else
        export CLASSPATH=$stacjar:$CLASSPATH
      fi
    fi
done

#
# LD_LIBRARY_PATH for native libs (eg. EPICS)
#
export stacjavalibs=$STACDIR/thirdparty/EpicsClient/linux-x86/jca2.1.2/linux-x86/:$STACDIR/thirdparty/jars/jmf:$STACDIR/thirdparty/jars/jogl:$STACDIR/thirdparty/jars/v4l4j
if [ -z $LD_LIBRARY_PATH ]; then
	export LD_LIBRARY_PATH=$stacjavalibs
else
	export LD_LIBRARY_PATH=$stacjavalibs:$LD_LIBRARY_PATH
fi

ARCH=`uname -i`
if [ "$ARCH" == "x86_64" ]; then
	ARCH=amd64
fi
export LD_LIBRARY_PATH=$STACDIR/thirdparty/java3d/lib/$ARCH:$LD_LIBRARY_PATH

#
# DNA settings
#
export STAC_PARAMETERS=" -DSTACDIR="$STACDIR" -DBCMDEF="$BCMDEF
export DNAKAPPA=IS_ALREADY_SETUP


#printout
echo "PATH     =" $PATH
echo "CLASSPATH=" $CLASSPATH
echo "BCMDEF  =" $BCMDEF
echo "STAC_DEF_MOS_SETT=" $STAC_DEF_MOS_SETT
echo "STAC_DEF_MOS_MAT =" $STAC_DEF_MOS_MAT
echo "STAC_DEF_HKL     =" $STAC_DEF_HKL
echo "STAC_DEF_XDS     =" $STAC_DEF_XDS
echo "STACPARAMS    ="$STAC_PARAMETERS
echo "DNAKAPPA      ="$DNAKAPPA


