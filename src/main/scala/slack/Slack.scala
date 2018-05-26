package slack

import akka.actor.{ Actor, Props }
import jira.Jira
import slack.rtm.SlackRtmClient
import utils.ConfigurationReader

class Slack extends Actor {
  implicit val system = context.system

  val client = SlackRtmClient(ConfigurationReader("slack.token"))

  val watcher = system.actorOf(Props(new Jira(self)), "Jira")

  override def receive: Receive = {
    case uri: String =>
      client.sendMessage(client.state.getChannelIdForName("general").get, uri)
    case SlackState.Receive =>
      client.onMessage { message =>
        if (message.text.toLowerCase.contains(Jira.issueKey + "-")) watcher ! message.text
      }
  }
}
