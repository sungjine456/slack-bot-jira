import akka.actor.{ ActorSystem, Props }
import slack.{ Slack, SlackState }

object AppLauncher extends App {
  implicit val system = ActorSystem("slack-jira")

  val slackReceiverActor = system.actorOf(Props[Slack], "SlackReceiver")

  slackReceiverActor ! SlackState.Receive
}
