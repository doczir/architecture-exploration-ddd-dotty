package com.rdoczi.template.adapters

import cats.*
import cats.syntax.functor.*
import cats.effect.*
import com.rdoczi.template.domain.{Template, TemplateRepository}
import java.util.UUID

class InMemoryTemplateRepository[F[_]: Functor](dataRef: Ref[F, Map[UUID, Template]]) extends TemplateRepository[F]:
  def save(template: Template): F[Unit]  = dataRef.update(data => data + (template.id -> template))
  def get(id: UUID): F[Option[Template]] = dataRef.get.map(_.get(id))
  def list: F[List[Template]]            = dataRef.get.map(_.values.toList)

object InMemoryTemplateRepository:
  def create[F[_]: Concurrent](initialData: Map[UUID, Template] = Map.empty): F[InMemoryTemplateRepository[F]] =
    for dataRef <- Ref.of(initialData)
    yield InMemoryTemplateRepository(dataRef)
