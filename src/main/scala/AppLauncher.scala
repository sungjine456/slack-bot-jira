import scala.io.Source

import akka.actor.ActorSystem
import slack.rtm.SlackRtmClient

object AppLauncher extends App {
  val token = Source.fromFile("src/main/resources/key.properties").getLines().next().substring(6)

  implicit val system = ActorSystem("slack")
  implicit val ec = system.dispatcher

  val client = SlackRtmClient(token)

  client.onMessage { message =>
    if(message.text.toLowerCase.contains("hi")) {
      client.sendMessage(message.channel, "안녕")
    } else if(message.text.toLowerCase.contains("hello")) {
      client.sendMessage(message.channel, "여보세요")
    }
  }
}
