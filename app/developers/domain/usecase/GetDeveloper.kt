package developers.domain.usecase

import arrow.core.Either
import arrow.core.left
import developers.domain.Developer
import developers.domain.DeveloperError
import developers.storage.DeveloperStorageOperations
import java.util.UUID

interface GetDeveloper {

  val storageOperations: DeveloperStorageOperations

  fun UUID.getDeveloperWithId(): Either<DeveloperError, Developer> {
    val id = this
    return storageOperations.run {
      id.getById().fold(
        ifFailure = { DeveloperError.StorageError.left() },
        ifSuccess = { it.toEither { DeveloperError.NotFound } }
      )
    }
  }

}