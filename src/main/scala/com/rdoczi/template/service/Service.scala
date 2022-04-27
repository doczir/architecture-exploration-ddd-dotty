package com.rdoczi.template.service

import cats.effect.*
import cats.syntax.all.*

import com.rdoczi.template.app.*
import com.rdoczi.template.app.command.*
import com.rdoczi.template.app.query.*
import com.rdoczi.template.adapters.*

object Service:
  def newApplication[F[_]: Concurrent]: F[App[F]] =
    for
      templateRepository <- InMemoryTemplateRepository.create[F]()

      addTemplate = AddTemplateHandler[F](templateRepository)
      commands    = Commands(addTemplate)

      getTemplate         = GetTemplateHandler[F](templateRepository)
      getTemplates        = GetTemplatesHandler[F](templateRepository)
      getRenderedTemplate = GetRenderedTemplateHandler[F](templateRepository)
      queries             = Queries(getTemplate, getTemplates, getRenderedTemplate)
    yield App(commands, queries)
