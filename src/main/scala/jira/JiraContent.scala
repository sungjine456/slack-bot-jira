package jira

import slack.models.Attachment
import spray.json.{ JsArray, JsObject, JsValue }

case class JiraContent(private val jsValue: JsObject) {
  def check: Boolean = jsValue.fields.get("total").map(total => total.toString() != "0").get

  def makeAttachments(issueKey: String): Option[Seq[Attachment]] = {
    val (summary, statusName) = jsValue.fields("issues").asInstanceOf[JsArray].elements.headOption.map { value =>
      val fieldsObj = value.asJsObject.fields("fields").asJsObject.fields
      val statusObj = fieldsObj("status").asJsObject.fields

      (jsValueConvertString(fieldsObj("summary")), jsValueConvertString(statusObj("name")))
    }.get

    Some(Seq(Attachment(text = Some(s"<${Jira.issueUri(issueKey)}|$issueKey> : `$statusName` $summary"))))
  }

  private def jsValueConvertString(value: JsValue) = {
    val str = value.toString()
    str.substring(1, str.length - 1)
  }
}
