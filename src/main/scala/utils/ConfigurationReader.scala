package utils

import com.typesafe.config.ConfigFactory

object ConfigurationReader {
  def apply(key: String): String = config.getString(key)

  private val config = ConfigFactory.load("key")
}
