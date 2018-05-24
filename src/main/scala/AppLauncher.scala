import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import jira.Jira
import slack.rtm.SlackRtmClient
import utils.ConfigurationReader

object AppLauncher extends App {
  implicit val system = ActorSystem("slack")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val client = SlackRtmClient(ConfigurationReader("slack.token"))

  val jira = new Jira

  client.onMessage { message =>
    if (message.text.toLowerCase.contains(Jira.issueKey + "-")) {
      jira.Run(message.text).onComplete {
        case Success(res) =>
          val str = Await.result(Unmarshal(res.entity).to[String], 5.seconds)
          if (Jira.check(str)) client.sendMessage(message.channel, Jira.issueUri(message.text))
        case Failure(_) =>
      }
    }
  }
}
