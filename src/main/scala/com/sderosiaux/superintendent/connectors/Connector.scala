package com.sderosiaux.superintendent.connectors

case class Connector[A](name: String, specific: A)

sealed trait ConnectorDetails
case class PostgreSQL(connection: String) extends ConnectorDetails
case class Elasticsearch(address: String, props: Map[String, String]) extends ConnectorDetails
case class Kafka(brokers: String, props: Map[String, String]) extends ConnectorDetails
case class Api(url: String, headers: Map[String, String]) extends ConnectorDetails
