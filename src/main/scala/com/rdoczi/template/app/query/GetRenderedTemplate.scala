package com.rdoczi.template.app.query

import java.util.UUID

import cats._
import cats.syntax.all._

import com.rdoczi.template.domain._

case class GetRenderedTemplate(id: UUID, parameters: Map[String, String]) extends Query

class GetRenderedTemplateHandler[F[_]: Monad](TemplateRepository: TemplateRepository[F]) extends QueryHandler[F, GetRenderedTemplate, Option[Either[RenderError, String]]] :
  override def handle(query: GetRenderedTemplate): F[Option[Either[RenderError, String]]] =
    for
      templateO <- TemplateRepository.get(query.id)
      rendered = templateO.map(template => template.validate(query.parameters).map(template.render))
    yield rendered