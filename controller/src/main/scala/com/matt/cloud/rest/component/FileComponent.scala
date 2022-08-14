package com.matt.cloud.rest.component

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, extractRequestContext, fileUpload, formFields, get, onComplete, path, put}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.alpakka.s3.{MultipartUploadResult, ObjectMetadata}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.google.inject.Inject
import com.matt.cloud.rest.entities.S3DownloadRequestJsonSupport._
import com.matt.cloud.rest.entities.{S3DownloadRequest, S3File, S3UploadRequest}
import com.matt.cloud.rest.service.S3FileService
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class FileComponent @Inject()
(
  s3FileService: S3FileService
)
(
  implicit val system: ActorSystem,
  implicit val executionContext: ExecutionContext,
  implicit val materializer: Materializer
) extends AbstractComponent with LazyLogging {

  override def route: Route = {
    concat(
      put {
        path("/upload") {
          extractRequestContext { ctx =>
            fileUpload("content") {
              case (metadata, byteSource) =>
                formFields("bucket", "path") { (bucket, path) =>
                  val result: Future[MultipartUploadResult] = s3FileService.upload(S3UploadRequest(bucket, path, byteSource))

                  onComplete(result) {
                    case Success(multipartUploadResult) =>
                      complete(
                        HttpResponse(
                          StatusCodes.Created,
                          entity = HttpEntity(
                            contentType = ContentTypes.`application/json`,
                            data = ByteString(multipartUploadResult.toString)
                          )
                        ))
                    case Failure(e) =>
                      logger.error(s"Unable to upload file as requested for bucket $bucket with path $path", e)
                      complete(
                        HttpResponse(
                          StatusCodes.InternalServerError,
                          entity = HttpEntity(
                            contentType = ContentTypes.`text/plain(UTF-8)`,
                            data = ByteString(e.getMessage)
                          )
                        )
                      )
                  }
                }
            }

          }
        }
      },
      get {
        path("/download") {
          entity(as[S3DownloadRequest]) { downloadRequest =>

            val s3File: Future[Option[S3File]] = s3FileService.download(downloadRequest)

            onComplete(s3File) {
              case Success(optionS3File) => optionS3File match {
                case Some(s3File) =>
                  val S3File(_: String, _: String, bytes: Long, metadata: ObjectMetadata, data: Source[ByteString, NotUsed]) = s3File
                  complete(
                    HttpResponse(
                      entity = HttpEntity(
                        metadata.contentType
                          .flatMap(ContentType.parse(_).right.toOption)
                          .getOrElse(ContentTypes.`application/octet-stream`),
                        bytes,
                        data
                      )
                    )
                  )
                case None =>
                  complete(
                    HttpResponse(
                      StatusCodes.NotFound
                    )
                  )
              }
              case Failure(e) =>
                complete(
                  HttpResponse(
                    StatusCodes.InternalServerError,
                    entity = HttpEntity(
                      contentType = ContentTypes.`text/plain(UTF-8)`,
                      data = ByteString(e.getMessage)
                    )
                  )
                )
            }
          }
        }
      }
    )
  }

}
