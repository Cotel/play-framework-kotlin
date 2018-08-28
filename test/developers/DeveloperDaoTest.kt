package developers

import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.applicativeError
import arrow.effects.extensions
import arrow.effects.fix
import arrow.typeclasses.ApplicativeError
import arrow.typeclasses.binding
import developers.storage.DeveloperEntity
import developers.storage.DeveloperStorageOperations
import given.GivenDeveloper
import given.givenDeveloper
import io.ebean.Finder
import junit.framework.TestCase.assertEquals
import org.junit.Test
import utils.ApplicationWithDatabase
import utils.getOrNull
import java.util.*

class DeveloperDaoTest : ApplicationWithDatabase(), GivenDeveloper by givenDeveloper {

  val dao = object : DeveloperStorageOperations<ForIO> {
    override val AE: ApplicativeError<ForIO, Throwable> = IO.applicativeError()
    override val DAO: Finder<UUID, DeveloperEntity> = DeveloperEntity.DAO
  }

  @Test
  fun `developer should be updated`() {
    with(dao) {
      ForIO extensions {
        binding {
          val developer = givenDeveloper()
          developer.create().bind()

          val developerUpdate = developer.copy(username = "Pedro")
          val updatedDeveloper = developerUpdate.update().bind()
          val obtainedDeveloper = developer.id.getById().bind()

          assertEquals(developerUpdate, updatedDeveloper)
          assertEquals(developerUpdate, obtainedDeveloper)
        }.fix().unsafeRunSync()
      }
    }
  }
}