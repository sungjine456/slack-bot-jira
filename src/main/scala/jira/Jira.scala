package jira

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import utils.ConfigurationReader

class Jira {
  def getUri(message: String): Option[String] = {
    implicit val system = ActorSystem("jira")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val authorization = headers.Authorization(BasicHttpCredentials(ConfigurationReader("jira.user"), ConfigurationReader("jira.pass")))

    var uri: Option[String] = None

    val httpResponse = Http().singleRequest(
      HttpRequest(uri = Jira.searchUri + message, headers = List(authorization))
    )
    val res = Await.result(httpResponse, 2.seconds)
    val result = Await.result(Unmarshal(res.entity).to[String], 2.seconds)

    if (check(result)) uri = Some(Jira.issueUri(message))

    uri
  }

  private def check(str: String): Boolean = {
    val totalStr = "total\":"
    val totalLength = totalStr.length
    val totalValuePosition = str.indexOf(totalStr)
    val totalValue = str.substring(totalValuePosition + totalLength, totalValuePosition + totalLength + 1)

    totalValue != "0"
  }
}

object Jira {
  private val baseUri = ConfigurationReader("jira.baseUri")
  private val searchUri = baseUri + "rest/api/2/search?jql=issue="
  val issueKey: String = ConfigurationReader("jira.issueKey").toLowerCase

  def issueUri(issueKey: String): String = baseUri + "browse/" + issueKey.toUpperCase
}
