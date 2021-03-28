package com.rdoczi.template.domain

import java.util.UUID

case class Template(id: UUID, name: String, template: String, schema: Schema)

case class Schema(rules: List[SchemaRule])

case class SchemaRule(name: String, tpe: SchemaType)

enum SchemaType:
  case Text
  case Number
  case Choice(possibleValues: List[String])

enum RenderError:
  case InvalidChoice(actual: String, expected: List[String])
  case InvalidNumber(value: String)
  case MissingReplacement(value: String)
  case UnexpectedReplacement(value: String)

extension (template: Template)
  def validate(parameters: Map[String, String]): Either[RenderError, Replacements] = 
    parameters.foldLeft(Right(Replacements.empty): Either[RenderError, Replacements]) { 
      case (Right(replacements), (name, value)) =>
        template.schema.rules.find(rule => rule.name == name)
          .toRight(RenderError.UnexpectedReplacement(name))
          .flatMap { rule => 
            rule.tpe match {
              case SchemaType.Text =>
                Right(replacements.add(name, Replacement.TextReplacement(value)))
              case SchemaType.Number if value.toFloatOption.isDefined => 
                Right(replacements.add(name, Replacement.NumericReplacement(value)))
              case SchemaType.Number => 
                Left(RenderError.InvalidNumber(value))
              case SchemaType.Choice(values) if values.contains(value) => 
                Right(replacements.add(name, Replacement.TextReplacement(value)))
              case SchemaType.Choice(values) =>
                Left(RenderError.InvalidChoice(value, values))
                
            }
          }

      case (error, _) => error
    }

  def render(replacements: Replacements): String = 
    replacements.foldL[String](template.template) { case (tempalte, name, replacement) =>
       tempalte.replace(s"[[$name]]", replacement.toRendered)
    }

private[domain] opaque type Replacements = Map[String, Replacement]
object Replacements:
  def empty: Replacements = Map.empty[String, Replacement]

extension (replacements: Replacements)
  def add(name: String, replacement: Replacement): Replacements =
    replacements + (name -> replacement)
  def foldL[A](a: A)(fn: (A, String, Replacement) => A): A =
    replacements.foldLeft[A](a) { 
      case (acc, (name, replacement)) => 
        fn(acc, name, replacement) 
    }

private[domain] enum Replacement:
  case TextReplacement(value: String)
  case NumericReplacement(value: String)

extension (replacement: Replacement)
  def toRendered: String = 
    replacement match {
      case Replacement.TextReplacement(value) => s""""$value""""
      case Replacement.NumericReplacement(value) => value
    }