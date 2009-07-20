require 'xmpp4r/client'
require '../xmpp-rb/linked_process'
require '../xmpp-rb/vm_struct'
require '../xmpp-rb/host_struct'
require '../xmpp-rb/farm_struct'
require '../xmpp-rb/struct'
include Jabber

module Lop

class PacketMaker

  private # DECLARATION OF PRIVATE METHODS
    def PacketMaker::create_lop_packet(to_jid, iq_type, packet_id)
      iq = Iq.new(iq_type, to_jid)
      iq.delete_namespace();
      if packet_id
        iq.set_id(packet_id)
      end
      iq
    end


  public # DECLARATION OF PUBLIC METHODS
  def PacketMaker::create_spawn_vm(farm_struct, vm_species, packet_id)
      iq = create_lop_packet(farm_struct.full_jid, :get, packet_id)
      spawn_vm = IqQuery.new(LinkedProcess::SPAWN_VM_TAGNAME)
      spawn_vm.add_namespace(LinkedProcess::LOP_FARM_NAMESPACE)
      spawn_vm.add_attribute(REXML::Attribute.new(LinkedProcess::VM_SPECIES_ATTRIBUTE, vm_species));
      iq.add(spawn_vm)
      iq
    end

    def PacketMaker::create_terminate_vm(vm_struct, packet_id)
      iq = create_lop_packet(vm_struct.full_jid, :get, packet_id)
      terminate_vm = IqQuery.new(LinkedProcess::TERMINATE_VM_TAGNAME)
      terminate_vm.add_namespace(LinkedProcess::LOP_VM_NAMESPACE)
      terminate_vm.add_attribute(REXML::Attribute.new(LinkedProcess::VM_PASSWORD_ATTRIBUTE, vm_struct.vm_password));
      iq.add(terminate_vm)
      iq
    end

    def PacketMaker::create_submit_job(vm_struct, expression, packet_id)
      iq = create_lop_packet(vm_struct.full_jid, :get, packet_id)
      submit_job = IqQuery.new(LinkedProcess::SUBMIT_JOB_TAGNAME)
      submit_job.add_namespace(LinkedProcess::LOP_VM_NAMESPACE)
      submit_job.add_attribute(REXML::Attribute.new(LinkedProcess::VM_PASSWORD_ATTRIBUTE, vm_struct.vm_password));
      submit_job.add_text(expression)
      iq.add(submit_job)
      iq
    end

    def PacketMaker::create_manage_bindings(vm_struct, bindings, iq_type, packet_id)
      iq = create_lop_packet(vm_struct.full_jid, iq_type, packet_id)
      manage_bindings = IqQuery::new(LinkedProcess::MANAGE_BINDINGS_TAGNAME)
      manage_bindings.add_namespace(LinkedProcess::LOP_VM_NAMESPACE)
      manage_bindings.add_attribute(REXML::Attribute.new(LinkedProcess::VM_PASSWORD_ATTRIBUTE, vm_struct.vm_password));
      bindings.each do |name, value|
        if (iq_type == :get)
          manage_bindings.add_element(LinkedProcess::BINDING_TAGNAME, {'name'=> name})
        else
          manage_bindings.add_element(LinkedProcess::BINDING_TAGNAME, {'name'=> name, 'value'=> value})
        end
      end
      iq.add(manage_bindings)
      iq
    end

    def PacketMaker::create_presence_subscribe(host_struct)
      subscribe = Presence::new()
      subscribe.delete_namespace()
      subscribe.set_to(host_struct.full_jid)
      subscribe.set_type(:subscribe)
      subscribe
    end

    def PacketMaker::create_presence_probe(host_struct)
      probe = Presence::new()
      probe.delete_namespace()
      probe.set_to(host_struct.full_jid)
      probe.set_type(:probe)
      probe
    end

    def PacketMaker::create_disco_info(struct)
      iq = Iq.new(:get, struct.full_jid)
      iq.delete_namespace()
      disco_info = IqQuery::new(LinkedProcess::QUERY_TAGNAME)
      disco_info.add_namespace(LinkedProcess::DISCO_INFO_NAMESPACE);
      iq.add(disco_info)
      iq
    end

    def PacketMaker::create_roster_query()
      iq = Iq.new(:get)
      iq.delete_namespace()
      roster = IqQuery::new(LinkedProcess::QUERY_TAGNAME)
      roster.add_namespace(LinkedProcess::IQ_ROSTER_NAMESPACE)
      iq.add(roster)
      iq
    end
end
end
