package jira

import spray.json.JsObject

case class JiraContent(private val jsValue: JsObject) {
  def check: Boolean = jsValue.fields.get("total").map(total => total.toString() != "0").get
}
