package com.ldaniels528.trifecta.sandboxes

import KafkaSandbox._
import java.util.Properties
import java.util.concurrent.TimeUnit

import kafka.server.{KafkaConfig, KafkaServerStartable}
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryOneTime
import org.apache.curator.test.TestingServer
import org.slf4j.LoggerFactory

import scala.util.Try

/**
 * Kafka Sandbox Server
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class KafkaSandbox() {
  // start the local Zookeeper instance
  val testServer = new TestingServer(true)
  val cli = CuratorFrameworkFactory.newClient(testServer.getConnectString, new RetryOneTime(2000))
  cli.blockUntilConnected(3, TimeUnit.SECONDS)

  // define the Kafka properties
  val kafkaProperties = new Properties()
  kafkaProperties.setProperty("zookeeper.connect", testServer.getConnectString)
  kafkaProperties.setProperty("broker.id", "1")

  // start local Kafka broker
  logger.info("Starting local Kafka broker...")
  private val kafkaServer = new KafkaServerStartable(new KafkaConfig(kafkaProperties))
  kafkaServer.startup()

  def getConnectString: String = testServer.getConnectString

  def stop() {
    logger.info("Stopping Kafka...")
    Try(kafkaServer.shutdown())

    logger.info("Stopping Zookeeper...")
    Try(cli.close())
    Try(testServer.close())
    ()
  }

}

/**
 * Kafka Sandbox Server Singleton
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object KafkaSandbox {
  private val logger = LoggerFactory.getLogger(getClass)
  private var instance: Option[KafkaSandbox] = None

  /**
   * Creates or retrieves a local Kafka server instance
   * @return a local Kafka server instance
   */
  def apply(): KafkaSandbox = {
    instance.getOrElse {
      val KafkaSandbox = new KafkaSandbox()
      instance = Option(KafkaSandbox)
      KafkaSandbox
    }
  }

  /**
   * Optionally returns an instance of the local Kafka instance
   * @return the option of a local Kafka instance
   */
  def getInstance: Option[KafkaSandbox] = instance

}