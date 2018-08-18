package bitbucket

import utils.State

object ReviewerState extends State {
  val APPROVED, UNAPPROVED, NEEDS_WORK = Value
}
