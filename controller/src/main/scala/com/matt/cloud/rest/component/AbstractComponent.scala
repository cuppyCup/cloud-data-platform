package com.matt.cloud.rest.component

import akka.http.scaladsl.server.Route

trait AbstractComponent {

  def route: Route

}