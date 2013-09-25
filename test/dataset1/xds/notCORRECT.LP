
 ***** CORRECT *****  (XDS VERSION  December 2003)           26-Jul-2004

 INPUT PARAMETER VALUES
 ----------------------
 SPACE_GROUP_NUMBER=   19
 UNIT_CELL_CONSTANTS=    62.07    78.23   100.21  90.000  90.000  90.000
 FRIEDEL'S_LAW=TRUE 
 OVERLOAD=   65000     MINPK=  75.0    WFAC1=   1.0
 INCLUDE_RESOLUTION_RANGE=    50.000     0.000
 NAME_TEMPLATE_OF_DATA_FRAMES= ../Peb3_dc_1_???.mccd                            
 DATA_RANGE=       1       1
 ROTATION_AXIS=  0.999995 -0.000345 -0.003054
 OSCILLATION_RANGE=  0.500000
 STARTING_ANGLE=     0.000     STARTING_FRAME=       1
 X-RAY_WAVELENGTH=  0.979440
 INCIDENT_BEAM_DIRECTION=  0.001800  0.000199  1.020990
 FRACTION_OF_POLARIZATION=   0.980
 POLARIZATION_PLANE_NORMAL=  0.000000  1.000000  0.000000
 AIR=   0.00100
 DETECTOR=CCDCHESS                                          
 DIRECTION_OF_DETECTOR_X-AXIS=   1.00000   0.00000   0.00000
 DIRECTION_OF_DETECTOR_Y-AXIS=   0.00000   1.00000   0.00000
 DETECTOR_DISTANCE=   164.678
 ORGX=   1528.84  ORGY=   1543.52
 NX=  3072  NY=  3072    QX=  0.073240  QY=  0.073240
 BEAM_DIVERGENCE_E.S.D.=   0.036
 REFLECTING_RANGE_E.S.D.=   0.102
 MAXIMUM_ERROR_OF_SPOT_POSITION=   3.0
 REFERENCE_DATA_SET=                                                  

 *****  4 EQUIVALENT POSITIONS IN SPACE GROUP # 19 *****

    If x',y',z' is an equivalent position to x,y,z, then
        x'=x*ML(1)+y*ML( 2)+z*ML( 3)+ML( 4)/12.0
        y'=x*ML(5)+y*ML( 6)+z*ML( 7)+ML( 8)/12.0
        z'=x*ML(9)+y*ML(10)+z*ML(11)+ML(12)/12.0

    #    1  2  3  4    5  6  7  8    9 10 11 12 
    1    1  0  0  0    0  1  0  0    0  0  1  0
    2   -1  0  0  6    0 -1  0  0    0  0  1  6
    3    1  0  0  6    0 -1  0  6    0  0 -1  0
    4   -1  0  0  0    0  1  0  6    0  0 -1  6

     398 REFLECTIONS ON FILE "INTEGRATE.HKL"
       0 REFLECTIONS INCOMPLETE OR OUTSIDE IMAGE RANGE       1 ...       1
       0 OVERLOADED REFLECTIONS (IGNORED)
       0 REFLECTIONS OUTSIDE ACCEPTED RESOLUTION RANGES (IGNORED)
       0 REFLECTIONS REJECTED AS REQUESTED FROM FILE "REMOVE.HKL"
     398 REFLECTIONS ACCEPTED



 ******************************************************************************
  REFINEMENT OF DIFFRACTION PARAMETERS USING ALL IMAGES
 ******************************************************************************


 REFINED PARAMETERS:  DISTANCE BEAM ORIENTATION CELL                        
 USING     310 INDEXED SPOTS
 STANDARD DEVIATION OF SPOT    POSITION (PIXELS)     0.51
 STANDARD DEVIATION OF SPINDLE POSITION (DEGREES)    0.06
 CRYSTAL MOSAICITY (DEGREES)     0.102
 DIRECT BEAM COORDINATES (REC. ANGSTROEM)   0.001776  0.000096  1.020990
 DETECTOR COORDINATES (PIXELS) OF DIRECT BEAM    1532.85   1543.98
 DETECTOR ORIGIN (PIXELS) AT                     1528.93   1543.77
 CRYSTAL TO DETECTOR DISTANCE (mm)       164.84
 LAB COORDINATES OF DETECTOR X-AXIS  1.000000  0.000000  0.000000
 LAB COORDINATES OF DETECTOR Y-AXIS  0.000000  1.000000  0.000000
 LAB COORDINATES OF ROTATION AXIS  0.999995 -0.000345 -0.003054
 COORDINATES OF UNIT CELL A-AXIS   -20.107    58.420    -4.552
 COORDINATES OF UNIT CELL B-AXIS    59.003    23.846    45.419
 COORDINATES OF UNIT CELL C-AXIS    56.912    13.284   -80.907
 REC. CELL PARAMETERS   0.016142  0.012790  0.010019  90.000  90.000  90.000
 UNIT CELL PARAMETERS     61.951    78.185    99.807  90.000  90.000  90.000
 E.S.D. OF CELL PARAMETERS  4.5E-02 5.0E-02 8.4E-02 0.0E+00 0.0E+00 0.0E+00
 SPACE GROUP NUMBER     19
 CORRECTIONS FOR SPIRAL READ-OUT SCANNERS: ROFF=   0.000  TOFF=   0.000


 THE DATA COLLECTION STATISTICS REPORTED BELOW ASSUMES:
 SPACE_GROUP_NUMBER=   19
 UNIT_CELL_CONSTANTS=    61.95    78.18    99.81  90.000  90.000  90.000


 ******************************************************************************
        MEAN INTENSITY AS FUNCTION OF SPINDLE POSITION WITHIN DATA IMAGE
 ******************************************************************************

 This statistics could serve as a diagnostic tool for detecting
 shutter problems (suggested by Kay Diederichs).
 Data are corrected for this effect if PATCH_SHUTTER_PROBLEM=TRUE in XDS.INP.
 ===> Selected : PATCH_SHUTTER_PROBLEM=FALSE

 INTERVAL = Angular interval in units of the oscillation range
            covered by a data image.
 NUMBER   = Number of reflections in each interval
 INTENSITY= Mean intensity of the reflections in each interval
 FACTOR   = Correction factor applied to intensities;
            INTENSITY(overall)/INTENSITY(interval)

  INTERVAL    NUMBER      INTENSITY      FACTOR
  0.1  0.2         1        234.959       2.041
  0.2  0.3        49        460.314       1.042
  0.3  0.4        64        440.830       1.088
  0.4  0.5        48        409.521       1.171
  0.5  0.6        56        631.618       0.759
  0.6  0.7        60        367.552       1.305
  0.7  0.8        62        532.332       0.901
  0.8  0.9        53        528.487       0.907
  0.9  1.0         5        346.491       1.384
 
  0.0  1.0       398        479.475       1.000
 

 ******************************************************************************
                    DETERMINATION OF RECIPROCAL SCALING FACTORS
 ******************************************************************************

 *** RECIPROCAL SCALING FACTORS AS FUNCTION OF RESOLUTION AND FRAME NUMBER ***


 RECIPROCAL SCALING FACTORS FOR INPUT DATA SETS MERGED TO
 OUTPUT FILE: XDS_ASCII.HKL                                     



 !!! WARNING !!! THE PROGRAM IS UNABLE TO PERFORM THE REQUESTED CORRECTION.
 THE SCALED DATA SET PRODUCED IS VALID BUT WITHOUT CORRECTION.


 ******************************************************************************
    CORRECTION PARAMETERS FOR THE STANDARD ERROR OF REFLECTION INTENSITIES
 ******************************************************************************

 The initial estimate v0(I) for the variance of the reflection intensity I
 is replaced by v(I)=a*(v0(I)+b*I^2). The two constants a and b are chosen
 for each input data set to minimize the discrepancies between v(I) and the
 variance estimated from sample statistics of symmetry related reflections.

 NUMBER = number of reflections used to determine a and b

      a          b      NUMBER          INPUT DATA SET
  1.000E+00  0.000E+00       0  INTEGRATE.HKL                                     
 

 ******************************************************************************
      STANDARD ERROR OF REFLECTION INTENSITIES AS FUNCTION OF RESOLUTION
      FOR DATA SET  XDS_ASCII.HKL                                     
 ******************************************************************************

  I/Sigma  = mean intensity/Sigma of a reflection in shell
  Chi^2    = goodness of fit between sample variances of
             symmetry-related intensities and their errors
             (Chi^2 = 1 for perfect agreement)
  R-FACTOR
  observed = (SUM(ABS(I(h,i)-I(h))))/(SUM(I(h,i)))
  expected = expected R-FACTOR derived from Sigma(I)

   NUMBER  = total number of reflections in resolution shell
  ACCEPTED = number of accepted reflections used for
             calculation of R-FACTOR
  REJECTED = number of rejected reflections (MISFITS),
             recognized by comparison with symmetry-related
             reflections.

 RESOLUTION RANGE  I/Sigma  Chi^2  R-FACTOR  R-FACTOR  NUMBER ACCEPTED REJECTED
                                   observed  expected

    8.499   2.008    14.29   0.00    -99.90    -99.90     398       0       0
   --------------------------------------------------------------------------
    8.499   2.008    14.29   0.00    -99.90    -99.90     398       0       0
 


 ******************************************************************************
  SUMMARY OF DATA SET STATISTICS FOR VARIOUS SUBSETS OF INCLUDED DATA IMAGES
 ******************************************************************************

 Data set statistics is reported below several times, each with a different
 upper limit on the number of images included. This provides the user with
 the information for deciding which data images should be excluded from the
 final data set because of radiation damage or other defects. If the user
 decides for a subset of "good" images that differs from the specification
 as given by the input parameter (see DATA_RANGE= in XDS.INP) the CORRECT-
 step of XDS must be repeated with the new parameter values for DATA_RANGE=.


 ******************************************************************************
  STATISTICS OF SAVED DATA SET "XDS_ASCII.HKL" (DATA_RANGE=       1       1)
 FILE TYPE:         XDS_ASCII      MERGE=FALSE          FRIEDEL'S_LAW=TRUE 
 ******************************************************************************

   REFLECTIONS OF TYPE H,0,0  0,K,0  0,0,L OR EXPECTED TO BE ABSENT (*)
   --------------------------------------------------------------------

   H    K    L  RESOLUTION  INTENSITY     SIGMA    INTENSITY/SIGMA  #OBSERVED

    9    0    0     6.878   0.4656E+01  0.5350E+01         0.87           1*

 AVERAGE INTENSITY FOR       1 REFLECTIONS WHICH SHOULD
 BE SYSTEMATICALLY ABSENT IS     1.0% OF MEAN INTENSITY


          COMPLETENESS AND QUALITY OF DATA SET
          ------------------------------------

 R-FACTOR
 observed = (SUM(ABS(I(h,i)-I(h))))/(SUM(I(h,i)))
 expected = expected R-FACTOR derived from Sigma(I)

 COMPARED = number of reflections used for calculating R-FACTOR
 I/SIGMA  = mean of intensity/Sigma(I) of unique reflections
            (after merging symmetry-related observations)
 Sigma(I) = standard deviation of reflection intensity I
            estimated from sample statistics

 R-meas   = redundancy independent R-factor (intensities)
 Rmrgd-F  = quality of amplitudes (F) of this data set
            For definition of R-meas and Rmrgd-F see 
            Diederichs & Karplus (1997), Nature Struct. Biol. 4, 269-275.

 S_norm   = mean value of Sigma(I) for acentric reflections in
            each resolution shell assuming FRIEDEL'S_LAW=TRUE
 S_ano    = mean value of Sigma(I) for acentric reflections in
            each resolution shell assuming FRIEDEL'S_LAW=FALSE
            Thus, anomalous scattering contributions to
            the intensities are indicated by S_norm/S_ano>1


 SUBSET OF INTENSITY DATA WITH SIGNAL/NOISE >= -3.0 AS FUNCTION OF RESOLUTION
 RESOLUTION     NUMBER OF REFLECTIONS    COMPLETENESS R-FACTOR  R-FACTOR COMPARED I/SIGMA   R-meas  Rmrgd-F  S_norm/
   LIMIT     OBSERVED  UNIQUE  POSSIBLE     OF DATA   observed  expected                                     S_ano

     5.01          23      23      2292        1.0%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     3.90          30      30      2448        1.2%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     3.30          32      32      2966        1.1%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.91          44      44      3387        1.3%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.64          37      37      3745        1.0%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.43          50      50      4081        1.2%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.26          43      43      4420        1.0%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.12          68      68      4675        1.5%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.01          70      70      4966        1.4%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
    total         397     397     32980        1.2%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00

 SUBSET OF INTENSITY DATA WITH SIGNAL/NOISE >=  0.0 AS FUNCTION OF RESOLUTION
 RESOLUTION     NUMBER OF REFLECTIONS    COMPLETENESS R-FACTOR  R-FACTOR COMPARED I/SIGMA   R-meas  Rmrgd-F  S_norm/
   LIMIT     OBSERVED  UNIQUE  POSSIBLE     OF DATA   observed  expected                                     S_ano

     5.01          23      23      2292        1.0%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     3.90          30      30      2448        1.2%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     3.30          32      32      2966        1.1%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.91          44      44      3387        1.3%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.64          37      37      3745        1.0%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.43          48      48      4081        1.2%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.26          43      43      4420        1.0%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.12          68      68      4675        1.5%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.01          70      70      4966        1.4%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
    total         395     395     32980        1.2%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00

 SUBSET OF INTENSITY DATA WITH SIGNAL/NOISE >=  3.0 AS FUNCTION OF RESOLUTION
 RESOLUTION     NUMBER OF REFLECTIONS    COMPLETENESS R-FACTOR  R-FACTOR COMPARED I/SIGMA   R-meas  Rmrgd-F  S_norm/
   LIMIT     OBSERVED  UNIQUE  POSSIBLE     OF DATA   observed  expected                                     S_ano

     5.01          23      23      2292        1.0%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     3.90          30      30      2448        1.2%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     3.30          32      32      2966        1.1%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.91          40      40      3387        1.2%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.64          33      33      3745        0.9%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.43          45      45      4081        1.1%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.26          36      36      4420        0.8%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.12          52      52      4675        1.1%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.01          52      52      4966        1.0%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
    total         343     343     32980        1.0%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00

 SUBSET OF INTENSITY DATA WITH SIGNAL/NOISE >=  6.0 AS FUNCTION OF RESOLUTION
 RESOLUTION     NUMBER OF REFLECTIONS    COMPLETENESS R-FACTOR  R-FACTOR COMPARED I/SIGMA   R-meas  Rmrgd-F  S_norm/
   LIMIT     OBSERVED  UNIQUE  POSSIBLE     OF DATA   observed  expected                                     S_ano

     5.01          23      23      2292        1.0%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     3.90          30      30      2448        1.2%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     3.30          31      31      2966        1.0%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.91          36      36      3387        1.1%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.64          30      30      3745        0.8%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.43          38      38      4081        0.9%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.26          26      26      4420        0.6%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.12          39      39      4675        0.8%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.01          34      34      4966        0.7%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
    total         287     287     32980        0.9%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00

 SUBSET OF INTENSITY DATA WITH SIGNAL/NOISE >=  9.0 AS FUNCTION OF RESOLUTION
 RESOLUTION     NUMBER OF REFLECTIONS    COMPLETENESS R-FACTOR  R-FACTOR COMPARED I/SIGMA   R-meas  Rmrgd-F  S_norm/
   LIMIT     OBSERVED  UNIQUE  POSSIBLE     OF DATA   observed  expected                                     S_ano

     5.01          23      23      2292        1.0%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     3.90          30      30      2448        1.2%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     3.30          30      30      2966        1.0%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.91          31      31      3387        0.9%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.64          25      25      3745        0.7%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.43          32      32      4081        0.8%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.26          18      18      4420        0.4%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.12          28      28      4675        0.6%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
     2.01          23      23      4966        0.5%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00
    total         240     240     32980        0.7%     -99.9%    -99.9%        0  -99.00   -99.9%   -99.9%    0.00


 NUMBER OF REFLECTIONS IN SELECTED SUBSET OF IMAGES     398
 NUMBER OF REJECTED MISFITS                               0
 NUMBER OF SYSTEMATIC ABSENT REFLECTIONS                  1
 NUMBER OF ACCEPTED OBSERVATIONS                        397
 NUMBER OF UNIQUE ACCEPTED REFLECTIONS                  397

 NUMBER OF REFLECTIONS SAVED ON "XDS_ASCII.HKL"         398
 (ACCEPTED OBSERVATIONS + REJECTED MISFITS + ABSENCES;
  MISFITS are marked by -1*SIGMA(IOBS) in "XDS_ASCII.HKL")



 ******************************************************************************
    WILSON STATISTICS OF DATA SET  "XDS_ASCII.HKL"
 ******************************************************************************

 Data is divided into resolution shells and a straight line 
 A - 2*B*SS is fitted to log<I>, where
   RES    = mean resolution (Angstrom) in shell
   SS     = mean of (sin(THETA)/LAMBDA)**2 in shell
   <I>    = mean reflection intensity in shell
   BO     = (A - log<I>)/(2*SS)
    #     = number of reflections in resolution shell

   WILSON LINE (using all data) : A=   7.425 B=  20.256 CORRELATION=  0.90
      #      RES      SS        <I>       log(<I>)       BO
      10     7.042   0.005    1552.863       7.348       7.7
      10     5.799   0.007     905.207       6.808      41.5
      14     4.781   0.011    1403.227       7.247       8.2
      17     4.181   0.014    1140.323       7.039      13.5
      12     3.758   0.018    1068.978       6.974      12.7
      14     3.514   0.020    1183.142       7.076       8.6
      29     3.238   0.024     681.961       6.525      18.9
      13     3.048   0.027     316.310       5.757      31.0
      22     2.895   0.030     320.090       5.769      27.8
      15     2.756   0.033     282.337       5.643      27.1
      17     2.641   0.036     531.219       6.275      16.0
      26     2.535   0.039     370.968       5.916      19.4
      18     2.455   0.041     247.300       5.511      23.1
      30     2.361   0.045     140.923       4.948      27.6
      20     2.267   0.049     275.417       5.618      18.6
      31     2.208   0.051     304.114       5.717      16.7
      31     2.143   0.054     181.558       5.202      20.4
      32     2.088   0.057     229.716       5.437      17.3
      35     2.033   0.061     156.156       5.051      19.6


 HIGHER ORDER MOMENTS OF WILSON DISTRIBUTION OF  CENTRIC DATA
    AS COMPARED WITH THEORETICAL VALUES. (EXPECTED: 1.00)    
      #      RES        <I**2>/      <I**3>/      <I**4>/  
                         3<I>**2     15<I>**3    105<I>**4 

       1     5.799        0.949        0.320        0.077
       2     4.781        1.280        0.646        0.242
       1     3.758        2.517        1.383        0.543
       1     3.514        0.001        0.000        0.000
       3     3.238        0.077        0.013        0.001
       2     3.048        0.215        0.045        0.007
       1     2.895        0.049        0.004        0.000
       1     2.641        0.100        0.011        0.001
       1     2.535        1.372        0.557        0.161
       1     2.361        0.052        0.004        0.000
       1     2.267        1.552        0.670        0.206
       3     2.143        0.749        0.346        0.120
      18   overall        0.670        0.300        0.103


 HIGHER ORDER MOMENTS OF WILSON DISTRIBUTION OF ACENTRIC DATA
    AS COMPARED WITH THEORETICAL VALUES. (EXPECTED: 1.00)    
      #      RES        <I**2>/      <I**3>/      <I**4>/  
                         2<I>**2      6<I>**3     24<I>**4 

      10     7.042        1.413        1.982        2.312
       9     5.799        0.745        0.579        0.397
      12     4.781        0.682        0.482        0.297
      17     4.181        0.797        0.528        0.296
      11     3.758        0.636        0.409        0.215
      13     3.514        0.776        0.464        0.242
      26     3.238        1.255        1.370        1.332
      11     3.048        0.744        0.424        0.202
      21     2.895        0.975        0.766        0.494
      15     2.756        0.867        0.670        0.459
      16     2.641        0.996        0.825        0.572
      25     2.535        0.841        0.632        0.399
      18     2.455        0.768        0.463        0.229
      29     2.361        0.838        0.548        0.297
      19     2.267        0.692        0.437        0.233
      31     2.208        1.153        1.329        1.398
      28     2.143        0.946        0.927        0.888
      32     2.088        0.983        0.818        0.575
      35     2.033        0.892        0.816        0.742
     378   overall        0.918        0.794        0.645

   ======= CUMULATIVE INTENSITY DISTRIBUTION =======
 DEFINITIONS:
   <I>    = mean reflection intensity
 Na(Z)exp = expected number of acentric reflections with I <= Z*<I>
 Na(Z)obs = observed number of acentric reflections with I <= Z*<I>
 Nc(Z)exp = expected number of  centric reflections with I <= Z*<I>
 Nc(Z)obs = observed number of  centric reflections with I <= Z*<I>



 Nc(Z)obs/Nc(Z)exp versus resolution and Z (0.1-1.0)
      #      RES     0.1   0.2   0.3   0.4   0.5   0.6   0.7   0.8   0.9   1.0

       1     5.799  0.00  0.00  0.00  0.00  0.00  0.00  0.00  0.00  0.00  0.00
       2     4.781  0.00  0.00  0.00  0.00  0.00  0.00  0.00  0.80  0.76  0.73
       1     3.758  0.00  0.00  0.00  0.00  0.00  0.00  0.00  0.00  0.00  0.00
       1     3.514  4.03  2.89  2.40  2.11  1.92  1.78  1.67  1.59  1.52  1.47
       3     3.238  2.68  1.93  1.60  1.41  1.28  1.19  1.12  1.06  1.52  1.47
       2     3.048  0.00  0.00  1.20  1.06  0.96  0.89  0.84  0.80  0.76  0.73
       1     2.895  0.00  0.00  0.00  2.11  1.92  1.78  1.67  1.59  1.52  1.47
       1     2.641  0.00  0.00  0.00  0.00  0.00  1.78  1.67  1.59  1.52  1.47
       1     2.535  0.00  0.00  0.00  0.00  0.00  0.00  0.00  0.00  0.00  0.00
       1     2.361  0.00  0.00  0.00  2.11  1.92  1.78  1.67  1.59  1.52  1.47
       1     2.267  0.00  0.00  0.00  0.00  0.00  0.00  0.00  0.00  0.00  0.00
       3     2.143  0.00  0.00  0.00  0.00  0.64  0.59  1.12  1.06  1.01  0.98
      18   overall  0.67  0.48  0.53  0.70  0.75  0.79  0.84  0.88  0.93  0.90


 Na(Z)obs/Na(Z)exp versus resolution and Z (0.1-1.0)
      #      RES     0.1   0.2   0.3   0.4   0.5   0.6   0.7   0.8   0.9   1.0

      10     7.042  1.05  1.65  1.16  0.91  1.02  1.55  1.39  1.27  1.18  1.11
       9     5.799  0.00  0.00  0.86  1.01  0.85  0.99  0.88  1.01  0.94  1.05
      12     4.781  0.88  0.92  0.64  0.76  1.06  0.92  1.16  1.06  0.98  1.05
      17     4.181  0.00  0.65  1.13  1.07  0.90  0.91  0.82  0.75  0.69  0.93
      11     3.758  0.00  1.00  1.05  1.10  1.16  1.41  1.26  1.16  1.07  1.15
      13     3.514  0.81  0.42  0.30  0.23  0.20  0.34  0.61  0.70  0.78  0.97
      26     3.238  0.40  0.64  1.19  1.28  1.17  1.19  1.15  1.12  1.04  0.97
      11     3.048  0.00  0.00  0.00  0.00  0.46  0.60  0.72  0.99  0.92  1.01
      21     2.895  1.00  1.05  0.92  1.01  1.09  1.06  1.14  1.04  1.12  1.05
      15     2.756  0.70  0.74  0.77  1.01  1.02  0.89  1.06  0.97  0.90  0.84
      16     2.641  1.31  1.03  0.96  0.95  1.11  1.11  0.99  1.13  1.05  0.99
      25     2.535  0.84  1.10  0.77  0.85  1.12  1.24  1.11  1.09  1.08  1.01
      18     2.455  0.58  0.61  0.86  1.01  0.99  0.86  0.88  0.81  0.84  0.79
      29     2.361  1.09  1.14  0.93  0.84  0.88  0.84  0.96  0.88  0.87  0.82
      19     2.267  0.55  0.29  0.61  0.64  0.67  0.82  0.94  0.96  1.06  1.17
      31     2.208  1.02  1.60  1.12  1.17  1.07  1.07  1.03  1.00  1.20  1.12
      28     2.143  0.75  1.38  0.96  0.97  1.00  0.95  0.92  0.91  0.96  0.90
      32     2.088  1.31  1.55  1.45  1.33  1.11  1.04  0.93  0.91  1.00  0.94
      35     2.033  0.60  0.63  0.77  0.78  0.80  1.01  0.91  0.88  0.96  0.90
     378   overall  0.75  0.95  0.92  0.94  0.95  1.00  0.99  0.97  0.99  0.98

 cpu time used                  0.1 sec
 elapsed wall-clock time        0.4 sec
