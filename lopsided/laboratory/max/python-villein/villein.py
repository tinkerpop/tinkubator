#!/usr/bin/python

"""

villein

A Python villein for Linked Process (linkedprocess.org)

Requires:

* Twisted (http://twistedmatrix.com/trac/) - Python Networking Framework
* Wokkel (http://wokkel.ik.nu/)            - Support library for Twisted

"""

# Setup/Import

from lop_protocols import linked_process

from twisted.application import service

from wokkel.client import XMPPClient

class villein(linked_process):
     """ Linked Process client. Can handle one farm at a time. """

     # Farm Requests

     def spawn_vm(self, vm_species = "jython"):
          """ Use the Linked Process <spawn_vm/> to generate a VM on the farm. Return the VM jid. """

          packet = self.spawn_vm_packet(vm_species)
          packet_id = packet.attributes["id"]

          self.__send__(packet)
          response_packet = self.PACKET.get_packet(packet_id)

          output = (response_packet.children["spawn_vm"].attributes["vm_jid"], response_packet.children["spawn_vm"].attributes["vm_password"])

          self.MACHINES.add_vm(output[0], output[1]) # (vm_jid, vm_password)

     def discover_services(self):
          """ Find services the farm is running """

          # TODO

     # VM Requests

     def submit_job(self, vm_jid, code):
          """ Use the Linked Process <submit_job/> to execute code on the VM. Returns the job ID. """

          packet = self.submit_job_packet(vm_jid, code)
          packet_id = packet.attributes["id"]

          self.PACKET.reserve(packet_id) # Reserve, but don't wait for result; result is retrieved through job_status.
          self.__send__(packet)

          self.MACHINES.add_job(vm_jid, packet_id)

     def get_value(self, vm_jid, *variable):
          """ Use the Linked Process <manage_bindings/> to retrieve a variable's value. You can pass ass many variables as necessary. """

          self.__send__(self.get_value_packet(vm_jid, variable))
          response_packet = self.PACKET.get_packet(packet)
          # TODO

     def set_value(self, vm_jid, variable):
          """ Use the Linked Process <manage_bindings/> to set a variable's value. 'variable' must be a dictionary. You can set multiple variables at once. """

          self.__send__(self.set_value_packet(vm_jid, variable))

     def job_status(self, vm_jid, job_id):
          """ Use the Linked Process <job_status/> to retrieve a job's status. If it has completed, returns any value that the job returned."""

          output = None

          if job_id in self.PACKETQUEUE: # If the farm has issued a response, meaning, the job has been complete
               output = ("COMPLETE", self.PACKETQUEUE[job_id]) # Return output

          else: # Otherwise, query for status.

               packet = self.job_status_packet(vm_jid, job_id)

               self.__send__(packet)

               output = (self.PACKET.get_packet(packet_id), None) # Return status.

          return output

     def abort_job(self, vm_jid, job_id):
          """ Terminate a job. """

          self.__send__(self.abort_job_packet(vm_jid, job_id))

     def terminate_vm(self, vm_jid):
          """ Terminate a VM. """

          self.__send__(packet)

# Misc.

if __name__ == "__main__":
     FARM_JID = raw_input("Farm JID: ")
     MY_JID = raw_input("My JID: ")
     PASSWORD = raw_input("My password: ")

     app = service.Application("TestVillein")

     test_villein = villein(MY_JID, PASSWORD, FARM_JID)
     xmpp_client = XMPPClient(test_villein.JID, PASSWORD)
     xmpp_client.logTraffic = False
     test_villein.setHandlerParent(xmpp_client)
     xmpp_client.setServiceParent(app)
