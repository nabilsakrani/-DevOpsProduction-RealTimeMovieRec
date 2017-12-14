package it.reply.data.pasquali.controller

import java.io.File

import it.reply.data.pasquali.engine.MovieRecommenderEngine
import it.reply.data.pasquali.view.Template
import org.scalatra.scalate.ScalateSupport
import org.scalatra.{FlashMapSupport, ScalatraServlet}
import org.slf4j.LoggerFactory
import org.zeroturnaround.zip.ZipUtil

import scala.xml.Node

class Controller extends ScalatraServlet with FlashMapSupport with ScalateSupport{

  private def displayPage(title:String, content:Seq[Node]) =
    Template.page(title, content, url(_))

  var collabModel : MovieRecommenderEngine = null

  var modelArchivePath : String = ""
  var modelPath : String = ""
  var modelName : String = ""
  var sparkAppName : String = ""
  var sparkMaster : String = ""

  var FULL_MODEL_PATH = s"${modelPath}${modelName}"

  var movieIDs : Array[Int] = Array()
  var userIDs : Array[Int] = Array()

  var userNodes : Array[Node] = Array()
  var movieNodes : Array[Node] = Array()

  val logger = LoggerFactory.getLogger(getClass)

  def initSpark() : Unit = {

    logger.info("------> Retrieve Environment Variables <------")
    modelArchivePath = scala.util.Properties.envOrElse("MODEL_ZIP_PATH", "model/m20Model.zip")
    modelPath = scala.util.Properties.envOrElse("MODEL_PATH", "model/")
    modelName = scala.util.Properties.envOrElse("MODEL_NAME", "m20Model")
    sparkAppName = scala.util.Properties.envOrElse("SPARK_APP_NAME", "Movielens Real Time ML")
    sparkMaster = scala.util.Properties.envOrElse("SPARK_MASTER", "local[*]")

    logger.info("------> ENV")
    logger.info("------>")
    logger.info(s"------> MODEL_ZIP_PATH = ${modelArchivePath}")
    logger.info(s"------> MODEL_PATH = ${modelPath}")
    logger.info(s"------> MODEL_NAME = ${modelName}")
    logger.info(s"------> SPARK_APP_NAME = ${sparkAppName}")
    logger.info(s"------> SPARK_MASTER = ${sparkMaster}")
    logger.info("------>")
    logger.info(s"------> FULL_MODEL_PATH = ${FULL_MODEL_PATH}")
    logger.info("------> ")

    logger.info("------> Initialize Spark")

    collabModel = MovieRecommenderEngine()
      .init(sparkAppName, sparkMaster)

    logger.info("------> Spark Initialized")
    logger.info("------> Unzip ML Model")
    logger.info(s"------> FULL_MODEL_PATH = ${FULL_MODEL_PATH}")
    logger.info(s"------> FULL_MODEL_PATH = ${modelPath}${modelName}")

    ZipUtil.unpack(new File(modelArchivePath), new File(s"${modelPath}${modelName}"))

    logger.info("------> Load ML Model")

    collabModel.loadCollaborativeModel(s"${modelPath}${modelName}")

    logger.info("------> Populate Movies and Users Lists")

    userIDs = collabModel.model.userFeatures.sortByKey().collect().map(row => row._1)
    movieIDs = collabModel.model.productFeatures.sortByKey().collect().map(row => row._1)

//    userIDs.foreach(println)
//    movieIDs.foreach(println)

    userNodes = userIDs.map(id => <li>{id}</li>)
    movieNodes = movieIDs.map(id => <li>{id}</li>)
  }

  get("/") {

    if(collabModel == null)
      initSpark()

    val users = ""
    val movies = ""


    displayPage("Movielens Recommender Instructions",

      <span>
        For now there isn't any form to insert the move you want to see.<br/>
        But you can use a very confrotable search in the address bar :D.<br/>
        <br/>

        Actually the movie recommender works fine with movie already seen by someone,
        obviously you can see a movie that noboy alreasy seen, but don't expect to get a significant advice.
        <br/><br/>

        So, choose a User and see a movie as him: <br/>
        <code>/see/userID/movieID</code><br/>

        or insert a new User id and start rate movies:<br/>
        <code>/see/newUserID/newMovieID/rate</code>
        <br/>
        <b>rate is a decimal value from 0.0 to 5.0</b>
      </span>

      <h4>Users</h4>
      <ul>
        { userNodes }
      </ul>

      <h4>Movies</h4>
      <ul>
        { movieNodes }
      </ul>
    )
  }

  get("/see/:user/:movie") {

    if(collabModel == null)
      initSpark()

    val user = params.getOrElse("user", "-1").toInt
    val movie = params.getOrElse("movie", "-1").toInt

    val rate = collabModel.predictRating(user, movie)

    displayPage("See a Movie",
      <span>
        User { user } see movie { movie }...<br/>
          ...and I suppose he rate it { rate }/5.0
      </span>
    )
  }

  get("/see/:user/:movie/:rate") {

    if(collabModel == null)
      initSpark()

    val user = params.getOrElse("user", "-1").toInt
    val movie = params.getOrElse("movie", "-1").toInt
    val rate = params.getOrElse("rate", "0.0")

    val predict = collabModel.predictRating(user, movie)

    displayPage("See a Movie",
      <span>
        User { user } see movie { movie } and rate it { rate }.
        The recommender supposed a rate of { predict }
      </span>
    )
  }
}
