package com.matt.cloud.rest.entities

import akka.NotUsed
import akka.stream.alpakka.s3.ObjectMetadata
import akka.stream.scaladsl.Source
import akka.util.ByteString

final case class S3File(
                         bucket: String,
                         key: String,
                         bytes: Long,
                         metadata: ObjectMetadata,
                         source: Source[ByteString, NotUsed]
                       )
