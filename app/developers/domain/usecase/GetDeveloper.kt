package developers.domain.usecase

import arrow.Kind
import arrow.core.Either
import arrow.core.left
import developers.domain.Developer
import developers.domain.DeveloperError
import developers.storage.DeveloperStorageOperations
import java.util.UUID

interface GetDeveloper<F> {

  val storageOperations: DeveloperStorageOperations<F>

  fun UUID.getDeveloperWithId(): Kind<F, Developer> {
    val id = this
    return storageOperations.run {
      id.getById()
    }
  }

}