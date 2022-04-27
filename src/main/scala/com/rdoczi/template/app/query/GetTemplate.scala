package com.rdoczi.template.app.query

import java.util.UUID

import com.rdoczi.template.domain._

case class GetTemplate(id: UUID) extends Query

class GetTemplateHandler[F[_]](templateRepository: TemplateRepository[F]) extends QueryHandler[F, GetTemplate, Option[Template]]:
  override def handle(query: GetTemplate): F[Option[Template]] = 
    templateRepository.get(query.id)