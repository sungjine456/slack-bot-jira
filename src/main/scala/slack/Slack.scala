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
    case (channelId: String, attachments: Option[Seq[Attachment]]) => apiClient.postChatMessage(channelId, "", attachments = attachments)
    case SlackState.Receive =>
      rtmClient.onMessage { message =>
        if (message.text.toUpperCase.contains(Jira.issueKey + "-")) jiraActor ! (message.text, message.channel)
      }
  }
}
