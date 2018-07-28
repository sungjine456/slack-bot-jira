package jira

import akka.actor.{ Actor, ActorLogging }
import akka.stream.ActorMaterializer
import helper.HttpHelper
import jira.MyJsonProtocol._
import spray.json._
import utils.ConfigurationReader

class Jira extends Actor with ActorLogging with HttpHelper {
  implicit private val system = context.system
  implicit private val mat = ActorMaterializer()
  implicit private val ec = system.dispatcher

  override protected val authId = ConfigurationReader("jira.user")
  override protected val authPassword = ConfigurationReader("jira.pass")

  override def receive: Receive = {
    case (issueKey: String, channelId: String) =>
      val sender = this.sender

      for {
        res <- getResponse(Jira.searchUri(issueKey))
        message <- getMessage(res)
      } yield {
        val jiraContent = JiraContent(message.parseJson.convertTo[Content])

        if (jiraContent.nonEmpty) sender ! (channelId, jiraContent.makeAttachments(issueKey))
      }
    case _ =>
  }
}

object Jira {
  private val baseUri = ConfigurationReader("jira.baseUri")

  private def searchUri(issueKey: String) = s"${ baseUri }rest/api/2/search?jql=issue=$issueKey"

  private val issueKey: String = ConfigurationReader("jira.issueKey").toUpperCase
  private val regex = s"$issueKey-[0-9]+".r

  def issueUri(issueKey: String): String = baseUri + "browse/" + issueKey

  def containsIssueKey(text: String): Boolean = regex.findFirstIn(text.toUpperCase).nonEmpty

  def getIssuesInText(text: String): Set[String] = regex.findAllIn(text.toUpperCase).toSet
}
