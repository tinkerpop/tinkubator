require '../xmpp-rb/struct'

module Lop

  class VmStruct < Struct
    attr_accessor :vm_password, :vm_species, :jobs, :bindings, :farm

    def initialize
      @jobs = {}
      @bindings = {}
    end
  end

end