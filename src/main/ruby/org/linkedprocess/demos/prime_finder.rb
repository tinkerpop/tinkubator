require '../xmpp-rb/lop_villein'
require '../xmpp-rb/packet_maker'
include Lop

class PrimeFinder < LopVillein

  attr_accessor  :job_ids

  def initialize(jid, password)
    super(jid, password)
    self.send(PacketMaker::create_roster_query())
    self.job_ids = []
  end

  def spawn_vms(max_vms)
    number_vms = 0
    self.farms.each do |farm_jid, farm_struct|
      if number_vms < max_vms
        self.send(PacketMaker::create_spawn_vm(farm_struct, "jruby", nil))
        number_vms = number_vms + 1
      end
    end
    number_vms
  end

  def distribute_find_primes_function()
    vm_structs = self.get_vm_structs()
    vm_structs.each do |vm_struct|
      self.send(PacketMaker::create_submit_job(vm_struct, self.find_primes_string(), nil))
    end
  end

  def distribute_jobs(start_int, end_int)
    job_number = 1
    vm_structs = self.get_vm_structs()
    if(vm_structs.size == 0)
      print("\nNO AVAILABLE VIRTUAL MACHINES")
      self.shutdown()
      exit()
    end
    interval = ((end_int - start_int) / vm_structs.size).to_i
    current_start_int = start_int
    vm_structs.each do |vm_struct|
      current_end_int = current_start_int+interval
      if(current_end_int > end_int)
        current_end_int = end_int
      end
      expression = "find_primes(" + current_start_int.to_s + "," + current_end_int.to_s + ").inspect"
      job_id = "job-" + job_number.to_s
      self.send(PacketMaker::create_submit_job(vm_struct, expression, job_id))
      job_ids.push(job_id)
      print("\n\tJOB " + job_id + " DISTRIBUTED: determine primes for " + current_start_int.to_s + " to " + current_end_int.to_s)
      job_number = job_number+1
      current_start_int = current_end_int + 1
    end
  end

  def find_primes(start_int, end_int)
    primes = []
    for integer in (start_int..end_int)
      if ('1' * integer) !~ /^1?$|^(11+?)\1+$/
        primes.push(integer)
      end
    end
    primes
  end

  def find_primes_string()
    return "def find_primes(start_int, end_int)
    primes = []
    for integer in (start_int..end_int)
      if ('1' * integer) !~ /^1?$|^(11+?)\\1+$/
        primes.push(integer)
      end
    end
    primes
  end"
  end

  def organize_results()
    # TODO: MAKE THIS GET THE RESULTS BACK AND SORT THEM
  end
  ### MAIN METHOD ###

 # Jabber.debug = true

  start_int = 1;
  end_int = 100;

  prime_finder = PrimeFinder::new("linked.process.2@xmpp42.linkedprocess.org", "linked23")
  prime_finder.wait_for_farm_number(1)

  start_time = Time::new()
  number_vms = prime_finder.spawn_vms(10);
  print("\nSPAWNED VIRTUAL MACHINES: " + number_vms.to_s)
  prime_finder.wait_for_vm_number(number_vms)

  prime_finder.distribute_find_primes_function()
  prime_finder.distribute_jobs(start_int, end_int)

  print("\nRETRIEVING JOBS: " + prime_finder.job_ids.inspect.to_s)
  prime_finder.wait_for_job_results(prime_finder.job_ids)


  print "\n\nPRIMES RESULT: " + prime_finder.get_job_results(prime_finder.job_ids).inspect.to_s
  end_time = Time::new()
  print("\nTOTAL RUNNING TIME IN SECONDS: " + (end_time.to_f - start_time.to_f).to_s)

  prime_finder.terminate_virtual_machines()
  prime_finder.close()

  start_time = Time::new()
  print("\n\nCALCULATING PRIMES ON LOCAL MACHINE: " + start_int.to_s + " to " + end_int.to_s)
  print("\nPRIME RESULTS: " + prime_finder.find_primes(start_int, end_int).inspect.to_s)
  end_time = Time::new()
  print("\nTOTAL RUNNING TIME IN SECONDS: " + (end_time.to_f - start_time.to_f).to_s)



end