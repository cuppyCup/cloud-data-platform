package com.matt.cloud.rest.entities

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class S3DownloadRequest(bucket: String, key: String) extends DownloadRequest {
  override def getType: String = "s3"
}

// Marshalling discussed https://doc.akka.io/docs/akka-http/current/common/json-support.html
// as seen on 6 November 2020.
// Also discussed https://doc.akka.io/docs/akka-http/current/routing-dsl/index.html#minimal-example
// as seen 11 November 2020.
trait S3DownloadRequestJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val S3DownloadRequestFormats: RootJsonFormat[S3DownloadRequest] = jsonFormat2(S3DownloadRequest)
}
object S3DownloadRequestJsonSupport extends S3DownloadRequestJsonSupport