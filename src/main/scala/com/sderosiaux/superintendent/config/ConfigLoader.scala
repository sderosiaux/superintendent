package com.sderosiaux.superintendent.config

import com.sderosiaux.superintendent.connectors._
import com.sderosiaux.superintendent.format.{AvroSchemaRegistry, DateTime, Format, Json}
import com.sderosiaux.superintendent.mapping._
import pureconfig.error.ConfigReaderFailures
import pureconfig.module.yaml._
import zio.Task


object ConfigLoader {
  type KV = Map[String, Any]

  def config(): Task[SuperIntendent] = {
    // formats
    val avro = Format("avro", AvroSchemaRegistry("localhost:8081", Map("basic.auth.credentials.source" -> "USER_INFO")))
    val json = Format("json", Json)
    val datetime = Format("datetime", DateTime)

    // connectors
    val pg = Connector("pg", PostgreSQL("postgresql://[user[:password]@][netloc][:port][/dbname]"))

    val es = Connector(
      "elastic-consolidated",
      Elasticsearch(
        "127.0.0.1:9300",
        Map(
          "client.transport.sniff" -> "true",
          "cluster.name" -> "elasticsearch"
        )
      )
    )

    val kafka = Connector("kafka", Kafka("mykafka:9092,mykafka2:9092", Map("sasl.mechanism" -> "PLAIN")))

    val deliveryApi = Connector("delivery", Api("https://myapi.local/delivery/${key}", Map("X-API-Token" -> "admin")))

    val quotationApi =
      Connector("quotation", Api("https://myapi.local/quotation/${key}", Map("X-API-Token" -> "admin")))

    // mappings

    val orders = Mapping(
      "pg-staging",
      pg,
      unique = true,
      masterData = true,
      waitForMasterData = false,
      subKeyPath = None,
      PostgreSQLMapping(
        "orders",
        "id",
        List(PostgreSQLMappingColumn("created_at", datetime, None), PostgreSQLMappingColumn("data", json, None))
      )
    )

    val ordersEventsPg = Mapping(
      "pg-staging-events",
      pg,
      unique = false,
      masterData = false,
      waitForMasterData = false,
      None,
      PostgreSQLMapping(
        "order-events",
        "order_id",
        List(PostgreSQLMappingColumn("event", avro, None))
      )
    )

    val orderEventsKafka = Mapping(
      "kafka-events",
      kafka,
      unique = false,
      masterData = false,
      waitForMasterData = false,
      None,
      KafkaMapping(
        "customer-orders",
        KafkaMappingValue(avro, Some(".data.state"))
      )
    )

    val orderEventsCdcKafka = Mapping(
      "customer-order-cdc",
      kafka,
      unique = false,
      masterData = false,
      waitForMasterData = false,
      None,
      KafkaMapping(
        "customer-orders-cdc",
        KafkaMappingValue(json, Some(".payload.lsn"))
      )
    )

    val quotation = Mapping(
      "quotation",
      quotationApi,
      unique = false,
      masterData = false,
      waitForMasterData = true,
      Some(".quotation.id"),
      ApiMapping(json)
    )

    val delivery = Mapping(
      "delivery",
      deliveryApi,
      unique = false,
      masterData = false,
      waitForMasterData = true,
      Some(".delivery.id"),
      ApiMapping(json)
    )

    val elasticCons = Mapping(
      "consolidation",
      es,
      unique = true,
      masterData = false,
      waitForMasterData = false,
      None,
      ElasticMapping("orders", List(ElasticMappingColumn("_source", json, None)))
    )

    Task.succeed(SuperIntendent(
      List(json, avro, datetime),
      List(pg, es, kafka, deliveryApi, quotationApi),
      List(orders, orderEventsKafka, orderEventsCdcKafka, delivery, quotation)
    ))
  }

  private def yaml(s: String): Either[ConfigReaderFailures, SuperIntendent] = {
    // TODO: dtos? some parts are dynamic, need to do it by hand?
    val all = YamlConfigSource.string(s).value()
    all.map { all =>
      val yformats = all.unwrapped().asInstanceOf[KV].get("formats").map(_.asInstanceOf[KV])
      val yconnectors = all.unwrapped().asInstanceOf[KV].get("connectors").map(_.asInstanceOf[KV])
      val ymappings = all.unwrapped().asInstanceOf[KV].get("mappings").map(_.asInstanceOf[KV])

      // TODO ...

      SuperIntendent(
        List(),
        List(),
        List()
      )
    }
  }

//  def main(args: Array[String]): Unit = {
//    Using.resource(Source.fromFile("C:\\Code\\superintendent\\src\\test\\resources\\superintendent.yaml")) { x =>
//      val yamlS = x.getLines().mkString("\n")
//      println(yaml(yamlS))
//    }
//  }
}
