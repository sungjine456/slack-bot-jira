package jira

import scala.util.{ Failure, Success }

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import utils.ConfigurationReader

class Jira(slackActor: ActorRef) extends Actor with ActorLogging {
  implicit val system = context.system
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher

  val authorization = headers.Authorization(BasicHttpCredentials(ConfigurationReader("jira.user"), ConfigurationReader("jira.pass")))

  override def receive: Receive = {
    case (issueKey: String, channelId: String) =>
      Http().singleRequest(
        HttpRequest(uri = Jira.searchUri + issueKey, headers = List(authorization))
      ).onComplete {
        case Success(response) => Unmarshal(response.entity).to[String].onComplete {
          case Success(message) =>
            if (check(message)) slackActor ! (channelId, Jira.issueUri(issueKey))
          case Failure(e) => log.debug(e.getMessage)
        }
        case Failure(e) => log.debug(e.getMessage)
      }
    case _ =>
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
  val issueKey: String = ConfigurationReader("jira.issueKey").toUpperCase

  def issueUri(issueKey: String): String = baseUri + "browse/" + issueKey
}
