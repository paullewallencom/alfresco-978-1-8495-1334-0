<#--
    Sign-Off Rejection Email Template when rework is required.

    Email is sent to approver 2 or approver 3 when rework is requested by sign-off person.
    See Sign-off process definition.

    Author: martin.bergljung@opsera.com
    Since: 1.0
    -->
<#assign signOffPhase=args["signOffPhase"]/>
<#assign campaignId=args["campaignId"]/>
<#assign rejectorPersonNodeRef=args["rejectorPersonNodeRef"]/>
<#assign rejectorPerson=companyhome.nodeByReference[rejectorPersonNodeRef]/>

The ${signOffPhase} for the ${campaignId} campaign has been
rejected by ${rejectorPerson.properties.firstName} ${rejectorPerson.properties.lastName}.

Best Money
