package bitbucket

import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

case class BitbucketContent(private val pullRequests: PullRequests) {
  def printPullRequestSize: String = s"저장소에 등록된 PR은 ${pullRequests.size}개가 있습니다."
}

case class PullRequests(size: Int)

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val pullRequestsFormat: RootJsonFormat[PullRequests] = jsonFormat1(PullRequests)
}
