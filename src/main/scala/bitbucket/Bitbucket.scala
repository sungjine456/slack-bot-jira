package bitbucket

import akka.actor.{ Actor, ActorLogging }
import akka.stream.ActorMaterializer
import helper.HttpHelper
import bitbucket.MyJsonProtocol._
import spray.json._
import utils.ConfigurationReader

class Bitbucket extends Actor with ActorLogging with HttpHelper {
  implicit private val system = context.system
  implicit private val mat = ActorMaterializer()
  implicit private val ec = system.dispatcher

  override protected val authId = ConfigurationReader("bitbucket.user")
  override protected val authPassword = ConfigurationReader("bitbucket.pass")

  override def receive: Receive = {
    case ("bit-help", channelId: String) =>
      val builder = StringBuilder.newBuilder
      builder.append("Commends List\n")
      Bitbucket.commends.foreach(commend => builder.append(s"$commend\n"))

      sender ! (channelId, builder.toString)
    case ("bit-pull-request-size", channelId: String) =>
      val sender = this.sender

      for {
        res <- getResponse(Bitbucket.pullRequestUri)
        message <- getMessage(res)
      } yield {
        val bitbucketContent = BitbucketContent(message.parseJson.convertTo[PullRequests])

        sender ! (channelId, bitbucketContent.printPullRequestSize)
      }
    case ("bit-un-reviewed", channelId: String) =>
      val sender = this.sender

      for {
        res <- getResponse(Bitbucket.pullRequestUri)
        message <- getMessage(res)
      } yield {
        val bitbucketContent = BitbucketContent(message.parseJson.convertTo[PullRequests])

        sender ! (channelId, bitbucketContent.unReviewer)
      }
    case _ => println("error")
  }
}

object Bitbucket {
  private val baseUri = ConfigurationReader("bitbucket.baseUri")
  private val projectUri: String = s"${ baseUri }rest/api/1.0/projects/${ ConfigurationReader("bitbucket.projectName") }/"
  private val repositoryUri: String = s"${ projectUri }repos/${ ConfigurationReader("bitbucket.repositoryName") }"
  val pullRequestUri: String = s"$repositoryUri/pull-requests"

  private val commendPrefix = "bit-"

  val commends: Seq[String] = Seq(
    "help", "pull-request-size", "un-reviewed"
  ).map(commend => commendPrefix + commend)

  def containsCommends(commend: String): Boolean = {
    commends.contains(commend)
  }
}
