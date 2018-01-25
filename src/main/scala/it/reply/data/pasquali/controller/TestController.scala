package it.reply.data.pasquali.controller

import org.scalatra.{FlashMapSupport, ScalatraServlet}
import org.scalatra.scalate.ScalateSupport

class TestController extends ScalatraServlet with FlashMapSupport with ScalateSupport {

  var CONF_DIR = "conf"
  var CONFIG_FILE = "RealTimeML_staging.conf"
  var controller : Controller = null

  def initController() = {
    controller = new Controller
    controller.initSpark()
  }


  get("/isOnline"){

    if(controller == null)
      initController()

    "Status OK"
  }

  get("/status"){

    if(controller == null)
      initController()

    var status = "Status:\n"
    status += s"sc init:${controller.collabModel.sc != null}\n"
    status += s"spark init:${controller.collabModel.spark != null}\n"
    status += s"model loaded:${controller.collabModel.model != null}"

    status
  }

  get("/predict"){

    if(controller == null)
      initController()

    s"${controller.collabModel.predictRating(1,5)}"
  }


  get("/test1"){
    "test1"
  }


  get("/"){
    "hello"
  }
}
