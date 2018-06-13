package jira

import slack.models.Attachment
import spray.json.JsObject

case class JiraContent(private val jsValue: JsObject) {
  def check: Boolean = jsValue.fields.get("total").map(total => total.toString() != "0").get

  def makeAttachments(issueKey: String): Option[Seq[Attachment]] = {
    Some(Seq(Attachment(text = Some("<" + Jira.issueUri(issueKey) + "|" + issueKey + "> : "))))
  }
}
