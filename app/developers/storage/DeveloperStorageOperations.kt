package developers.storage

import arrow.core.Option
import arrow.core.Try
import arrow.core.toOption
import developers.domain.Developer
import io.ebean.Finder
import java.util.UUID

interface DeveloperStorageOperations {

  val DAO: Finder<UUID, DeveloperEntity>

  fun Developer.create(): Try<Developer> = Try {
    this.toEntity().save()
    this
  }

  fun Developer.update(): Try<Developer> = Try {
    this.toEntity().update()
    this
  }

  fun UUID.getById(): Try<Option<Developer>> = Try {
    DAO
      .query()
      .where()
      .idEq(this)
      .findOne()
      ?.toDomain()
      .toOption()
  }
}