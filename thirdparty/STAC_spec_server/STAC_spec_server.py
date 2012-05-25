#!/usr/bin/env python

import threading, string, os, sys
import time
import imp
import SimpleHTTPServer, BaseHTTPServer, httplib

global filename
global inaction
global f_out
global pid

class Redirect:
  def __init__(self,old_stdout,new_stdout):
      self.old_stdout=old_stdout
      self.new_stdout=new_stdout
      
  def write(self,s):
      ##f_out.write( s )
      f_out.flush()
      #self.old_stdout.write(s)
      #self.new_stdout.write(s)
      #writing to string:
      self.new_stdout=self.new_stdout+s
      #self.old_stdout.write("res:\n"+self.new_stdout+"\nres END\n")
      
  def flush(self):
      self.old_stdout.flush()
      #self.new_stdout.flush()
    
  def update(self):
      return self.new_stdout
      
class STAC_spec_Handler(SimpleHTTPServer.SimpleHTTPRequestHandler):
    
  def do_QUIT (self):
        """send 200 OK response, and set server.stop to True"""
        self.send_response(200)
        self.end_headers()
        self.server.stop = True
        
  def do_GET (self):
        """send 200 OK response, and set server.stop to True"""
        self.send_response(200)
        self.end_headers()
        self.server.stop = True

  def do_POST(self):
    #
    # Get incoming XML document
    #
    try:
      content_length_string = self.headers.getheader("Content-Length")
      if content_length_string is None:
        raise "BadRequestError"
      xml_message_length = string.atoi(content_length_string)
      xml_message = self.rfile.read(xml_message_length)
      if xml_message=="KillServer":
        self.server.stop=True
        f_out.write("killed!!!")
        f_out.flush()
        os.abort()
      xml_response = None
      #
      #  handle the request
      #
      moduleName = os.path.basename(filename)[:-3]+'_out'
      modulePath = os.path.dirname(filename)
     
      try:
       f_out.write( "Action req received" )
       ##self.wfile.write("Action req received\n")
       #while inaction==1:
       #  time.sleep(0.01)
       #inaction=1
       #print "Action..."
       #debug
       #print xml_message
       #self.wfile.write(xml_message)
       #end debug
       #write out the command because we execute it by loading as a new module
       #First, we have to remove the object code from the prev compilation
       #try:
       #  os.remove(filename[:-3]+"_out.pyc")
       #except:
       #  filewas_not_there=1
       #the we can write out the code
       #f=open(filename[:-3]+"_out.py", 'w')
       #f.write(xml_message)
       #f.flush()
       #os.fsync(f.fileno())
       #f.close()
       #end writting out the command
       #redirect the output of the execution
       #sys.stdout = f
       old_stdout=sys.stdout
       sys.stdout.flush()
       #sys.stdout = self.wfile
       #sys.stdout=Redirect(sys.stdout,self.wfile)
       #to string:
       xml_response=""
       sys.stdout=Redirect(sys.stdout,xml_response)
       try:
         problem=0
         try:  
           #module = imp.load_module(moduleName, *imp.find_module(moduleName, [modulePath]))
           exec "global m,cmd\n"+xml_message
         except:
           test12=12
           f_out.write( "Sorry could not execute: \n %s" % xml_message )
         #print "Import finished %s" % moduleName
         #f.flush()
         #os.fsync(f.fileno())
         #f.close()
         sys.stdout.flush()
         xml_response=sys.stdout.update() 
         sys.stdout = old_stdout
         #sys.stdout = sys.__stdout__
         #if problem==1:
         #  print "Sorry could not import %s" % moduleName             
       except:
         sys.stdout.flush()
         sys.stdout = old_stdout
         #sys.stdout = sys.__stdout__
         f_out.write( "Spec Exception happened" )
       sys.stdout.flush()
       sys.stdout = old_stdout
       #sys.stdout = sys.__stdout__
       #self.wfile.flush()
       f_out.write( "Action performed" )
      except:
       test12=12
       f_out.write( "General Exception happened" )
      f_out.write( "Action done" )
      inaction=0
      
      #
      #
      #
      if xml_response is None:
        #
        # Send back bad status
        #
        xml_response       = "1"
        #
        #
      #
      # Everything OK so far.
      #
      self.wfile.write("HTTP/1.1 200 OK\n")
      #print "XML_RESPONSE:\n"+xml_response
      #
      #
      #

    except "IternalServerError":
      self.wfile.write("HTTP/1.1 500 Internal Server Error\n")

    except "BadRequestError":
      self.wfile.write("HTTP/1.1 400 Bad Request\n")

    #
    # Only send back response if command is not "/shutdown"
    #
    if self.path != "/shutdown":
      #
      # Name of the ES server
      #
      self.wfile.write("Content-Type: text/xml\n")
      self.wfile.write("Content-Length: %d\n\n"%(len(xml_response)))
      self.wfile.write(xml_response)
    #self.wfile.close()


class STAC_spec_Server (BaseHTTPServer.HTTPServer):
    """http server that reacts to self.stop flag"""

    def serve_until_stopped(self):
        """Handle one request at a time until stopped."""
        self.stop = False
        while not self.stop:
            self.handle_request()

class HTTPServerThread(threading.Thread):

  def __init__(self, hostname, port):
    self.hostname = hostname
    self.port = port
    self.is_shutdown = 0
    server_class=STAC_spec_Server
    handler_class=STAC_spec_Handler
    server_address = (self.hostname, self.port)
    starting_port=self.port
    self.httpd_started=0
    while (self.httpd_started==0) and (self.port<starting_port+1000):
      try:
        server_address = (self.hostname, self.port)
        self.httpd = server_class(server_address, handler_class)
        self.httpd_started=1
      except:
        self.port=self.port+1
    if self.httpd_started==0:
      print "HTTP Server could not be started"
      self.is_shutdown = 1
    else:
      self.server_thread=threading.Thread(target=self.serving)
      self.server_thread.start()
    
  def serving(self):  
    self.httpd.serve_until_stopped()

  def shutdown(self):
    self.is_shutdown = 1
    conn = httplib.HTTPConnection("%s:%d"%(self.hostname, self.port))
    conn.request("QUIT", "/")
    conn.getresponse()

def http_post(server_name, port, message):
    h = httplib.HTTPConnection("%s:%d"%(server_name, port))
    h.putrequest("POST", "/")
    h.putheader("Content-type", "text/xml")
    h.putheader("Content-length", "%d" % len(message))
    h.putheader('Accept', 'text/plain')
    h.endheaders()
    h.send(message)
    #reply, msg, hdrs = h.getreply()
    #data = h.getfile().read(1)
    data=h.getresponse().read()
    return data
 
if __name__ == '__main__':
  try:
    directory = sys.argv[1]
    filename = sys.argv[2]
  except:
    print "Usage: %s <directory> <filename to work on>" % sys.argv[0]
    sys.exit(1)
 
  try:
    os.remove(filename[:-3]+".port_ready")
  except:
    inaction=0 #dummy
    
  f_outS=filename[:-3]+".log"
  try:
    os.remove(f_outS)
  except:
    inaction=0 #dummy
  f_out=open(f_outS,"w")
    
  directory = os.path.abspath(directory)
  filename = os.path.join(directory, filename)

  inaction=0

  if not os.path.isdir(directory):
    print "%s is not a directory !" % directory
    sys.exit(1)
  _server=HTTPServerThread('localhost',8088) #'localhost',8088)
  #write out the port number used
  f=open(filename[:-3]+".port", 'w')
  f.write("http://%s:%d"%(_server.hostname,_server.port))
  f.flush()
  os.fsync(f.fileno())
  f.close()
  #
  # test the connection
  #
  f_out.write( "Testing the connection..." )
  f_out.write( "test result: %s" % http_post(_server.hostname,_server.port,"a=3\nb=8\nc=a+b\nprint c\n") )
  print 11
  #f_out.write( "Testing the connection...artemis2:sandor" )
  #f_out.write( "test result: %s" % http_post(_server.hostname,_server.port,"#! /usr/bin/env python\nimport sys\nfrom SpecClient import *\nm=SpecMotor.SpecMotor('phi','artemis2:sandor',500)\np=m.getPosition()\nprint 'position_of_the_specified_motor_is: ',p\np=m.getDialPosition()\nprint 'dial_position_of_the_specified_motor_is: ',p\ncmd=SpecCommand.SpecCommand('','artemis2:sandor',500)\nprint 'speed_of_the_specified_motor_is: ' , cmd.executeCommand('motor_par(phi,\"velocity\")')\nprint 'lowest_speed_of_the_specified_motor_is: ' , cmd.executeCommand('motor_par(phi,\"base_rate\")')\nprint 'stepsize_of_the_specified_motor_is: ' , cmd.executeCommand('motor_par(phi,\"step_size\")')\nprint 'low_limit_of_the_specified_motor_is: ',cmd.executeCommand('dial(phi,get_lim(phi,-1))')\nprint 'high_limit_of_the_specified_motor_is: ',cmd.executeCommand('dial(phi,get_lim(phi,1))')\n") )
  #print "artemis"
  f_out.flush()
  #write out the flag file
  #print "it is here, filename:"+filename[:-3]+".port_ready"
  f=open(filename[:-3]+".port_ready", 'w')
  f.flush()
  os.fsync(f.fileno())
  f.close()
  #
  # extra tests
  #
  #for i in range(0, 10):
  #    print "Testing the connection...artemis2:sandor" , i
  #    print "test result: %s" % http_post(_server.hostname,_server.port,"#! /usr/bin/env python\nimport sys\nfrom SpecClient import *\nm=SpecMotor.SpecMotor('phi','artemis2:sandor',500)\np=m.getPosition()\nprint 'position_of_the_specified_motor_is: ',p\np=m.getDialPosition()\nprint 'dial_position_of_the_specified_motor_is: ',p\ncmd=SpecCommand.SpecCommand('','artemis2:sandor',500)\nprint 'speed_of_the_specified_motor_is: ' , cmd.executeCommand('motor_par(phi,\"velocity\")')\nprint 'lowest_speed_of_the_specified_motor_is: ' , cmd.executeCommand('motor_par(phi,\"base_rate\")')\nprint 'stepsize_of_the_specified_motor_is: ' , cmd.executeCommand('motor_par(phi,\"step_size\")')\nprint 'low_limit_of_the_specified_motor_is: ',cmd.executeCommand('dial(phi,get_lim(phi,-1))')\nprint 'high_limit_of_the_specified_motor_is: ',cmd.executeCommand('dial(phi,get_lim(phi,1))')\n")
  
  


