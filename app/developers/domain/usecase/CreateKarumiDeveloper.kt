package developers.domain.usecase

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import developers.domain.Developer
import developers.domain.DeveloperError
import developers.domain.DeveloperValidator
import developers.storage.DeveloperStorageOperations

interface CreateKarumiDeveloper {

  val storageOperations: DeveloperStorageOperations

  fun Developer.createKarumiDeveloper(): Either<DeveloperError, Developer> {
    val developer = this
    return storageOperations.run {
      validKarumiDeveloper(developer)
        .flatMap { validatedDeveloper ->
          validatedDeveloper.create()
            .toEither()
            .mapLeft { DeveloperError.StorageError }
        }
    }
  }

  private fun validKarumiDeveloper(developer: Developer): Either<DeveloperError, Developer> =
    if (DeveloperValidator.isKarumiDeveloper(developer)) {
      developer.right()
    } else {
      DeveloperError.NotKarumier.left()
    }
}