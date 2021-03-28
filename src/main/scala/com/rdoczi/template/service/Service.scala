package com.rdoczi.template.service

import cats.effect._
import cats.syntax.all._

import com.rdoczi.template.app._
import com.rdoczi.template.app.command._
import com.rdoczi.template.app.query._
import com.rdoczi.template.adapters._

object Service {
  def newApplication[F[_]: Concurrent]: F[App[F]] =
    for
      templateRepository <- InMemoryTemplateRepository.create[F]()

      addTemplate = AddTemplateHandler[F](templateRepository)
      commands = Commands(addTemplate)

      getTemplate = GetTemplateHandler[F](templateRepository)
      getTemplates = GetTemplatesHandler[F](templateRepository)
      getRenderedTemplate = GetRenderedTemplateHandler[F](templateRepository)
      queries = Queries(getTemplate, getTemplates, getRenderedTemplate)
    yield App(commands, queries)
}
