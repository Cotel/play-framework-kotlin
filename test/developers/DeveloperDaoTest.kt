package developers

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

  val dao = object : DeveloperStorageOperations {
    override val DAO: Finder<UUID, DeveloperEntity> = DeveloperEntity.DAO
  }

  @Test
  fun `developer should be updated`() {
    with(dao) {
      val developer = givenDeveloper()
      developer.create()

      val developerUpdate = developer.copy(username = "Pedro")
      val updatedDeveloper = developerUpdate.update().getOrNull()
      val obtainedDeveloper = developer.id.getById().getOrNull()?.getOrNull()

      assertEquals(developerUpdate, updatedDeveloper)
      assertEquals(developerUpdate, obtainedDeveloper)
    }
  }
}