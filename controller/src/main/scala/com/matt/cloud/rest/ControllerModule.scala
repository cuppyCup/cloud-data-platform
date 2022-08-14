package com.matt.cloud.rest

import akka.actor.{ActorSystem, ClassicActorSystemProvider}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteConcatenation._
import akka.stream.{Materializer, SystemMaterializer}
import com.google.inject.name.{Named, Names}
import com.google.inject.{AbstractModule, Provides}
import com.matt.cloud.rest.component.{AbstractComponent, FileComponent}
import com.typesafe.config.ConfigFactory
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}

import scala.concurrent.ExecutionContext

class ControllerModule extends AbstractModule with ScalaModule {

  override
  def configure(): Unit = {
    // Read  `resources/application.conf` and make that available to this `com.us.scheduler` app.
    ConfigFactory.load().entrySet().forEach( entry => {
      entry.getValue.unwrapped() match {
        case value: java.lang.String =>
          bindConstant().annotatedWith(Names.named(entry.getKey)).to(value)
        case value: java.lang.Integer =>
          bindConstant().annotatedWith(Names.named(entry.getKey)).to(value)
        case value: java.lang.Boolean =>
          bindConstant().annotatedWith(Names.named(entry.getKey)).to(value)
        case value: java.lang.Double =>
          bindConstant().annotatedWith(Names.named(entry.getKey)).to(value)
        case value: java.lang.Long =>
          bindConstant().annotatedWith(Names.named(entry.getKey)).to(value)
        case _ =>
      }
    })

    // https://github.com/codingwell/scala-guice 11 November 2020
    val componentMultiBinder: ScalaMultibinder[AbstractComponent] = ScalaMultibinder.newSetBinder[AbstractComponent](binder)
    componentMultiBinder.addBinding.to[FileComponent]

    bind[ActorSystem].toInstance(ActorSystem("MyActorSystem"))
    bind[ExecutionContext].toInstance(ExecutionContext.Implicits.global)
  }

  @Provides
  @Named("routes")
  def routesConcatenatingProvider(routes: Set[AbstractComponent]): Route = {
    // Multi-binding from scala-guide, https://github.com/codingwell/scala-guice 11 November 2020.
    routes.map(_.route).reduce[Route]( (a, b) => a ~ b )
  }

  @Provides
  @Named("my-materializer")
  def myMaterializer(actorSystem: ActorSystem)(implicit provider: ClassicActorSystemProvider): Materializer = {
    SystemMaterializer(actorSystem).materializer
  }

}
