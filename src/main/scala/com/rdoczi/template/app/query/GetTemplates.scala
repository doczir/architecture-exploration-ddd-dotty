package com.rdoczi.template.app.query

import com.rdoczi.template.domain._

case class GetTemplates() extends Query

case class GetTemplatesHandler[F[_]](TemplateRepository: TemplateRepository[F]) extends QueryHandler[F, GetTemplates, List[Template]]:
  override def handle(query: GetTemplates): F[List[Template]] =
    TemplateRepository.list
