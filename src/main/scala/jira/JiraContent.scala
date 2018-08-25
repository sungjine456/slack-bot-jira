package jira

import org.joda.time.format.{ DateTimeFormat, DateTimeFormatter }
import slack.models.Attachment
import spray.json._
import JiraContent.datePattern
import jira.MyJsonProtocol._

case class JiraContent(message: String) {

  private val content: Content = message.parseJson.convertTo[Content]

  def nonEmpty: Boolean = content.total > 0

  def makeAttachments(issueKey: String): Attachment = {
    content.issues.headOption.map { issue =>
      val date = datePattern.parseDateTime(issue.fields.updated)

      (issue.fields.summary, issue.fields.status.name, issue.fields.assignee.name, date.toLocalDate)
    } match {
      case Some((summary, statusName, assigneeName, updated)) =>
        Attachment(title = Some(s"<${ Jira.issueUri(issueKey) }|$issueKey> : `$statusName` $summary"),
          text=Some(s"$assigneeName | $updated"))
      case None => Attachment(text = Some(s"<${ Jira.issueUri(issueKey) }|$issueKey>"))
    }
  }
}

object JiraContent {
  val datePattern: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-mm-dd'T'HH:mm:ss.000Z")
}

case class Content(total: Int, issues: Seq[Issues])
case class Issues(fields: Fields)
case class Fields(summary: String, status: Status, assignee: Assignee, updated: String)
case class Status(name: String)
case class Assignee(name: String)

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val assigneeFormat: RootJsonFormat[Assignee] = jsonFormat1(Assignee)
  implicit val statusFormat: RootJsonFormat[Status] = jsonFormat1(Status)
  implicit val fieldsFormat: RootJsonFormat[Fields] = jsonFormat4(Fields)
  implicit val issuesFormat: RootJsonFormat[Issues] = jsonFormat1(Issues)
  implicit val contentFormat: RootJsonFormat[Content] = jsonFormat2(Content)
}
