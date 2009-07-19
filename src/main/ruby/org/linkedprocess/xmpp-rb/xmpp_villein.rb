require 'xmpp4r/client'
include Jabber

module Lop

  class XmppVillein < Client

    LOP_FARM_NAMESPACE = "http://linkedprocess.org/protocol#LoPFarm"
    LOP_VM_NAMESPACE = "http://linkedprocess.org/protocol#LoPVm"
    SPAWN_VM_TAGNAME = "spawn_vm"
    TERMINATE_VM_TAGNAME = "terminate_vm"
    SUBMIT_JOB_TAGNAME = "submit_job"
    VM_SPECIES_ATTRIBUTE = "vm_species"
    VM_PASSWORD_ATTRIBUTE = "vm_password"

    def initialize(jid, password)
      super(JID::new(jid))
      villein_jid = JID::new(jid)
      self.connect()
      self.auth(password)
      send(Presence.new(nil, "LoP rVillein v0.1", 0))

      self.add_iq_callback do |iq|
      if iq.type != :error
        print(iq)
      end
    end

    end

    def create_spawn_vm(farm_jid, vm_species)
      iq = Iq.new(:get, farm_jid)
      iq.delete_namespace()
      spawn_vm = IqQuery.new(SPAWN_VM_TAGNAME)
      spawn_vm.add_namespace(LOP_FARM_NAMESPACE)
      spawn_vm.add_attribute(REXML::Attribute.new(VM_SPECIES_ATTRIBUTE, vm_species));
      iq.add(spawn_vm)
      iq
    end

    def create_terminate_vm(vm_jid, vm_password)
      iq = Iq.new(:get, vm_jid)
      iq.delete_namespace()
      terminate_vm = IqQuery.new(TERMINATE_VM_TAGNAME)
      terminate_vm.add_namespace(LOP_VM_NAMESPACE)
      terminate_vm.add_attribute(REXML::Attribute.new(VM_PASSWORD_ATTRIBUTE, vm_password));
      iq.add(terminate_vm)
      iq
    end

    def create_submit_job(vm_jid, vm_password, expression)
      iq = Iq.new(:get, vm_jid)
      iq.delete_namespace()
      submit_job = IqQuery.new(SUBMIT_JOB_TAGNAME)
      submit_job.add_namespace(LOP_VM_NAMESPACE)
      submit_job.add_attribute(REXML::Attribute.new(VM_PASSWORD_ATTRIBUTE, vm_password));
      submit_job.add_text(expression)
      iq.add(submit_job)
      iq
    end


    Jabber::debug = true
    farm_jid = JID::new("linked.process.1@xmpp42.linkedprocess.org/LoPFarm/4L7T36YF");
    xmpp = XmppVillein.new("linked.process.2@xmpp42.linkedprocess.org", "linked23")
    spawn_vm_packet = xmpp.create_spawn_vm(farm_jid, "jruby");

    xmpp.send(spawn_vm_packet)
    sleep 10000
    #print(xmpp.create_submit_job("test@test", "password", "for(int i=0)"))

  end
end

#                                                                  RUBY NOTES
#<iq id="and7y-9" to="linked.process.1@xmpp42.linkedprocess.org/LoPFarm/4L7T36YF" from="linked.process.2@xmpp42.linkedprocess.org/LoPVillein/LKNXSRB3" type="get">
#  <spawn_vm xmlns="http://linkedprocess.org/protocol#LoPFarm" vm_species="js" />
#</iq>
      #query.add(REXML::Element.new('username').add_text(jid.node))