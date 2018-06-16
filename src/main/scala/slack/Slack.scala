package slack

import akka.actor.{ Actor, Props }
import jira.Jira
import slack.api.SlackApiClient
import slack.models.Attachment
import slack.rtm.SlackRtmClient
import utils.ConfigurationReader

class Slack extends Actor {
  implicit private val system = context.system
  implicit private val ec = system.dispatcher

  private val token = ConfigurationReader("slack.token")

  private val rtmClient = SlackRtmClient(token)
  private val apiClient = SlackApiClient(token)
  private val jiraActor = system.actorOf(Props(new Jira(self)), "Jira")

  override def receive: Receive = {
    case (channelId: String, attachment: Attachment) => apiClient.postChatMessage(channelId, "", attachments = Some(Seq(attachment)))
    case SlackState.Receive =>
      rtmClient.onMessage { message =>
        Jira.getIssuesInText(message.text.toUpperCase).foreach(jiraActor ! (_, message.channel))
      }
  }
}
