package it.reply.data.pasquali.controller

import java.io.File

import com.typesafe.config.ConfigFactory
import it.reply.data.pasquali.engine.MovieRecommenderEngine
import it.reply.data.pasquali.view.Template
import org.scalatra.scalate.ScalateSupport
import org.scalatra.{FlashMapSupport, ScalatraServlet}
import org.slf4j.LoggerFactory
import org.zeroturnaround.zip.ZipUtil

import scala.xml.Node

class Controller extends ScalatraServlet with FlashMapSupport with ScalateSupport{

  val CONF_FILE_DEFAULT = "/opt/conf/RealTimeML.conf"

  private def displayPage(title:String, content:Seq[Node]) =
    Template.page(title, content, url(_))

  var collabModel : MovieRecommenderEngine = null

  var MODEL_ARCHIVE_PATH : String = ""
  var MODEL_PATH : String = ""
  var MODEL_NAME : String = ""
  var SPARK_APPNAME : String = ""
  var SPARK_MASTER : String = ""

  var movieIDs : Array[Int] = Array()
  var userIDs : Array[Int] = Array()

  var userNodes : Array[Node] = Array()
  var movieNodes : Array[Node] = Array()

  val logger = LoggerFactory.getLogger(getClass)

  def initSpark(confFile : String) : Unit = {

    val cf = new File(confFile)
    val config = ConfigFactory.parseFile(cf)

    logger.info("------> Retrieve Conf variables <------")
    MODEL_ARCHIVE_PATH = config.getString("rtml.model.archive_path")
    MODEL_PATH = config.getString("rtml.model.path")
    MODEL_NAME = config.getString("rtml.model.name")
    SPARK_APPNAME = config.getString("rtml.spark.app_name")
    SPARK_MASTER = config.getString("rtml.spark.master")

    logger.info("------> ENV")
    logger.info("------>")
    logger.info(s"------> MODEL_ZIP_PATH = ${MODEL_ARCHIVE_PATH}")
    logger.info(s"------> MODEL_PATH = ${MODEL_PATH}")
    logger.info(s"------> MODEL_NAME = ${MODEL_NAME}")
    logger.info(s"------> SPARK_APP_NAME = ${SPARK_APPNAME}")
    logger.info(s"------> SPARK_MASTER = ${SPARK_MASTER}")

    logger.info("------> Initialize Spark")

    collabModel = MovieRecommenderEngine()
      .init(SPARK_APPNAME, SPARK_MASTER)

    logger.info("------> Spark Initialized")
    logger.info("------> Unzip ML Model")
    logger.info(s"------> FULL_MODEL_PATH = ${MODEL_PATH}${MODEL_NAME}")

    ZipUtil.unpack(new File(MODEL_ARCHIVE_PATH), new File(s"${MODEL_PATH}${MODEL_NAME}"))

    logger.info("------> Load ML Model")

    collabModel.loadCollaborativeModel(s"${MODEL_PATH}${MODEL_NAME}")

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
      initSpark(CONF_FILE_DEFAULT)

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
      initSpark(CONF_FILE_DEFAULT)

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
      initSpark(CONF_FILE_DEFAULT)

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
