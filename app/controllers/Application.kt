package controllers

import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.applicativeError
import arrow.effects.fix
import arrow.typeclasses.ApplicativeError
import developers.NewDeveloperJson
import developers.domain.Developer
import developers.domain.DeveloperError
import developers.domain.usecase.CreateKarumiDeveloper
import developers.domain.usecase.GetDeveloper
import developers.storage.DeveloperStorageOperations
import developers.storage.DeveloperEntity
import developers.toDomain
import io.ebean.Finder
import play.mvc.Controller
import play.mvc.Result
import java.util.UUID
import java.util.concurrent.CompletionStage

class Application : Controller(), ParseableJson {

  private val developerStorageOperations = object : DeveloperStorageOperations<ForIO> {
    override val DAO: Finder<UUID, DeveloperEntity> = DeveloperEntity.DAO
    override val AE: ApplicativeError<ForIO, Throwable> = IO.applicativeError()
  }

  fun index(): Result = ok("Your new application is ready.")

  fun createDeveloper(): CompletionStage<Result> {
    val createKarumiDeveloperUseCase = object : CreateKarumiDeveloper<ForIO> {
      override val storageOperations: DeveloperStorageOperations<ForIO> = developerStorageOperations
    }

    return createKarumiDeveloperUseCase.run {
      readJsonBody<NewDeveloperJson> { newDeveloperJson ->
        newDeveloperJson.toDomain().createKarumiDeveloper().fix().attempt().unsafeRunSync()
          .fold(
            ifLeft = { processError(it as DeveloperError) },
            ifRight = this@Application::created
          ).let { it.completeFuture() }
      }
    }
  }

  fun getDeveloper(developerId: String): CompletionStage<Result> {
    val getKarumiDeveloper = object : GetDeveloper<ForIO> {
      override val storageOperations: DeveloperStorageOperations<ForIO> = developerStorageOperations
    }

    return getKarumiDeveloper.run {
      async {
        val parsedId = UUID.fromString(developerId)
        parsedId.getDeveloperWithId().fix().attempt().unsafeRunSync()
          .fold(
            ifLeft = { processError(it as DeveloperError) },
            ifRight = this@Application::ok
          )
      }
    }
  }

  private fun processError(developerError: DeveloperError): Result = when (developerError) {
    DeveloperError.StorageError -> internalServerError()
    DeveloperError.NotFound -> notFound()
    DeveloperError.NotKarumier -> badRequest("Only karumies")
  }

  private fun created(developer: Developer): Result = created(developer.toJson())
  private fun ok(developer: Developer): Result = ok(developer.toJson())
}