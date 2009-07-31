#!/usr/bin/python

"""

Managers/handlers for Linked Process.

"""

# Setup/Import

import thread

# Class Definitions

class packet_manager:
     """ Manages XMPP packets, packet queue & packet-related methods. """

     def __init__(self):
          """ Initiate instance. """

          self.RESERVATIONS = [] # List of packet IDs that have been 'reserved', to pass to a function; non-reserved packets are tossed to __handle__().
          self.QUEUE = {} # {<id>:<packet>}

     # API/Misc.

     def add_packet(self, packet):
          """ Add a packet to the queue. """

          packet_id = msg.__getitem__("id")
          self.QUEUE[packet_id] = msg

          if not packet_id in self.RESERVATIONS:
               thread.start_new_thread(__handle__, tuple([packet_id])) # Handle a message w/o a handler. tuple(["xyz"]), because tuple("xyz") == ("x, "y", "z")

     def get_packet(self, packet_id):
          """ Reserves a packet, and returns it. """

          self.reserve(packet_id)

          while not self.received(packet_id):
               pass

          packet = self.QUEUE[packet_id]
          del self.QUEUE[packet]

          self.delete_reservation(packet_id)

          return packet

     def reserve(self, packet_id):
          """ Reserve a packet, but don't return. """

          self.RESERVATIONS.append(packet_id)

     def received(self, packet_id):
          """ Check whether packet has been received. """

          return packet_id in self.QUEUE

     def delete_reservation(self, packet_id):
          """ Stop reserving a packet. """

          try:

               self.RESERVATIONS.remove(packet_id)

          except:
               pass

     def __handle__(self, packet):
          """ Handle packets that weren't reserved.. """

          print packet.toXml() # TODO: Handle errors, etc.

class vm_manager:
     """ Manages Virtual Machines. Stores passwords, species, & jobs. """

     def __init__(self):
          """ Initiate instance. """

          self.MACHINES = {} # {<machine_jid>:[[<password>, <species>], <job_id1>, <job_id2>, ...]}

     # API/Misc.

     def add_vm(self, vm_jid, password):
          """ Add a VM to self.MACHINES. """

          self.MACHINES[vm_jid] = [password]

     def add_job(self, vm_jid, job_id):
          """ Add a job to the list. """

          self.MACHINES[vm_jid][1].append(job_id)

     def delete_vm(self, vm_jid):
          """ Delete a VM from self.MACHINES. """

          try:
               del self.MACHINES[vm_jid]

          except KeyError: # Machine doesn't exist
               pass

     def get_vm(self, vm_jid):
          """ Return a VM. """

          return self.MACHINES[vm_jid]

     def get_all_vms(self):
          """ Return all VMs. """

          return self.MACHINES

