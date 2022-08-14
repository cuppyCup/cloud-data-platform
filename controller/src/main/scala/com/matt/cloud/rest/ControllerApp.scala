package com.matt.cloud.rest

import com.google.inject.{Guice, Injector}

object ControllerApp {

  def main(args:  _root_.scala.Array[_root_.scala.Predef.String]): Unit = {
    val injector: Injector = Guice.createInjector(new ControllerModule)

    val server: ControllerServer = injector.getInstance(classOf[ControllerServer])

    server.start()
  }

}
