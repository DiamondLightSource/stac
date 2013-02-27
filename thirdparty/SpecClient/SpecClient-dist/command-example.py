import pprint
import cStringIO

from Tkinter import *
from Tkinter import _setit

from SpecClient.SpecClientError import SpecClientTimeoutError
from SpecClient import SpecWaitObject
from SpecClient import SpecConnectionsManager
from SpecClient.SpecCommand import SpecCommandA
from SpecClient import SpecEventsDispatcher


def change_state(widget, state=NORMAL):
   for w in widget.children.values():
      change_state(w, state)
      try:
         w.config(state=state)
      except TclError:
         pass
    

class CommandWindow(Toplevel, SpecCommandA):
   def __init__(self, parent):
      Toplevel.__init__(self, parent)
      SpecCommandA.__init__(self)

      self.lblCommand = Label(self, text="command")
      self.lblStatus = Label(self, text="status")
      self.cmdAbort = Button(self, text="abort")
      self.lblResult = Label(self, text="result")
      self.txtResult = Text(self)

      self.lblCommand.pack()
      self.lblStatus.pack()
      self.cmdAbort.pack()
      self.lblResult.pack()
      self.txtResult.pack()

      self.cmdAbort.configure(state=DISABLED)

      self.cmdAbort.bind("<ButtonRelease-1>", self.cmdAbortClicked)


   def connected(self):
      change_state(self, NORMAL)


   def disconnected(self):
      change_state(self, DISABLED)


   def beginWait(self):
      self.lblStatus.configure(text="waiting for reply...")


   def replyArrived(self, reply):
      self.cmdAbort.configure(state=DISABLED)
      
      if reply.error:
         self.lblStatus.configure(text="ERROR")
         self.txtResult.insert(END, "error_code %s" % reply.error_code)
      else:
         self.lblStatus.configure(text="OK")
         outputStream = cStringIO.StringIO()
         pprint.pprint(reply.getValue(), outputStream)
         self.txtResult.insert(END, outputStream.getvalue())

            
   def execute(self, specVersion, cmd):
      self.lblCommand.configure(text=cmd)
      self.lblStatus.configure(text="")
      self.cmdAbort.configure(state=NORMAL)
            
      self.connectToSpec(specVersion)
      self.executeCommand(cmd)


   def cmdAbortClicked(self, clickEvent):
      self.abort()


class MainWindow(Frame):
   def __init__(self, parent):
      Frame.__init__(self, parent)

      # variables
      self.connection = None #connection to Spec
      self.specVersion = StringVar() #string var containing the name 
      self.command = StringVar()
   
      # build GUI widgets
      self.topPanel = Frame(self)
      self.lblSpecVersion = Label(self.topPanel, text="Spec version (specify host:version string) :")
      self.txtSpecVersion = Entry(self.topPanel, textvariable = self.specVersion)
      self.cmdConnect = Button(self.topPanel, text="Connect")
      self.lblConnectionStatus = Label(self, text="connection status")
      self.bottomPanel = Frame(self)
      self.commandPanel = Frame(self.bottomPanel)
      self.lblExecuteCommand = Label(self.commandPanel, text="Command (press Return to execute) :")
      self.txtCommand = Entry(self.commandPanel, textvariable = self.command)


      # layout
      self.lblSpecVersion.pack(fill="x", expand = "no")
      self.txtSpecVersion.pack(fill="x", expand = "no")
      self.cmdConnect.pack(fill="x", expand = "yes")
      self.lblExecuteCommand.grid(row=0, sticky=W)
      self.txtCommand.grid(row=1)
      self.commandPanel.pack(fill="both", expand="yes")
      self.topPanel.pack(fill="both", expand="yes")
      self.lblConnectionStatus.pack(fill="x", expand="yes")
      self.bottomPanel.pack(fill="both", expand="yes")
      
      # configure
      change_state(self.bottomPanel, DISABLED)

      # callbacks
      self.cmdConnect.bind("<ButtonRelease-1>", self.cmdConnectClicked)
      self.txtCommand.bind("<Return>", self.txtCommandReturnPressed)


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


   def txtCommandReturnPressed(self, pressEvent):
      cmdWindow = CommandWindow(self)

      cmdWindow.execute(self.specVersion.get(), self.command.get())
      


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



