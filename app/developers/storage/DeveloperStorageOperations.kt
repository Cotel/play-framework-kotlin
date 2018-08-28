package developers.storage

import arrow.Kind
import arrow.core.Option
import arrow.core.Try
import arrow.core.toOption
import arrow.effects.typeclasses.MonadDefer
import arrow.typeclasses.ApplicativeError
import developers.domain.Developer
import developers.domain.DeveloperError
import io.ebean.Finder
import java.util.UUID

interface DeveloperStorageOperations<F> {

  val AE: ApplicativeError<F, Throwable>
  val DAO: Finder<UUID, DeveloperEntity>

  fun Developer.create(): Kind<F, Developer> = AE.run {
    val developer = this@create
    Try {
      developer.toEntity().save()
    }
      .map { developer }
      .fold({ raiseError(DeveloperError.StorageError) }, { just(it) })
  }

  fun Developer.update(): Kind<F, Developer> = AE.run {
    val developer = this@update
    Try {
      developer.toEntity().update()
    }
      .map { developer }
      .fold({ raiseError(DeveloperError.StorageError) }, { just(it) })
    }

  fun UUID.getById(): Kind<F, Developer> = AE.run {
    val id = this@getById
    Try {
      Option.fromNullable(DAO.query().where().idEq(id).findOne())
        .map { it.toDomain() }
        .fold({ raiseError<Developer>(DeveloperError.NotFound) }, { just(it) })
    }.fold({ raiseError(DeveloperError.StorageError) }, { it })
  }
}