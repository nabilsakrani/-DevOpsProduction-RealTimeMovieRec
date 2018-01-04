import java.io.File

import it.reply.data.pasquali.controller.TestController
import org.scalatest.BeforeAndAfterAll
import org.scalatra.test.scalatest.ScalatraFlatSpec
import com.typesafe.config._

class CotrollerSpec extends ScalatraFlatSpec with BeforeAndAfterAll {


  var config : Config = null

  override def beforeAll(): Unit = {
    super.beforeAll()
    config = ConfigFactory.parseFile(new File("/opt/conf/RealTimeML_test.conf"))
  }

  addServlet(classOf[TestController], "/test/*")

  "At sturtup, a valid archive" must "be in model folder" in {
    //assert(new File("model/m20Model.zip").exists)

    assert(new File(config.getString("rtml.model.archive_path")).exists)
  }

  "Test root api" should "be online and show hello" in {
    get("/test"){

      println(body)

      status should equal (200)
      body should include ("hello")
    }
  }

  "Test nested api" should "be online and show test1" in {
    get("/test/test1"){

      println(body)

      status should equal (200)
      body should include ("test1")
    }
  }

  "The Controller" must "be online" in {
    get("/test/isOnline"){
      status should equal (200)
      body should include ("Status OK")
    }
  }

  it must "be initialized" in {
    get("/test/status"){
      status should equal (200)
      body should include ("sc init:true")
      body should include ("spark init:true")
      body should include ("model loaded:true")
    }
  }

  it must "predict a valid value" in {
    get("/test/predict"){
      assert(status equals 200)

      println("\n\n\n\n\n"+body+"\n\n\n\n\n\n\n")

      var pred : Double = body.toDouble

      assert(pred >= 0.0)
      assert(pred <= 0.1)
    }
  }

}
