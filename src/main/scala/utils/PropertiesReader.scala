package utils

import scala.io.Source

object PropertiesReader {
  def apply(key: String): String = map(key)

  private val reader = Source.fromFile("src/main/resources/key.properties").bufferedReader()

  private var map: Map[String, String] = Map.empty

  reader.lines().forEach { line =>
    val prefix = line.indexOf("=")
    println(line.substring(0, prefix) -> line.substring(prefix + 1))
    map += (line.substring(0, prefix) -> line.substring(prefix + 1))
  }
}
