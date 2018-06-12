package jira

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import slack.models.Attachment
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

  private def makeAttachments(issueKey: String) = {
    Some(Seq(Attachment(title = Some(issueKey), title_link = Some(Jira.issueUri(issueKey)))))
  }

  override def receive: Receive = {
    case (issueKey: String, channelId: String) =>
      for {
        res <- getResponse(issueKey)
        message <- getMessage(res)
      } yield {
        val jiraContent = JiraContent(message.parseJson.asJsObject)

        if(jiraContent.check) slackActor ! (channelId, makeAttachments(issueKey))
      }
    case _ =>
  }
}

object Jira {
  private val baseUri = ConfigurationReader("jira.baseUri")
  private val searchUri = baseUri + "rest/api/2/search?jql=issue="
  private val issueKey: String = ConfigurationReader("jira.issueKey").toUpperCase
  private val regex = s"$issueKey-[0-9]+".r

  private def issueUri(issueKey: String) = baseUri + "browse/" + issueKey

  def containsIssueKey(text: String): Boolean = regex.findFirstIn(text).nonEmpty

  def getIssuesInText(text: String): Seq[String] = regex.findAllIn(text).toSeq
}
