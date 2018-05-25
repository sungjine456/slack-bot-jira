import akka.actor.ActorSystem
import jira.Jira
import slack.rtm.SlackRtmClient
import utils.ConfigurationReader

object AppLauncher extends App {
  implicit val system = ActorSystem("slack")
  implicit val executionContext = system.dispatcher

  val client = SlackRtmClient(ConfigurationReader("slack.token"))

  val jira = new Jira

  client.onMessage { message =>
    if (message.text.toLowerCase.contains(Jira.issueKey + "-")) {
      jira.getUri(message.text).map(uri => client.sendMessage(message.channel, uri))
    }
  }
}
