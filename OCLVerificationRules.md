#Verification rules defined in the USE environment

# OCL Verification Rules #

-- $ProjectHeader: use 3-0-release.1 Mon, 15 Ago 2014 20:18:33 +0200 green $

model BPMNExtension


-- Extension Mechanism Classes

class Element
end

class BaseElement < Element
attributes
id: String
end

class ExtensionDefinition
attributes
name: String
end

class ExtensionAttributeDefinition
attributes
name: String
type: String
isReference: Boolean
end

class ExtensionAttributeValue
end

-- Extension Mechanism Associations

association ExtensionDefinitions between
BaseElement[0..**] role baseElement
ExtensionDefinition[0..**] role extensionDefinitions
end

association ExtensionAttributeValueDefinitions between
ExtensionAttributeValue[0..**] role extensionAttributeValue
ExtensionAttributeDefinition[1](1.md) role extensionAttributeDefinition
end**

association ExtensionAttributeValueElementRefs between
ExtensionAttributeValue[0..**] role extensionAttributeValue
Element[0..1] role valueRef
end**

composition extensionAttributeValueElement between
ExtensionAttributeValue[0..1] role extensionAttributeValue2
Element[0..1] role value
end

composition ExtensionAttributeDefinitions between
ExtensionDefinition[0..1] role extensionDefinition
ExtensionAttributeDefinition[0..**] role extensionAttributeDefinitions
end**

composition ExtensionValues between
BaseElement[0..1] role baseElement
ExtensionAttributeValue[0..**] role extensionValues
end**

-- BPMN Classes

class Activity < BaseElement
attributes
name: String
end

class Task < Activity
end

class UserTask < Task
attributes
implementation: String
end

class Resource < BaseElement
attributes
name: String
end

class ResourceParameter < BaseElement
attributes
name: String
isRequired: Boolean
end

class ResourceRole < BaseElement
attributes
name: String
end

class Performer < ResourceRole
end

class HumanPerformer < Performer
end

class PotentialOwner < HumanPerformer
end

class ResourceParameterBinding < BaseElement
end

class ResourceAssignmentExpression < BaseElement
end

class Expression < BaseElement
end

-- BPMN Associations

composition ResourceResourceParameters between
Resource[0..1] role resource
ResourceParameter[0..**] role resourceParameters
end**

composition resourceParameterBindingExpression between
ResourceParameterBinding[0..1] role resourceParameterBinding
Expression[1](1.md) role expression
end

association resourceParameterBindingResourceParameter between
ResourceParameterBinding[0..**] role resourceParameterBinding
ResourceParameter[1](1.md) role parameterRef
end**

composition resourceAssignmentExpressionExpression between
ResourceAssignmentExpression[0..1] role resourceAssignmentExpression
Expression[1](1.md) role expression
end

composition activityResourceRole between
Activity[0..1] role activity
ResourceRole[0..**] role resources
end**

association ResourceRoleResources between
ResourceRole[0..**] role resourceRole
Resource[0..1] role resourceRef
end**

-- Resource Perspective Extension Classes

class ExtensionElement < Element
end

class ResourceParameterValue < ExtensionElement
end

class ResourcePrivilege < ExtensionElement
end


-- Resource Perspective Extension Associations

association ResourceParameterValueImplementations between
ResourceParameterValue[0..**] role resourceParameterValue
ResourceParameterValueImpl[1](1.md) role implementation
end**

association ResourcePrivilegeImplementations between
ResourcePrivilege[0..**] role resourcePrivilege
ResourcePrivilegeImpl[1](1.md) role implementation
end**

-- RPIM Classes

class RPIMElement < Element
end

class ResourceImpl < RPIMElement
attributes
name: String
documentation: String
isReferenceable: Boolean
isBindable: Boolean
bindingType: String
end

class HumanResourceImpl < ResourceImpl
end

class ResourceClassifierImpl < ResourceImpl
end

class ClassificationImpl < RPIMElement
attributes
name: String
documentation: String
lower: Integer
upper: Integer
end

class RelationshipImpl < RPIMElement
end

class ResourceParameterImpl < RPIMElement
end

class ResourceParameterValueImpl < RPIMElement
end

class ParameterTypeImpl < RPIMElement
end

class ResourcePrivilegeImpl < RPIMElement
end

-- RPIM Associations

association ClassificationSources between
ClassificationImpl[0..**] role classificationImpl
HumanResourceImpl[1](1.md) role source
end**

association ClassificationTargets between
ClassificationImpl[0..**] role classificationImpl
ResourceClassifierImpl[1](1.md) role target
end**

association RelationshipSources between
RelationshipImpl[0..**] role classificationImplSource
ResourceClassifierImpl[1](1.md) role source
end**

association RelationshipTargets between
RelationshipImpl[0..**] role classificationImplTarget
ResourceClassifierImpl[1](1.md) role target
end**

association ResourceParameterImplTypes between
ResourceParameterImpl[0..**] role resourceParameterImplType
ParameterTypeImpl[1](1.md) role type
end**

association ResourceParameterImplResources between
ResourceParameterImpl[0..**] role resourceParameterImplResource
ResourceImpl[1](1.md) role resource
end**

association ResourcePrivilegeImplResource between
ResourcePrivilegeImpl[0..**] role resourcePrivilegeImplResource
ResourceImpl[1](1.md) role resource
end**



-- Resource Perspective Extension Constraints

constraints

context Resource
-- i1) Consistent implementation of Resource Classifications, Relationships, Valued Parameters and Resource Privileges
inv resourceParameters:
> self.resourceParameters->forAll(r:ResourceParameter | r.extensionDefinitions->forAll(
> > ed:ExtensionDefinition |
> > (ed.name = 'ResourceClassification' implies
> > r.extensionValues->select(ev | ev.extensionAttributeDefinition.name = 'target')->asSequence()->first().valueRef.oclAsType(Resource).
> > extensionValues->select(ev | ev.extensionAttributeDefinition.name = 'implementation')->asSequence()->first().valueRef =
> > > r.extensionValues->select(ev | ev.extensionAttributeDefinition.name = 'implementation')->asSequence()->first().valueRef.oclAsType(ClassificationImpl).target

> > and
> > self.extensionValues->select(ev | ev.extensionAttributeDefinition.name = 'implementation')->asSequence()->first().valueRef =
> > > r.extensionValues->select(ev | ev.extensionAttributeDefinition.name = 'implementation')->asSequence()->first().valueRef.oclAsType(ClassificationImpl).source

> > )
> > and
> > (
> > ed.name = 'ResourceRelationship' implies
> > r.extensionValues->select(ev | ev.extensionAttributeDefinition.name = 'target')->asSequence()->first().valueRef.oclAsType(Resource).
> > extensionValues->select(ev | ev.extensionAttributeDefinition.name = 'implementation')->asSequence()->first().valueRef =
> > > r.extensionValues->select(ev | ev.extensionAttributeDefinition.name = 'implementation')->asSequence()->first().valueRef.oclAsType(RelationshipImpl).target

> > and
> > self.extensionValues->select(ev | ev.extensionAttributeDefinition.name = 'implementation')->asSequence()->first().valueRef =
> > > r.extensionValues->select(ev | ev.extensionAttributeDefinition.name = 'implementation')->asSequence()->first().valueRef.oclAsType(RelationshipImpl).source

> > )
> > and
> > (
> > ed.name = 'ValuedParameter' implies
> > r.extensionValues->select(ev | ev.extensionAttributeDefinition.name = 'value')->asSequence()->first().value.oclAsType(ResourceParameterValue).implementation =
> > > r.extensionValues->select(ev | ev.extensionAttributeDefinition.name = 'implementation')->asSequence()->first().valueRef.oclAsType(ResourceParameterImpl).type

> > and
> > self.extensionValues->select(ev | ev.extensionAttributeDefinition.name = 'implementation')->asSequence()->first().valueRef =
> > > r.extensionValues->select(ev | ev.extensionAttributeDefinition.name = 'implementation')->asSequence()->first().valueRef.oclAsType(ResourceParameterImpl).resource

> > )

> ))
> and self.extensionDefinitions->forAll(
> > ed:ExtensionDefinition |
> > ed.name = 'HumanResource' or ed.name = 'ResourceClassifier' implies
> > > self.extensionValues->select(ev | ev.extensionAttributeDefinition.name = 'resourcePrivileges')->forAll(ev:ExtensionAttributeValue |
> > > ev.value.oclAsType(ResourcePrivilege).implementation.oclAsType(ResourcePrivilegeImpl).resource =
> > > self.extensionValues->select(ev | ev.extensionAttributeDefinition.name = 'implementation')->asSequence()->first().valueRef

> > )

> )



