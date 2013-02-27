
REM install
set STACDIR=/scratch/STAC
set BCMDEF=%STACDIR%\config\BCM.dat
set RUNDIR=%STACDIR%\
REM  common
echo "Starting Paramteres:"
echo "STACDIR  =" %STACDIR%
echo "JDK      =" %JDK%
echo "PATH     =" %PATH%

rem stac settings
set STAC_DEF_MOS_SETT=%STACDIR%\test\dataset1\mosflm\mosflm.inp
set STAC_DEF_MOS_MAT=%STACDIR%\test\dataset1\mosflm\Peb3_dc_1_001.mat
set STAC_DEF_HKL=%STACDIR%\test\dataset1\denzo\Peb3_dc_1_001.x
set STAC_DEF_XDS=%STACDIR%\test\dataset1\xds\CORRECT.LP



rem main classes
set CLASSPATH=%STACDIR%\STAC.jar;%STACDIR%\plugins\;%CLASSPATH%
rem jama
set CLASSPATH=%STACDIR%\thirdparty\jars\Jama-1.0.1.jar;%CLASSPATH%
rem sutil
set CLASSPATH=%STACDIR%\thirdparty\jars\SUtil-1.0.jar;%CLASSPATH%
rem bioxdm
set CLASSPATH=%STACDIR%\thirdparty\jars\bioxdm.jar;%CLASSPATH%
rem commons;icu4j;jasypt
set CLASSPATH=%STACDIR%\thirdparty\jars\commons-codec-1.3.jar;%STACDIR%\thirdparty\jars\commons-lang-2.3.jar;%STACDIR%\thirdparty\jars\icu4j-3_8_1.jar;%STACDIR%\thirdparty\jars\jasypt-1.4.jar;%CLASSPATH%
rem ecpics plugin
set CLASSPATH=%STACDIR%\thirdparty\EpicsClient\linux-x86\jca2.1.2\jca.jar;%CLASSPATH%
rem tango plugin
set CLASSPATH=%STACDIR%\thirdparty\TangoClient\TangORB-5.1.0.jar;%STACDIR%\thirdparty\TangoClient\jive-5.0.1.jar;%CLASSPATH%
rem tine plugin
set CLASSPATH=%STACDIR%\thirdparty\TineClient\tine.jar;%CLASSPATH%
rem xj3d
rem export %CLASSPATH%="%STACDIR%\thirdparty\xj3dm10\jars\xj3dm10.jar $%CLASSPATH%"
set CLASSPATH=%STACDIR%\thirdparty\xj3dm10\jars\linux.jar;%STACDIR%\thirdparty\xj3dm10\jars\dom4j.jar;%STACDIR%\thirdparty\xj3dm10\jars\HIDWrapper;%STACDIR%\thirdparty\xj3dm10\jars\vecmath.jar;%STACDIR%\thirdparty\xj3dm10\jars\gt2-main.jar;%STACDIR%\thirdparty\xj3dm10\jars\opengis.jar;%STACDIR%\thirdparty\xj3dm10\jars\units-0.01.jar;%STACDIR%\thirdparty\xj3dm10\jars\aviatrix3d-all.jar;%STACDIR%\thirdparty\xj3dm10\jars\dis.jar;%STACDIR%\thirdparty\xj3dm10\jars\gnu-regexp-1.0.8.jar;%STACDIR%\thirdparty\xj3dm10\jars\httpclient.jar;%STACDIR%\thirdparty\xj3dm10\jars\j3d-org-images.jar;%STACDIR%\thirdparty\xj3dm10\jars\j3d-org.jar;%STACDIR%\thirdparty\xj3dm10\jars\jinput.jar;%STACDIR%\thirdparty\xj3dm10\jars\jogl.jar;%STACDIR%\thirdparty\xj3dm10\jars\joal.jar;%STACDIR%\thirdparty\xj3dm10\jars\js.jar;%STACDIR%\thirdparty\xj3dm10\jars\dxinput.jar;%STACDIR%\thirdparty\xj3dm10\jars\jutils.jar;%STACDIR%\thirdparty\xj3dm10\jars\odejava.jar;%STACDIR%\thirdparty\xj3dm10\jars\uri.jar;%STACDIR%\thirdparty\xj3dm10\jars\vlc_uri.jar;%CLASSPATH%
set CLASSPATH=%STACDIR%\thirdparty\xj3dm10\jars\xj3d-common.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-core.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-eai.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-ecmascript.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-external-sai.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-j3d.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-java-sai.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-jaxp.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-jsai.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-net.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-norender.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-ogl.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-parser.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-render.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-runtime.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-sai.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-sav.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-script-base.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-xml-util.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-images.jar;%STACDIR%\thirdparty\xj3dm10\jars\xj3d-xml.jar;%STACDIR%\thirdparty\xj3dm10\jars\log4j.jar;%CLASSPATH%
rem 
rem  DNA settings
rem 
set STAC_PARAMETERS=" -DSTACDIR="%STACDIR%" -DBCMDEF="%BCMDEF%
set DNAKAPPA=IS_ALREADY_SETUP

rem  EDNA settings
set CLASSPATH=%STACDIR%\thirdparty\jars\edna_XSData.jar;%CLASSPATH%


rem printout
echo "PATH     =" %PATH%
echo "CLASSPATH=" %CLASSPATH%
echo "BCMDEF  =" %BCMDEF%
echo "STAC_DEF_MOS_SETT=" %STAC_DEF_MOS_SETT%
echo "STAC_DEF_MOS_MAT =" %STAC_DEF_MOS_MAT%
echo "STAC_DEF_HKL     =" %STAC_DEF_HKL%
echo "STAC_DEF_XDS     =" %STAC_DEF_XDS%
echo "STACPARAMS    ="%STAC_PARAMETERS%
echo "DNAKAPPA      ="%DNAKAPPA%



rem dna conflicting classes
set CLASSPATH=%STACDIR%\thirdparty\jars\dna.jar;%STACDIR%\thirdparty\jars\castor-0.9.3.19-dna.jar %STACDIR%\thirdparty\jars\gnu-regexp-1.1.4.jar %STACDIR%\thirdparty\jars\xerces.jar;%CLASSPATH%

