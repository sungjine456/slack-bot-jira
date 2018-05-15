import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import jira.Jira
import slack.rtm.SlackRtmClient
import utils.PropertiesReader

object AppLauncher extends App {
  implicit val system = ActorSystem("slack")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val client = SlackRtmClient(PropertiesReader("slack.token"))
  val issuePrefix = PropertiesReader("jira.issueKey").toLowerCase

  val jira = new Jira

  client.onMessage { message =>
    if (message.text.toLowerCase.contains(s"$issuePrefix-")) {
      jira.Run(message.text).onComplete {
        case Success(res) =>
          val str = Await.result(Unmarshal(res.entity).to[String], 5.seconds)
          if (check(str)) client.sendMessage(message.channel, Jira.issueUri(message.text))
        case Failure(_) =>
      }
    }
  }

  private def check(str: String): Boolean = {
    val totalStr = "total\":"
    val totalLength = totalStr.length
    val totalValuePosition = str.indexOf(totalStr)
    val totalValue = str.substring(totalValuePosition + totalLength, totalValuePosition + totalLength + 1)

    totalValue != "0"
  }
}
