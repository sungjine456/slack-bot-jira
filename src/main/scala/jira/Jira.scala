package jira

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import utils.PropertiesReader

class Jira {
  def Run(message: String): Future[HttpResponse] = {
    implicit val system = ActorSystem("jira")
    implicit val executionContext = system.dispatcher

    val authorization = headers.Authorization(BasicHttpCredentials(PropertiesReader("jira.user"), PropertiesReader("jira.pass")))

    Http().singleRequest(
      HttpRequest(uri = Jira.searchUri + message, headers = List(authorization))
    )
  }
}

object Jira {
  private val baseUri = PropertiesReader("baseUri")
  private val searchUri = baseUri + "rest/api/2/search?jql=issue="

  def issueUri(issueKey: String): String = baseUri + "browse/" + issueKey.toUpperCase
}
