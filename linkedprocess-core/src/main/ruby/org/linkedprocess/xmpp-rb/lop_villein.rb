require 'xmpp4r/client'
require '../xmpp-rb/packet_maker'
require '../xmpp-rb/linked_process'
require '../xmpp-rb/struct'
require '../xmpp-rb/host_struct'
require '../xmpp-rb/farm_struct'
require '../xmpp-rb/vm_struct'
require '../xmpp-rb/job'

include Jabber

module Lop

  class LopVillein < Client


    attr_accessor :farms, :full_jid

    def initialize(jid, password, port=5222, log_packets=false)
      super(JID::new(jid))
      @logger = log_packets
      @full_jid = JID::new(jid)
      self.connect(self.full_jid.domain(), port)
      self.auth(password)
      @farms = {}
      self.send(Presence::new(nil, "LoP Prime Finder v0.1", 0))
      self.setup_lop_callback()
      self.setup_presence_callback()
      self.setup_roster_callback()
      self.setup_disco_callback()
      if (@logger)
        self.setup_packet_logger_callback()
      end
    end

    protected # DECLARATION OF PROTECTED METHODS
    def setup_presence_callback()
      self.add_presence_callback do |presence|
        if presence.type() != :unsubscribe && presence.type() != :unsubscribed
          struct = Struct::new()
          struct.full_jid = presence.from()
          self.send(PacketMaker::create_disco_info(struct))
        end
      end
    end

    def setup_packet_logger_callback()
      self.add_stanza_callback do |stanza|
        print("\nLOGGER: " + stanza.to_s)
      end
    end

    def setup_disco_callback()
      self.add_iq_callback do |iq|
        if iq.type == :result
          if iq.first_element(LinkedProcess::QUERY_TAGNAME) && iq.first_element(LinkedProcess::QUERY_TAGNAME).namespace == LinkedProcess::DISCO_INFO_NAMESPACE
            iq.first_element(LinkedProcess::QUERY_TAGNAME).get_elements(LinkedProcess::FEATURE_TAGNAME).each do |element|
              if element.attributes[LinkedProcess::VAR_ATTRIBUTE] == LinkedProcess::LOP_FARM_NAMESPACE
                farm_struct = FarmStruct::new()
                farm_struct.full_jid = iq.from()
                if self.farms[farm_struct.full_jid] == nil
                  self.farms[farm_struct.full_jid] = farm_struct
                end
              end
            end
          end
        end
      end
    end

    def setup_roster_callback()
      self.add_iq_callback do |iq|
        if iq.type == :result
          if iq.first_element(LinkedProcess::QUERY_TAGNAME) && iq.first_element(LinkedProcess::QUERY_TAGNAME).namespace == LinkedProcess::IQ_ROSTER_NAMESPACE
            iq.first_element(LinkedProcess::QUERY_TAGNAME).get_elements(LinkedProcess::ITEM_TAGNAME).each do |item|
              host_struct = HostStruct::new()
              host_struct.full_jid = item.attributes[LinkedProcess::JID_ATTRIBUTE]
              self.send(PacketMaker::create_presence_probe(host_struct))
            end
          end
        end
      end
    end

    def setup_lop_callback()
      self.add_iq_callback do |iq|
        # HANDLE INCOMING SPAWN_VM PACKET
        if iq.first_element(LinkedProcess::SPAWN_VM_TAGNAME) && iq.type == :result
          spawn_vm = iq.first_element(LinkedProcess::SPAWN_VM_TAGNAME)
          farm_struct = @farms[iq.from()]
          if (farm_struct != nil)
            vm_struct = Lop::VmStruct.new()
            vm_struct.vm_password = spawn_vm.attributes[LinkedProcess::VM_PASSWORD_ATTRIBUTE]
            vm_struct.vm_species = spawn_vm.attributes[LinkedProcess::VM_SPECIES_ATTRIBUTE]
            vm_struct.full_jid = JID::new(spawn_vm.attributes[LinkedProcess::VM_JID_ATTRIBUTE])
            farm_struct.virtual_machines[vm_struct.full_jid] = vm_struct
          end
        # HANDLE INCOMING TERMINATE_VM PACKET
        elsif iq.first_element(LinkedProcess::TERMINATE_VM_TAGNAME)
          @farms.each do |farm_jid, farm_struct|
            farm_struct.virtual_machines.delete(iq.from())
          end
        # HANDLE INCOMING SUBMIT_JOB PACKET
        elsif iq.first_element(LinkedProcess::SUBMIT_JOB_TAGNAME)
          self.handle_incoming_submit_job(iq)
        # HANDLE INCOMING MANAGE BINDINGS PACKET
        elsif iq.first_element(LinkedProcess::MANAGE_BINDINGS_TAGNAME) && iq.type == :result
          manage_bindings = iq.first_element(LinkedProcess::MANAGE_BINDINGS_TAGNAME)
          vm_struct = self.get_vm_struct(iq.from())
          if (vm_struct)
            manage_bindings.get_elements(LinkedProcess::BINDING_TAGNAME).each do |binding|
              binding.each do |key, value|
                vm_struct.bindings[key] = value
              end
            end
          end
        end
      end
    end

    def handle_incoming_submit_job(iq)
      submit_job = iq.first_element(LinkedProcess::SUBMIT_JOB_TAGNAME)
      vm_struct = self.get_vm_struct(iq.from())
      if (vm_struct)
        job = vm_struct.jobs[iq.id]
        if(job == nil)     
          job = Job::new()
          job.job_id = iq.id
          vm_struct.jobs[iq.id] = job
        end
        if(iq.type == :error)
            job.error_type = submit_job.attributes[LinkedProcess::ERROR_TYPE_ATTRIBUTE]
            job.error_message = submit_job.text
          elsif(iq.type == :result)
            job.result = submit_job.text
        end
       end
    end

    public # DECLARATION OF PUBLIC METHODS
    def shutdown
      self.close()
    end

    def get_farm_struct(jid)
      return @farms[jid]
    end

    def get_farm_structs(jid)
      return @farms.values
    end

    def get_vm_struct(jid)
      @farms.each do |farm_jid, farm_struct|
        farm_struct.virtual_machines.each do | vm_jid, vm_struct |
          if vm_jid == jid
            return vm_struct
          end
        end
      end
    end

    def get_vm_structs()
      vm_structs = []
      @farms.each do |farm_jid, farm_struct|
        farm_struct.virtual_machines.each do | vm_jid, vm_struct |
          vm_structs.push(vm_struct)
        end
      end
      return vm_structs
    end

    def jobs_done(job_ids)
      vm_structs = self.get_vm_structs()
      for job_id in job_ids
        found = false
        vm_structs.each do |vm_struct|
          if vm_struct.jobs[job_id]
            found = true
          end
        end
        if(!found)
          return false
        end
      end
      return true
    end

    def get_job_results(job_ids)
      job_results = []
      vm_structs = self.get_vm_structs()
      for job_id in job_ids
        vm_structs.each do |vm_struct|
          if vm_struct.jobs[job_id]
            job_results.push(vm_struct.jobs[job_id])
          end  
        end
      end
      job_results
    end

    def wait_for_farm_number(min_farm_number, sleep_time=0.5)
      while(self.farms.size() < min_farm_number)
        sleep sleep_time
      end
    end

    def wait_for_vm_number(min_vm_number, sleep_time=0.5)
      while(self.get_vm_structs().size() < min_vm_number)
        sleep sleep_time
      end
    end

    def wait_for_job_results(job_ids, sleep_time=0.5)
      while(!self.jobs_done(job_ids))
        sleep sleep_time
      end
    end

    def terminate_virtual_machines()
      vm_structs = self.get_vm_structs()
      vm_structs.each do |vm_struct|
        self.send(PacketMaker::create_terminate_vm(vm_struct,nil))
      end
    end

    def setup_packet_logger(log_packets)
      @logger = log_packets
    end

  end
end
