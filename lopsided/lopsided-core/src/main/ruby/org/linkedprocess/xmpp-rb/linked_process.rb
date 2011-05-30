module Lop

  class LinkedProcess
    LOP_FARM_NAMESPACE = "http://linkedprocess.org/protocol#LoPFarm"
    LOP_VM_NAMESPACE = "http://linkedprocess.org/protocol#LoPVM"
    DISCO_INFO_NAMESPACE = "http://jabber.org/protocol/disco#info"
    IQ_ROSTER_NAMESPACE = "jabber:iq:roster"

    SPAWN_VM_TAGNAME = "spawn_vm"
    TERMINATE_VM_TAGNAME = "terminate_vm"
    SUBMIT_JOB_TAGNAME = "submit_job"
    MANAGE_BINDINGS_TAGNAME = "manage_bindings"
    BINDING_TAGNAME = "binding"
    ####
    FEATURE_TAGNAME = "feature"
    QUERY_TAGNAME = "query"
    ITEM_TAGNAME = "item"

    VM_SPECIES_ATTRIBUTE = "vm_species"
    VM_PASSWORD_ATTRIBUTE = "vm_password"
    VM_JID_ATTRIBUTE = "vm_jid"
    ERROR_TYPE_ATTRIBUTE = "error_type"
    ####
    JID_ATTRIBUTE = "jid"
    VAR_ATTRIBUTE = "var"
  end

end