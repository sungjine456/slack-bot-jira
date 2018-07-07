package jira

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import jira.MyJsonProtocol._
import spray.json._
import utils.ConfigurationReader

class Jira(slackActor: ActorRef) extends Actor with ActorLogging {
  implicit private val system = context.system
  implicit private val mat = ActorMaterializer()
  implicit private val ec = system.dispatcher

  private val authorization = headers.Authorization(BasicHttpCredentials(ConfigurationReader("jira.user"), ConfigurationReader("jira.pass")))

  private def getResponse(issueKey: String) = {
    Http().singleRequest(
      HttpRequest(uri = Jira.searchUri + issueKey, headers = List(authorization))
    )
  }

  private def getMessage(response: HttpResponse) = Unmarshal(response.entity).to[String]

  override def receive: Receive = {
    case (issueKey: String, channelId: String) =>
      for {
        res <- getResponse(issueKey)
        message <- getMessage(res)
      } yield {
        val jiraContent = JiraContent(message.parseJson.convertTo[Content])

        if (jiraContent.nonEmpty) slackActor ! (channelId, jiraContent.makeAttachments(issueKey))
      }
    case _ =>
  }
}

object Jira {
  private val baseUri = ConfigurationReader("jira.baseUri")
  private val searchUri = baseUri + "rest/api/2/search?jql=issue="
  private val issueKey: String = ConfigurationReader("jira.issueKey").toUpperCase
  private val regex = s"$issueKey-[0-9]+".r

  def issueUri(issueKey: String): String = baseUri + "browse/" + issueKey

  def containsIssueKey(text: String): Boolean = regex.findFirstIn(text.toUpperCase).nonEmpty

  def getIssuesInText(text: String): Set[String] = regex.findAllIn(text.toUpperCase).toSet
}
