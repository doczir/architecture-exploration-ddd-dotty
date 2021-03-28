package com.rdoczi.template.app.command

trait Command

trait CommandHandler[F[_], A <: Command]:
  def handle(command: A): F[Unit]

