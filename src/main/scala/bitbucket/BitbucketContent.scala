package bitbucket

import spray.json._
import MyJsonProtocol._

case class BitbucketContent(message: String) {

  import ReviewerState._

  private val pullRequests = message.parseJson.convertTo[PullRequests]

  def printPullRequestSize: String = s"저장소에 등록된 PR은 ${ pullRequests.size }개가 있습니다."

  def printUnReviewedPullRequest: String = {
    val reviewer: Map[String, Int] = pullRequests.values.flatMap { pr =>
      pr.reviewers
        .filter(reviewer => reviewer.status == UNAPPROVED.toString)
        .map(reviewer => reviewer.user.name)
    }.groupBy(identity).mapValues(_.size)

    val result = StringBuilder.newBuilder

    if(reviewer.nonEmpty)
      reviewer.foreach(s => result.append(s"${ s._1 }님은 ${ s._2 }개의 리뷰가 남았습니다.\n"))
    else
      result.append("모두 리뷰가 된 상태입니다.")

    result.toString()
  }
}

case class PullRequests(size: Int, values: Seq[PullRequest])
case class PullRequest(reviewers: Seq[Reviewer])
case class Reviewer(user: User, status: String)
case class User(name: String)

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat1(User)
  implicit val reviewerFormat: RootJsonFormat[Reviewer] = jsonFormat2(Reviewer)
  implicit val pullRequestFormat: RootJsonFormat[PullRequest] = jsonFormat1(PullRequest)
  implicit val pullRequestsFormat: RootJsonFormat[PullRequests] = jsonFormat2(PullRequests)
}
