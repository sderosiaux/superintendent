package com.sderosiaux.superintendent.mapping

import com.sderosiaux.superintendent.connectors.{Connector, ConnectorDetails}
import com.sderosiaux.superintendent.format.Format

case class Mapping[A, B](
                          name: String,
                          connector: Connector[B],
                          unique: Boolean,
                          masterData: Boolean,
                          waitForMasterData: Boolean,
                          subKeyPath: Option[String],
                          specific: A
)

sealed trait MappingConfig

case class PostgreSQLMapping(table: String, key: String, columns: List[PostgreSQLMappingColumn]) extends MappingConfig
case class PostgreSQLMappingColumn(name: String, format: Format[_], projection: Option[String])

case class KafkaMapping(topic: String, value: KafkaMappingValue) extends MappingConfig
case class KafkaMappingValue(format: Format[_], projection: Option[String])

case class ApiMapping(format: Format[_]) extends MappingConfig

case class ElasticMapping(index: String, columns: List[ElasticMappingColumn]) extends MappingConfig
case class ElasticMappingColumn(name: String, format: Format[_], projection: Option[String])
