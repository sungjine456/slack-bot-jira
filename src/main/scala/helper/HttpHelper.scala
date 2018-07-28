package helper

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, headers }
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer

trait HttpHelper {
  protected val authId: String
  protected val authPassword: String

  private def authorization = headers.Authorization(BasicHttpCredentials(authId, authPassword))

  protected def getResponse(uri: String)(implicit actorSystem: ActorSystem): Future[HttpResponse] = {
    Http().singleRequest(
      HttpRequest(uri = uri, headers = List(authorization))
    )
  }

  protected def getMessage(response: HttpResponse)(implicit mat: Materializer): Future[String] = Unmarshal(response.entity).to[String]
}
