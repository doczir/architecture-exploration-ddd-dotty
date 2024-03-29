package com.rdoczi.template.app

import com.rdoczi.template.app.command.*
import com.rdoczi.template.app.query.*

case class Commands[F[_]](addTemplate: AddTemplateHandler[F])

case class Queries[F[_]](
    getTemplate: GetTemplateHandler[F],
    getTemplates: GetTemplatesHandler[F],
    getRenderedTemplate: GetRenderedTemplateHandler[F]
)

case class App[F[_]](commands: Commands[F], queries: Queries[F])
