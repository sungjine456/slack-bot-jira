package jira

import slack.models.Attachment
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

case class JiraContent(private val content: Content) {
  def nonEmpty: Boolean = content.total > 0

  def makeAttachments(issueKey: String): Attachment = {
    val (summary, statusName) = content.issues.headOption.map { issue =>
      (issue.fields.summary, issue.fields.status.name)
    }.get

    Attachment(text = Some(s"<${ Jira.issueUri(issueKey) }|$issueKey> : `$statusName` $summary"))
  }
}

case class Content(total: Int, issues: Seq[Issues])
case class Issues(fields: Fields)
case class Fields(summary: String, status: Status)
case class Status(name: String)

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val statusFormat: RootJsonFormat[Status] = jsonFormat1(Status)
  implicit val fieldsFormat: RootJsonFormat[Fields] = jsonFormat2(Fields)
  implicit val issuesFormat: RootJsonFormat[Issues] = jsonFormat1(Issues)
  implicit val contentFormat: RootJsonFormat[Content] = jsonFormat2(Content)
}
