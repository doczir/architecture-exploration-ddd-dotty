package com.rdoczi.template

import cats.effect.*
import com.rdoczi.template.app.{App, Commands}
import com.rdoczi.template.ports.http.Server
import com.rdoczi.template.service.Service

object Main extends IOApp.Simple:
  def run: IO[Unit] =
    for
      app <- Service.newApplication[IO]
      server = Server.fromApp(app)
      _ <- server.serve()
    yield ()
