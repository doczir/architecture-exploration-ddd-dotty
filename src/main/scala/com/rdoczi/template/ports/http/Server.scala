package com.rdoczi.template.ports.http

import cats._
import cats.effect._
import org.http4s.implicits._
import com.rdoczi.template.app.App
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.middleware.Logger

class Server[F[_] : Async](TemplateRoutes: TemplateRoutes[F]):
  def serve() =
    val httpApp = Logger.httpApp(true, true)(TemplateRoutes.routes.orNotFound)
    BlazeServerBuilder[F]
      .bindHttp(9999, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain

object Server:
  def fromApp[F[_] : Async](app: App[F]): Server[F] = 
    Server(
      TemplateRoutes(app)
    )
