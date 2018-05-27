package slack

import akka.actor.{ Actor, Props }
import jira.Jira
import slack.rtm.SlackRtmClient
import utils.ConfigurationReader

class Slack extends Actor {
  implicit val system = context.system

  val client = SlackRtmClient(ConfigurationReader("slack.token"))

  val jiraActor = system.actorOf(Props(new Jira(self)), "Jira")

  override def receive: Receive = {
    case message: Tuple2[String, String]=>
      client.sendMessage(message._2, message._1)
    case SlackState.Receive =>
      client.onMessage { message =>
        if (message.text.toUpperCase.contains(Jira.issueKey + "-")) jiraActor ! (message.text, message.channel)
      }
  }
}
