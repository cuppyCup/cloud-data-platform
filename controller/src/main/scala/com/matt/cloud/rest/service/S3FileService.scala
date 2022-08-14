package com.matt.cloud.rest.service

import akka.stream.Materializer
import akka.stream.alpakka.s3.MultipartUploadResult
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import com.google.inject.Inject
import com.matt.cloud.rest.entities.{S3DownloadRequest, S3File, S3UploadRequest}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

final class S3FileService @Inject()
(
)
(
  implicit val materializer: Materializer
)
  extends FileService[S3UploadRequest, MultipartUploadResult, S3DownloadRequest, Option[S3File]] with LazyLogging {

  // https://doc.akka.io/docs/alpakka/current/s3.html#download-a-file-from-s3  15 February 2021
  override
  def upload(uploadRequest: S3UploadRequest): Future[MultipartUploadResult] = {
    val S3UploadRequest(bucket: String, path: String, contents: Source[ByteString, Any]) = uploadRequest

    val s3Sink: Sink[ByteString, Future[MultipartUploadResult]] =
      S3.multipartUpload(bucket, path)

    contents.runWith(s3Sink)
  }

  // https://medium.com/thg-tech-blog/fully-functional-akka-7914f05a0977  15 February 2021
  override
  def download(downloadRequest: S3DownloadRequest)(implicit materializer: Materializer): Future[Option[S3File]] = {
    val S3DownloadRequest(bucket: String, key: String) = downloadRequest
    S3.download(bucket, key).map(_.flatMap {
      case (source, meta) ⇒
        logger.debug(s"File [$bucket / $key] found, size = [${meta.contentLength} bytes]")
        Option(S3File(bucket, key, meta.contentLength, meta, source))
    }).runWith(Sink.head)
  }

}
