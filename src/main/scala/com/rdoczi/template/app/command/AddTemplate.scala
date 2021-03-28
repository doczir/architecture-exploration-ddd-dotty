package com.rdoczi.template.app.command

import java.util.UUID

import com.rdoczi.template.domain._

case class AddTemplate(id: UUID, name: String, template: String, schema: Map[String, AppSchemaRule]) extends Command
case class AppSchemaRule(tpe: String, values: Option[List[String]])

class AddTemplateHandler[F[_]](TemplateRepository: TemplateRepository[F]) extends CommandHandler[F, AddTemplate]:
  override def handle(command: AddTemplate): F[Unit] = 
    val domainTemplate = addTemplateToDomainModel(command)
    TemplateRepository.save(domainTemplate)

  private def addTemplateToDomainModel(addTemplate: AddTemplate): Template = 
    val rules = addTemplate.schema.toList.map { case ((name, rule)) => 
        val tpe = rule.tpe match {
          case "text" => SchemaType.Text
          case "number" => SchemaType.Number
          case "choice" => SchemaType.Choice(rule.values.get)
        }
        SchemaRule(name, tpe)
      }

    Template(addTemplate.id, addTemplate.name, addTemplate.template, Schema(rules))
