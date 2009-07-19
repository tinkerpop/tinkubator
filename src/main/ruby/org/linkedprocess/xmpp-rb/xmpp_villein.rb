require 'xmpp4r/client'
require 'vm_struct'
require 'farm_struct'
include Jabber

module Lop

  class XmppVillein < Client

    LOP_FARM_NAMESPACE = "http://linkedprocess.org/protocol#LoPFarm"
    LOP_VM_NAMESPACE = "http://linkedprocess.org/protocol#LoPVM"
    SPAWN_VM_TAGNAME = "spawn_vm"
    TERMINATE_VM_TAGNAME = "terminate_vm"
    SUBMIT_JOB_TAGNAME = "submit_job"
    VM_SPECIES_ATTRIBUTE = "vm_species"
    VM_PASSWORD_ATTRIBUTE = "vm_password"
    VM_JID_ATTRIBUTE = "vm_jid"

    attr_accessor :virtual_machines

    def initialize(jid, password)
      super(JID::new(jid))
      villein_jid = JID::new(jid)
      self.connect()
      self.auth(password)
      self.virtual_machines = {}
      send(Presence.new(nil, "LoP rVillein v0.1", 0))
      self.setup_callback()
    end



    def setup_callback()
      self.add_iq_callback do |iq|
        if iq.type != :error
          if iq.first_element(SPAWN_VM_TAGNAME)
            spawn_vm = iq.first_element(SPAWN_VM_TAGNAME)
            vm_struct = Lop::VmStruct.new()
            vm_struct.vm_password = spawn_vm.attributes[VM_PASSWORD_ATTRIBUTE]
            vm_struct.vm_species = spawn_vm.attributes[VM_SPECIES_ATTRIBUTE]
            vm_struct.full_jid = JID::new(spawn_vm.attributes[VM_JID_ATTRIBUTE])
            self.virtual_machines[vm_struct.full_jid] = vm_struct
            print(self.virtual_machines.inspect)
          elsif iq.first_element(TERMINATE_VM_TAGNAME)
            self.virtual_machines.delete(iq.from())
          elsif iq.first_element(SUBMIT_JOB_TAGNAME)

          end
        end
      end
    end

    def create_spawn_vm(farm_struct, vm_species)
      iq = Iq.new(:get, farm_struct.full_jid)
      iq.delete_namespace()
      spawn_vm = IqQuery.new(SPAWN_VM_TAGNAME)
      spawn_vm.add_namespace(LOP_FARM_NAMESPACE)
      spawn_vm.add_attribute(REXML::Attribute.new(VM_SPECIES_ATTRIBUTE, vm_species));
      iq.add(spawn_vm)
      iq
    end

    def create_terminate_vm(vm_struct)
      iq = Iq.new(:get, vm_struct.full_jid)
      iq.delete_namespace()
      terminate_vm = IqQuery.new(TERMINATE_VM_TAGNAME)
      terminate_vm.add_namespace(LOP_VM_NAMESPACE)
      terminate_vm.add_attribute(REXML::Attribute.new(VM_PASSWORD_ATTRIBUTE, vm_struct.vm_password));
      iq.add(terminate_vm)
      iq
    end

    def create_submit_job(vm_struct, expression)
      iq = Iq.new(:get, vm_struct.full_jid)
      iq.delete_namespace()
      submit_job = IqQuery.new(SUBMIT_JOB_TAGNAME)
      submit_job.add_namespace(LOP_VM_NAMESPACE)
      submit_job.add_attribute(REXML::Attribute.new(VM_PASSWORD_ATTRIBUTE, vm_struct.vm_password));
      submit_job.add_text(expression)
      iq.add(submit_job)
      iq
    end


    #Jabber::debug = true

    farm_struct = Lop::FarmStruct.new()
    farm_struct.full_jid = JID::new("linked.process.1@xmpp42.linkedprocess.org/LoPFarm/6TVDEIET");
    xmpp = XmppVillein.new("linked.process.2@xmpp42.linkedprocess.org", "linked23")
    spawn_vm_packet = xmpp.create_spawn_vm(farm_struct, "jruby");

    xmpp.send(spawn_vm_packet)
    while xmpp.processing > 0 do
      print(xmpp.processing)
      print("\n")
      sleep 7
      print("\n")
      print(xmpp.virtual_machines.inspect)
      xmpp.virtual_machines.each do |key, value|
        xmpp.send(xmpp.create_terminate_vm(xmpp.virtual_machines[key]))
      end
    end
    xmpp.close()
    quit
    #print(xmpp.create_submit_job("test@test", "password", "for(int i=0)"))

  end
end

#                                                                  RUBY NOTES
#<iq id="and7y-9" to="linked.process.1@xmpp42.linkedprocess.org/LoPFarm/4L7T36YF" from="linked.process.2@xmpp42.linkedprocess.org/LoPVillein/LKNXSRB3" type="get">
#  <spawn_vm xmlns="http://linkedprocess.org/protocol#LoPFarm" vm_species="js" />
#</iq>
      #query.add(REXML::Element.new('username').add_text(jid.node))