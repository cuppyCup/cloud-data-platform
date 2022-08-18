package com.matt.cloud.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.google.inject.{Inject, Singleton}
import com.google.inject.name.Named

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ControllerServer @Inject()
(
  @Named("controller.hostname") hostname: String,
  @Named("controller.port") port: Int,
  @Named("routes") routes: Route
)
(
  implicit val actorSystem: ActorSystem,
  implicit val executionContext: ExecutionContext
){

  private var bindingFuture: Future[Http.ServerBinding] = _

  def start(): Unit = {
    sys.addShutdownHook(
      stop()
    )

    bindingFuture = Http().newServerAt(hostname, port).bind(routes)
  }

  def stop(): Unit = {
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => actorSystem.terminate()) // and shutdown when done
  }

}
