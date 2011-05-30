require '../xmpp-rb/struct'

module Lop

  class FarmStruct < Struct
    attr_accessor :virtual_machines

    def initialize()
      @virtual_machines = {}
    end
  end

end