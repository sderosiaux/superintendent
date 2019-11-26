package com.sderosiaux.superintendent.format

case class Format[A](name: String, specific: A)

sealed trait FormatDetails
case object Json extends FormatDetails
case class AvroSchemaRegistry(schemaRegistryUrl: String, props: Map[String, String]) extends FormatDetails
case object DateTime extends FormatDetails
