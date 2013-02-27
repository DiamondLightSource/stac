#from SpecClient import *

i=0
while (i<100):
  i=i+1
  if i%2 == 0:
    mot="phi"
  else: 
    mot="kap1"
  from SpecClient import *
  cmd=SpecMotor.SpecMotor(mot,'artemis2:sandor')
  print mot,cmd.getPosition()

