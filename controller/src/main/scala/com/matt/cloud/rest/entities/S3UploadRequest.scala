package com.matt.cloud.rest.entities

import akka.stream.scaladsl.Source
import akka.util.ByteString

case class S3UploadRequest(bucket: String, path: String, contents: Source[ByteString, Any]) extends UploadRequest {
  override def getType: String = "s3"
}
