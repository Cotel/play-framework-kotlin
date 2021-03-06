package controllers

import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.applicativeError
import arrow.effects.fix
import arrow.typeclasses.ApplicativeError
import developers.domain.Developer
import developers.storage.DeveloperEntity
import developers.storage.DeveloperStorageOperations
import given.GivenDeveloper
import given.givenDeveloper
import io.ebean.Finder
import junit.framework.TestCase.assertEquals
import org.junit.Test
import play.mvc.Http.Status.BAD_REQUEST
import play.mvc.Http.Status.CREATED
import play.mvc.Http.Status.NOT_FOUND
import play.mvc.Http.Status.OK
import play.mvc.Result
import play.test.Helpers.fakeRequest
import play.test.Helpers.route
import utils.ApplicationWithDatabase
import utils.asObject
import utils.getOrNull
import java.util.UUID

class ApplicationTest : ApplicationWithDatabase(), ParseableJson, GivenDeveloper by givenDeveloper {

  val dao = object : DeveloperStorageOperations<ForIO> {
    override val AE: ApplicativeError<ForIO, Throwable> = IO.applicativeError()
    override val DAO: Finder<UUID, DeveloperEntity> = DeveloperEntity.DAO
  }

  @Test
  fun `developer POST should create a developer if it's a karumi developer`() {
    val newDeveloper = givenNewKarumiDeveloper()

    val result = postDeveloperRoute(newDeveloper)

    val createdDeveloper = result.asObject(Developer::class)
    val obtainedDeveloper = getById(createdDeveloper.id)

    assertEquals(newDeveloper.username, createdDeveloper.username)
    assertEquals(newDeveloper.email, createdDeveloper.email)
    assertEquals(createdDeveloper, obtainedDeveloper)
    assertEquals(CREATED, result.status())
  }

  @Test
  fun `developer POST shouldn't create a developer if it isn't a karumi developer`() {
    val newDeveloper = givenNewDeveloper()

    val result = postDeveloperRoute(newDeveloper)

    assertEquals(BAD_REQUEST, result.status())
  }

  @Test
  fun `developer POST should returns 400 if the json body isn't the expected`() {
    val result = postDeveloperRoute(InvalidJson())

    assertEquals(BAD_REQUEST, result.status())
  }

  @Test
  fun `developer GET should retrieve by id`() {
    val developer = givenDeveloper().let { create(it) }

    val result = getDeveloperRoute(developer)

    assertEquals(developer, result.asObject(Developer::class))
    assertEquals(OK, result.status())
  }

  @Test
  fun `developer GET should returns 404 code if there isn't the developer in the database`() {
    val developer = givenDeveloper()

    val result = getDeveloperRoute(developer)

    assertEquals(NOT_FOUND, result.status())
  }

  private fun getDeveloperRoute(developer: Developer): Result =
    route(app, fakeRequest("GET", "/developer/${developer.id}"))

  private fun <A> postDeveloperRoute(body: A): Result =
    route(app, fakeRequest("POST", "/developer").bodyJson(body.toJson()))

  private data class InvalidJson(val invalid: String = "")

  private fun getById(id: UUID) = dao.run { id.getById().fix().unsafeRunSync() }
  private fun create(developer: Developer) = dao.run { developer.create().fix().unsafeRunSync() }
}