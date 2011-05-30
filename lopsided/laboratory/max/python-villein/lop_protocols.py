#!/usr/bin/python

"""

lop_protocols

A Python library for the Linked Process protocol/s (linkedprocess.org).

Requires:

* Twisted (http://twistedmatrix.com/trac/) - Python Networking Framework
* Wokkel (http://wokkel.ik.nu/)            - Support library for Twisted

"""

# Setup/Import

from lop_managers import packet_manager, vm_manager

import random

from twisted.words.protocols.jabber import jid
from twisted.words.xish import domish

from wokkel.xmppim import MessageProtocol, AvailablePresence

# Class Definitions

class linked_process(MessageProtocol):
     """ Base class for Linked Process protocol. Overwrite methods as necessary. """

     def __init__(self, str_jid, password, farm_jid, farm_password = None):
          """ Establish connection with XMPP server, subscript to farm. """

          self.RESOURCE = "villein-%x" % random.getrandbits(16) # Generate random 'resource' URI.
          self.STR_JID = str_jid + self.RESOURCE # Called str_jid, because jid is taken. (twisted.protocols.jabber.jid)
          self.JID = jid.internJID(self.STR_JID)

          self.FARM_PASSWORD = farm_password
          self.XMLNS = "http://linkedprocess.org/protocol"

          self.PACKET = packet_manager()
          self.MACHINES = vm_manager()

     # MessageProtocol

     def connectionMade(self):
          """ Executed on establishment of connection. """

          print "Connection established."
          self.send(AvailablePresence())

     def connectionLost(self, reason):
          """ Executed on termination of connection. """

          self.die(reason)

     def onMessage(self, msg):
          """ Executed when a message is received. """

          self.PACKET.add_packet(msg)

     # API/Misc.

     def die(self, reason = None):
          """ Kills process for 'reason'. """

          output = "Terminated"

          if reason:
               output += (": " + reason)

          else:
               output += "."

          print output
          raise SystemExit

     def get_vm(self, vm_jid):
          """ Retrieve a VM. """

          return self.MACHINES.get_vm(vm_jid)

     def get_password(self, vm_jid):

          return self.get_vm(vm_jid)[0][1] # Retrieve password from self.MACHINES

     def get_all_vms(self):
          """ Return all of the VMs running on the farm. """

          return self.MACHINES.get_all_vms()

     # Packet Creators

     def iq_packet(self, packet_id = None):
          """ Template packet. """

          if not packet_id: # If the message type is agnostic to packet ID
               packet_id = rand.getrandbits(16)

          packet = domish.Element((None, "iq")).attributes = {"xmlns" : self.XMLNS, "from" : self.STR_JID, "to" : farm_jid, "type" : get, "id" : packet_id}

          return packet

     def spawn_vm_packet(self, vm_species):
          """ Create a <spawn_vm/> packet. """

          packet = self.iq_packet

          if self.FARM_PASSWORD: # If a password is set
               packet.attributes["farm_password"] = self.FARM_PASSWORD

          spawn_tag = packet.addElement("spawn_vm").attributes = {"xmlns" : self.XMLNS, "vm_species" : vm_species}

          return packet

     def submit_job_packet(self, vm_jid, code):
          """ Packet to submit new job to a VM. """

          code = self.__escape__(code)

          vm_password = self.get_password(vm_jid)

          packet = self.iq_packet()

          packet.attributes["to"] = vm_jid

          submit_tag = packet.addElement("submit_job").attributes = {"xmlns" : self.XMLNS, "vm_password" : vm_password}
          submit_tag.addContent(code)

     def get_value_packet(self, vm_jid, variable):
          """ Packet for a <manage_bindings/> get. """

          vm_password = self.get_password(vm_jid)

          packet = self.iq_packet()
          packet.attributes["to"] = vm_jid

          binding_tag = packet.addElement((None, "manage_bindings")).attributes = {"xmlns" : self.XMLNS, "vm_password" : vm_password}
          for var in variable:
               binding_tag.addElement("binding").attributes = {"name" : var}

          return packet

     def set_value_packet(self, vm_jid, variable):
          """ Set the value of variables. """

          vm_password = self.get_password(vm_jid)

          packet = self.iq_packet()
          packet.attributes["type"] = "set"
          packet.attributes["to"] = vm_jid

          binding_tag = packet.addElement((None, "manage_bindings")).attributes = {"xmlns" : self.XMLNS, "vm_password" : vm_password}
          for var in variable.keys():
               binding_tag.addElement("binding").attributes = {"name" : var, "value" : variable[var]}

          return packet

     def job_status_packet(self, vm_jid, job_id):
          """ Query a job's status. """

          vm_password = self.get_password(vm_jid)

          packet = self.iq_packet()
          packet.attributes["to"] = vm_jid

          packet.addElement("job_status").attributes = {"xmlns" : self.XMLNS, "vm_password" : vm_password, "job_id" : job_id}

          return packet

     def abort_job_packet(self, vm_jid, job_id):

          vm_password = self.get_password(vm_jid)

          packet = iq_packet()
          packet.attributes["to"] = vm_jid

          packet.addElement("abort_job").attributes = {"xmlns" : self.XMLNS, "vm_password" : vm_password, "job_id" : job_id}

          return packet

     def terminate_vm_packet(self, vm_jid):

          vm_password = self.get_password(vm_jid)

          packet_id = random.getrandbits(16)
          packet.attributes["to"] = vm_jid

          packet.addElement("terminate_vm").attributes = {"xmlns" : self.XMLNS, "vm_password" : vm_password}

          return packet

     # Internal

     def __send__(self, msg):
          """ Send a message. """

          self.send(msg)

     def __escape__(self, string):
          """ Escapes the string with XML/HTTP equivalents. """

          output = string

          for char in ['<>\0 #{}|/\\^`~?[];:@$=&\n']: # Characters that need to be escaped.

               output = re.replace(char, ('%' + ('%x' % ord(char)), output)) # Replace with escape ASCII encoding (ord())

          return output

     def __unescape__(self, string):
          """ Replace escaped characters. """

          output = string

          for char in ['60', '62', '0', '32', '35', '123', '125', '124', '47', '92', '94', '96', '126', '63', '91', '93', '59', '58', '64', '36', '61', '38', '10']:

               output = re.replace(char, chr(char), output)

               return output
