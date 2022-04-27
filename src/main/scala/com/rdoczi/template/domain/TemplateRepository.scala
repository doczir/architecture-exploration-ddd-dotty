package com.rdoczi.template.domain

import java.util.UUID

trait TemplateRepository[F[_]]:
  def save(Template: Template): F[Unit]
  def get(id: UUID): F[Option[Template]]
  def list: F[List[Template]]
