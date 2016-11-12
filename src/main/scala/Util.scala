import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.hunorkovacs.koauth.domain.KoauthRequest
import com.hunorkovacs.koauth.service.consumer.{DefaultConsumerService, RequestWithInfo}
import org.json4s.DefaultFormats

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

object Util {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val formats = DefaultFormats

  val map = TrieMap[String,Int]()

  val oAuthRequest: Future[RequestWithInfo] = {
    val consumer = new DefaultConsumerService(system.dispatcher)
    consumer.createOauthenticatedRequest(
      KoauthRequest(
        method = "GET",
        url = Config.url,
        authorizationHeader = None,
        body = None
      ),
      Config.consumerKey,
      Config.consumerSecret,
      Config.accessToken,
      Config.accessTokenSecret
    )
  }

  def requestStreamWithAuth(header : String): Future[HttpResponse] = {
    val httpHeaders: List[HttpHeader] = List(
      HttpHeader.parse("Authorization", header) match {
        case ParsingResult.Ok(h, _) => Some(h)
        case _ => None
      },
      HttpHeader.parse("Accept", "*/*") match {
        case ParsingResult.Ok(h, _) => Some(h)
        case _ => None
      }
    ).flatten

    val httpRequest: HttpRequest = HttpRequest(
      method = HttpMethods.GET,
      uri = Uri(Config.url),
      headers = httpHeaders
    )

    Http().singleRequest(httpRequest)
  }

  def toStreamSource(response : HttpResponse) : Source[String,Any] = {
    if (response.status.intValue() != 200) {
      println(response.entity.dataBytes.runForeach(_.utf8String))
      Source.empty
    } else {
      response.entity.dataBytes
        .scan("")((acc, curr) => if (acc.contains("\r\n")) curr.utf8String else acc + curr.utf8String)
        .filter(_.contains("\r\n"))
    }
  }
}