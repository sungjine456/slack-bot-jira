package slack

import akka.actor.{ Actor, Props }
import bitbucket.Bitbucket
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
  private val jiraActor = system.actorOf(Props[Jira], "Jira")
  private val bitbucketActor = system.actorOf(Props[Bitbucket], "Bitbucket")

  override def receive: Receive = {
    case (channelId: String, attachment: Attachment) => apiClient.postChatMessage(channelId, "", attachments = Some(Seq(attachment)))
    case (channelId: String, message: String) => apiClient.postChatMessage(channelId, message)
    case SlackState.Receive =>
      rtmClient.onMessage { message =>
        val text = message.text
        if (Jira.containsIssueKey(text))
          Jira.getIssuesInText(text).foreach(issueKey => jiraActor tell((issueKey, message.channel), self))
        else if(Bitbucket.containsCommends(text))
          bitbucketActor tell((text, message.channel), self)
      }
  }
}
