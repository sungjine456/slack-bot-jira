import akka.actor.{ ActorSystem, Props }
import slack.{ Slack, SlackState }

object AppLauncher extends App {
  implicit val system = ActorSystem("slack-jira")

  val client = system.actorOf(Props[Slack], "SlackReceiver")

  client ! SlackState.Receive
}
