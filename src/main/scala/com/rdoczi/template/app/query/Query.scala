package com.rdoczi.template.app.query

trait Query

trait QueryHandler[F[_], A <: Query, B]:
  def handle(query: A): F[B]