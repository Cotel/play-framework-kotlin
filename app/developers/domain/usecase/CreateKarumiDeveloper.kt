package developers.domain.usecase

import arrow.Kind
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrow.typeclasses.ApplicativeError
import developers.domain.Developer
import developers.domain.DeveloperError
import developers.domain.DeveloperValidator
import developers.storage.DeveloperStorageOperations

interface CreateKarumiDeveloper<F> {

  val storageOperations: DeveloperStorageOperations<F>

  fun Developer.createKarumiDeveloper(): Kind<F, Developer> {
    val developer = this
    return storageOperations.run {
      AE.run {
        validKarumiDeveloper(developer)
          .fold({ raiseError(it) }, { it.create() })
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