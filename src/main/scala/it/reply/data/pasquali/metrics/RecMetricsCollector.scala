package it.reply.data.pasquali.metrics

import it.reply.data.pasquali.metrics.model.{CounterMetric, Metric, TimerMetric}

import scala.collection.mutable

object RecMetricsCollector {

  val metrics : mutable.HashMap[String, Metric] =
    mutable.HashMap[String, Metric]()

  def addMetric(label : String, metric: Metric) = {
    metrics.put(label, metric)
  }

  def dropMetric(label : String) = {
    metrics.remove(label)
  }

  def getPrometheusMetrics() : String = {

    var body = ""

    for(el <- metrics.values)
    {
      body += el.getPrometheusMetrics()
      body += "\n"
    }
    body
  }

  def set(label : String, value : Double) = {
    metrics(label)._value = value
  }

  def startTimer(label : String) = {
    metrics(label).asInstanceOf[TimerMetric].startTimer()
  }

  def stopTimer(label : String) = {
    metrics(label).asInstanceOf[TimerMetric].stopTimer()
  }

  def inc(label : String) = {
    metrics(label).asInstanceOf[CounterMetric].inc()
  }

}
