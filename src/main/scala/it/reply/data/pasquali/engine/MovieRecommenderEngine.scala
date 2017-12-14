package it.reply.data.pasquali.engine

import org.apache.spark.SparkContext
import org.apache.spark.mllib.recommendation.{MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

case class MovieRecommenderEngine() {

  var spark : SparkSession = null
  var sc : SparkContext = null

  var model : MatrixFactorizationModel = null

  def init(appName : String, master : String) :  MovieRecommenderEngine = {

    spark = SparkSession.builder().master(master).appName(appName).getOrCreate()
    sc = spark.sparkContext
    this
  }

  def loadCollaborativeModel(dirPath : String) : MovieRecommenderEngine = {

    model = MatrixFactorizationModel.load(sc, dirPath)
    this
  }

  def predictRating(ratings : RDD[(Int, Int)]) : RDD[((Int, Int), Double)] = {

    model.predict(ratings).map { case Rating(user, product, rate) =>
      ((user, product), rate)
    }
  }

  def predictRating(userID : Int, movieID : Int) : Double = {

    val rdd = sc.parallelize(Seq( (userID, movieID)))

    model.predict(rdd).map { case Rating(user, product, rate) =>
      ((user, product), rate)
    }.filter(el => el._1._1 == userID && el._1._2 == movieID).collect()(0)._2
  }

  def closeSession() : Unit = {

    if(spark != null)
      spark.stop()

    spark = null
    sc = null
    model = null
  }

}
