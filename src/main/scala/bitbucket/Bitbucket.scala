package bitbucket

import akka.actor.{ Actor, ActorLogging }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, headers }
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import utils.ConfigurationReader

class Bitbucket extends Actor with ActorLogging {
  implicit private val system = context.system
  implicit private val mat = ActorMaterializer()
  implicit private val ec = system.dispatcher

  private val authorization = headers.Authorization(BasicHttpCredentials(ConfigurationReader("bitbucket.user"), ConfigurationReader("bitbucket.pass")))

  private def getResponse = {
    Http().singleRequest(
      HttpRequest(uri = Bitbucket.pullRequestUri, headers = List(authorization))
    )
  }

  private def getMessage(response: HttpResponse) = Unmarshal(response.entity).to[String]

  override def receive: Receive = {
    case ("bit-help", channelId: String) =>
      val builder = StringBuilder.newBuilder
      builder.append("Commends List\n")
      Bitbucket.commends.foreach(commend => builder.append(s"$commend\n"))

      sender ! (channelId, builder.toString)
    case ("bit-pull-request", channelId: String) =>
      val sender = this.sender

      for {
        res <- getResponse
        message <- getMessage(res)
      } yield {
        println(sender)
        sender ! (channelId, s"success")
      }
    case _ => println("error")
  }
}

object Bitbucket {
  private val baseUri = ConfigurationReader("bitbucket.baseUri")
  private val projectUri: String = baseUri + "rest/api/1.0/projects/" + ConfigurationReader("bitbucket.projectName")
  private val repositoryUri: String = projectUri + "/repos/" + ConfigurationReader("bitbucket.repositoryName")
  val pullRequestUri: String = repositoryUri + "/pull-requests"

  private val commendPrefix = "bit-"

  val commends: Seq[String] = Seq(
    commendPrefix + "help",
    commendPrefix + "pull-request"
  )

  def containsCommends(commend: String): Boolean = {
    commends.contains(commend)
  }
}
