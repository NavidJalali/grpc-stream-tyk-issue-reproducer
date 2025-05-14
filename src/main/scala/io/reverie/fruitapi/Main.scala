package io.reverie.fruitapi

import com.google.protobuf.empty.Empty
import com.google.rpc.Code
import com.google.rpc.scalapb.ResourceInfo
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.grpc.GrpcServiceException
import org.apache.pekko.grpc.scaladsl.Metadata
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.stream.scaladsl.Source

import java.security.SecureRandom
import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}

final class FruitAPIImpl extends FruitAPIPowerApi {
  private val random = SecureRandom()

  private def response: GetFruitResponse =
    GetFruitResponse(
      Some(
        Fruit(
          random.nextInt(3) match
            case 0 =>
              val color = random.nextInt(3) match
                case 0 => Fruit.Apple.Color.RED
                case 1 => Fruit.Apple.Color.GREEN
                case 2 => Fruit.Apple.Color.YELLOW
              Fruit.Fruit.Apple(Fruit.Apple(color))
            case 1 =>
              val length = random.nextDouble() * 10
              Fruit.Fruit.Banana(Fruit.Banana(length))
            case 2 =>
              val radius = random.nextDouble() * 8
              Fruit.Fruit.Orange(Fruit.Orange(radius))
        )
      )
    )

  private val error: GrpcServiceException =
    GrpcServiceException(
      Code.RESOURCE_EXHAUSTED, "Ran out of fruits :(",
      List(
        ResourceInfo(
          resourceType = "Fruit",
          resourceName = "io.reverie.Fruit",
          owner = "io.reverie",
          description = "apples, bananas, and oranges",
        )
      )
    )

  override def getFruits(in: GetFruitRequest, metadata: Metadata): Source[GetFruitResponse, NotUsed] =
    if in.succeed then
      Source.repeat(()).map(_ => response).take(in.emit)
    else
      Source
        .unfoldAsync(in.emit) {
          case 0 => Future.failed(error)
          case state => Future.successful(Some((state - 1, response)))
        }

  override def healthCheck(in: Empty, metadata: Metadata): Future[Empty] =
    Future.successful(Empty())
}

@main def main(): Unit =

  given system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "FruitAPI")

  given ExecutionContext = system.executionContext

  Http().newServerAt("0.0.0.0", 50051)
    .bind(FruitAPIPowerApiHandler(FruitAPIImpl()))
    .andThen {
      case Success(binding) =>
        println(s"gRPC server online at ${binding.localAddress.getHostString}:${binding.localAddress.getPort}")
      case Failure(exception) =>
        println(s"Failed to bind gRPC endpoint, terminating system: $exception")
        system.terminate()
    }

