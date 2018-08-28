package controllers

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

  private val developerStorageOperations = object : DeveloperStorageOperations {
    override val DAO: Finder<UUID, DeveloperEntity> = DeveloperEntity.DAO
  }

  fun index(): Result = ok("Your new application is ready.")

  fun createDeveloper(): CompletionStage<Result> {
    val createKarumiDeveloperUseCase = object : CreateKarumiDeveloper {
      override val storageOperations: DeveloperStorageOperations = developerStorageOperations
    }

    return createKarumiDeveloperUseCase.run {
      readAsyncJsonBody<NewDeveloperJson> {
        it.toDomain().createKarumiDeveloper()
          .fold(
            ifLeft = this@Application::processError,
            ifRight = this@Application::created
          )
      }
    }
  }

  fun getDeveloper(developerId: String): CompletionStage<Result> {
    val getKarumiDeveloper = object : GetDeveloper {
      override val storageOperations: DeveloperStorageOperations = developerStorageOperations
    }

    return getKarumiDeveloper.run {
      async {
        val parsedId = UUID.fromString(developerId)
        parsedId.getDeveloperWithId().fold(
          ifLeft = this@Application::processError,
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