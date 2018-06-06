import akka.actor.{ ActorSystem, Props }
import slack.{ Slack, SlackState }

object AppLauncher extends App {
  implicit private val system = ActorSystem("slack-jira")

  private val slackReceiverActor = system.actorOf(Props[Slack], "SlackReceiver")

  slackReceiverActor ! SlackState.Receive
}
