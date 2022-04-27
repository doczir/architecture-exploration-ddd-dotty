package com.rdoczi.template.ports.http

import cats._
import cats.syntax.all._
import cats.effect._

import io.circe._
import io.circe.syntax._

import org.http4s._
import org.http4s.headers._
import org.http4s.headers.`Content-Location`
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.server.blaze._
import org.http4s.implicits._

import java.util.UUID

import com.rdoczi.template.app.command._
import com.rdoczi.template.app.query._
import com.rdoczi.template.app._
import com.rdoczi.template.domain._

case class GetTemplateResponse(id: UUID, name: String, template: String, schema: Map[String, SchemaRule]) derives Encoder.AsObject
object GetTemplateResponse:
  given EntityEncoder.Pure[GetTemplateResponse] = jsonEncoderOf[GetTemplateResponse]

  def fromDomain(template: Template): GetTemplateResponse = 
    val schema = 
      template.schema.rules
        .map(rule => (rule.name, rule.tpe match {
          case SchemaType.Text => SchemaRule("text", None)
          case SchemaType.Number => SchemaRule("number", None)
          case SchemaType.Choice(values) => SchemaRule("choice", Some(values))
        }))
        .toMap
    GetTemplateResponse(template.id, template.name, template.template, schema)

case class ListTemplateResponse(templates: List[GetTemplateResponse]) derives Encoder.AsObject
object ListTemplateResponse:
  given EntityEncoder.Pure[ListTemplateResponse] = jsonEncoderOf[ListTemplateResponse]

  def fromDomain(templates: List[Template]): ListTemplateResponse = 
    ListTemplateResponse(templates.map(GetTemplateResponse.fromDomain))

case class GetRenderedTemplateResponse(rendered: String) derives Encoder.AsObject
object GetRenderedTemplateResponse:
  given EntityEncoder.Pure[GetRenderedTemplateResponse] = jsonEncoderOf[GetRenderedTemplateResponse]

case class GetRenderedTemplateRequest(params: Map[String, String]) derives Decoder:
  def toCommand(id: UUID): GetRenderedTemplate = GetRenderedTemplate(id, params)

object GetRenderedTemplateRequest:
  given [F[_]: Concurrent]: EntityDecoder[F, GetRenderedTemplateRequest] = jsonOf[F, GetRenderedTemplateRequest]

case class CreateTemplateRequest(name: String, template: String, schema: Map[String, SchemaRule]) derives Decoder:
  def toCommand(id: UUID): AddTemplate = 
    val appSchema = schema.map{ case ((name, rule)) => (name, AppSchemaRule(rule.`type`, rule.values)) }
    AddTemplate(id, name, template, appSchema)

object CreateTemplateRequest:
  given [F[_]: Concurrent]: EntityDecoder[F, CreateTemplateRequest] = jsonOf[F, CreateTemplateRequest]
  

case class SchemaRule(`type`: String, values: Option[List[String]]) derives Codec.AsObject

class TemplateRoutes[F[_]: Concurrent](app: App[F]):
  val routes: HttpRoutes[F] =
    val dsl = new Http4sDsl[F]{}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "template" => 
        for
          templates <- app.queries.getTemplates.handle(GetTemplates())
          templateResponses = ListTemplateResponse.fromDomain(templates)          
          resp <- Ok(templateResponses)
        yield resp

      case GET -> Root / "template" / UUIDVar(id) => 
        for
          templateO <- app.queries.getTemplate.handle(GetTemplate(id))
          getTemplateResponseO = templateO.map(GetTemplateResponse.fromDomain)
          resp <- getTemplateResponseO.map(Ok(_)).getOrElse(NotFound())
        yield resp

      case req @ POST -> Root / "template" / UUIDVar(id) / "render" => 
        for
          getRenderedTemplateRequest <- req.as[GetRenderedTemplateRequest]
          getRenderedTemplate = getRenderedTemplateRequest.toCommand(id)

          renderedO <- app.queries.getRenderedTemplate.handle(getRenderedTemplate)

          resp <- renderedO match {
            case Some(Right(rendered)) => Ok(rendered)
            case Some(Left(error)) => BadRequest(error.toString)
            case None => NotFound()
          }
        yield resp

      case req @ POST -> Root / "template" / "create" =>
        for
          createTemplateRequest <- req.as[CreateTemplateRequest]
          id = UUID.randomUUID
          addTemplate = createTemplateRequest.toCommand(id)
          _ <- app.commands.addTemplate.handle(addTemplate)
          resp <- Created()
        yield resp
    }
