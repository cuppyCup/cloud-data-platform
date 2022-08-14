package com.matt.cloud.rest.service

import akka.stream.Materializer
import com.matt.cloud.rest.entities.{DownloadRequest, UploadRequest}

import scala.concurrent.Future

trait FileService[S <: UploadRequest, T <: Any, U <: DownloadRequest, V <: Any] {

  def upload(uploadRequest : S): Future[T]

  def download(downloadRequest : U)(implicit materializer: Materializer): Future[V]

}