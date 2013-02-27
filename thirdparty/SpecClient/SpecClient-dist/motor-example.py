import pprint
import cStringIO

from Tkinter import *
from Tkinter import _setit

from SpecClient.SpecClientError import SpecClientTimeoutError
from SpecClient import SpecWaitObject
from SpecClient import SpecConnectionsManager
from SpecClient.SpecMotor import SpecMotorA
from SpecClient import SpecEventsDispatcher
from SpecClient import Spec


def change_state(widget, state=NORMAL):
   for w in widget.children.values():
      change_state(w, state)
      try:
         w.config(state=state)
      except TclError:
         pass


class MotorWindow(Toplevel, SpecMotorA):
   def __init__(self,parent):
       Toplevel.__init__(self, parent)
       SpecMotorA.__init__(self)

       self.newpos = DoubleVar()

       self.lblMotorStatus = Label(self, text="-", background="white")
       self.txtPosition = Entry(self, textvariable = self.newpos)
       self.cmdGo = Button(self, text="Go")

       self.lblMotorStatus.grid(row = 0, columnspan = 2)
       self.txtPosition.grid(row=1)
       self.cmdGo.grid(row=1, column=1)

       self.cmdGo.bind('<ButtonRelease-1>', self.cmdGoClicked)


   def setMotor(self, motor_mne, specversion):
      """establish a connection to Spec ;

      after that, the SpecMotorA-derived object is fully initialized
      """
      self.connectToSpec(motor_mne, specversion)
      
      
   def cmdGoClicked(self, event):
      """user clicked on the [Go] button ;

      the SpecMotorA.move method is called to move to the new position
      """
      self.move(self.newpos.get())
   

   def motorPositionChanged(self, position):
      """called while the motor is moving"""
      self.lblMotorStatus.configure(text="%s : %s" % (self.specName, self.getPosition()))
      
      
   def motorStateChanged(self, status):
      """called when the motor status has changed

      possible values for status are :
        0 -- NOTINITIALIZED
        1 -- UNUSABLE
        2 -- READY
        3 -- MOVESTARTED
        4 -- MOVING
        5 -- ONLIMIT

      (see SpecMotor.py file)
      """
      colorTable = ["white", "grey", "green", "yellow", "yellow", "red"]
      self.lblMotorStatus.configure(background=colorTable[status])

      if status < 2:
         self.cmdGo.configure(state=DISABLED)
      else:
         self.cmdGo.configure(state=NORMAL)
      

class BlissOptionMenu(OptionMenu):
    def __init__(self, *args, **kw):
        self._command = kw.get("command")
        OptionMenu.__init__(self, *args, **kw)


    def addOption(self, label, variable):
        self["menu"].add_command(label=label,
            command=_setit(variable, label, self._command))
        

class MainWindow(Frame):
   def __init__(self, parent):
      Frame.__init__(self, parent)

      # variables
      self.connection = None #connection to Spec
      self.specVersion = StringVar() #string var containing the name 
      self.currentMotor = StringVar()
     
      # build GUI widgets
      self.topPanel = Frame(self)
      self.lblSpecVersion = Label(self.topPanel, text="Spec version (specify host:version string) :")
      self.txtSpecVersion = Entry(self.topPanel, textvariable = self.specVersion)
      self.cmdConnect = Button(self.topPanel, text="Connect")
      self.lblConnectionStatus = Label(self, text="connection status")
      self.bottomPanel = Frame(self)
      self.motorsPanel = Frame(self.bottomPanel)
      self.cmdGetMotors = Button(self.motorsPanel, text="Get motors list")
      self.lblMotors = Label(self.motorsPanel, text="Motors available :")
      self.lstMotors = BlissOptionMenu(self.motorsPanel, self.currentMotor, (""))
      self.cmdDisplayMotor = Button(self.motorsPanel, text="display motor")
     

      # layout
      self.lblSpecVersion.pack(fill="x", expand = "no")
      self.txtSpecVersion.pack(fill="x", expand = "no")
      self.cmdConnect.pack(fill="x", expand = "yes")
      self.cmdGetMotors.grid(row=0, columnspan = 2)
      self.lblMotors.grid(row=1, sticky=W)
      self.lstMotors.grid(row=2, column=0, sticky=W)
      self.cmdDisplayMotor.grid(row=2, column=1, sticky=E)
      self.motorsPanel.pack(fill="both", expand="yes")
      self.topPanel.pack(fill="both", expand="yes")
      self.lblConnectionStatus.pack(fill="x", expand="yes")
      self.bottomPanel.pack(fill="both", expand="yes")
      
      # configure
      change_state(self.bottomPanel, DISABLED)

      # callbacks
      self.cmdConnect.bind("<ButtonRelease-1>", self.cmdConnectClicked)
      self.cmdGetMotors.bind("<ButtonRelease-1>", self.cmdGetMotorsClicked)
      self.cmdDisplayMotor.bind("<ButtonRelease-1>", self.cmdDisplayMotorClicked)


   def specConnected(self):
      self.lblConnectionStatus.configure(text="connected :-)")
      change_state(self.bottomPanel, NORMAL)
      change_state(self.topPanel, DISABLED)


   def specDisconnected(self):
      self.lblConnectionStatus.configure(text="disconnected :-)")
      change_state(self.topPanel, NORMAL)
      change_state(self.bottomPanel, DISABLED)


   def cmdConnectClicked(self, clickEvent):
      self.connection = SpecConnectionsManager.SpecConnectionsManager().getConnection(self.specVersion.get())

      SpecEventsDispatcher.connect(self.connection, 'connected', self.specConnected)
      SpecEventsDispatcher.connect(self.connection, 'disconnected', self.specDisconnected)

      try:
         SpecWaitObject.waitConnection(self.connection, timeout=1000) #1 s. timeout
      except SpecClientTimeoutError:
         self.lblConnectionStatus.configure(text="timeout !")


   def cmdGetMotorsClicked(self, clickEvent):         
      remote_spec = Spec.Spec(self.specVersion.get())
      motors_mne = remote_spec.getMotorsMne()
        
      for mne in motors_mne:
         self.lstMotors.addOption(mne, self.currentMotor)

      if len(motors_mne) > 0:
         self.currentMotor.set(motors_mne[0])
      else:
         self.currentMotor.set("")
            

   def cmdDisplayMotorClicked(self, clickEvent):
      motorWindow = MotorWindow(self)

      motorWindow.setMotor(self.currentMotor.get(), self.specVersion.get())
    


if __name__ == '__main__':  
   root = Tk()
   root.title("SpecClient demo")

   def poll():
      SpecEventsDispatcher.dispatch()
      root.after(20, poll)

   root.after(20, poll)

   main = MainWindow(root)
   main.pack(fill="both", expand = "yes")

   root.mainloop()



