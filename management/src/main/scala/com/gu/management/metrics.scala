package com.gu.management

import java.util.concurrent.atomic.AtomicLong
import java.util.Date

abstract class Metric() {
  val group: String
  val name: String

  def asJson: StatusMetric

  def definition: Definition = Definition(group, name)

  def reset() {}
}

object TimingMetric {
  val empty = new TimingMetric("application", "Empty", "Empty", "Empty", "Empty")
}

case class Definition(group: String,
                      name: String)

case class GaugeMetric(group: String,
                       name: String,
                       title: String,
                       description: String,
                       units: String = "level",
                       master: Option[Metric] = None) extends Metric {

  private val _count = new AtomicLong()

  override def reset() {
    _count.set(0)
  }

  def recordCount(count: Int) {
    _count.addAndGet(count)
  }

  def count = _count.get

  def asJson = StatusMetric(
    group = group,
    master = master map {
      _.definition
    },
    name = name,
    `type` = "gauge",
    title = title,
    description = description,
    value = Some(count.toString)
  )
}

case class CountMetric(group: String,
                       name: String,
                       title: String,
                       description: String,
                       units: String = "requests",
                       master: Option[Metric] = None) extends Metric {
  
  private val _count = new AtomicLong()

  def recordCount(count: Int) {
    _count.addAndGet(count)
  }

  override def reset() {
    _count.set(0)
  }

  def count = _count.get

  def asJson = StatusMetric(
    group = group,
    master = master map {
      _.definition
    },
    name = name,
    `type` = "counter",
    title = title,
    description = description,
    count = Some(count.toString)
  )
}

case class TimingMetric(group: String, 
                        name: String, title: String,
                        description: String,
                        units: String = "ms",
                        master: Option[Metric] = None) extends Metric {

  private val _totalTimeInMillis = new AtomicLong()
  private val _count = new AtomicLong()

  def recordTimeSpent(durationInMillis: Long) {
    _totalTimeInMillis.addAndGet(durationInMillis)
    _count.incrementAndGet
  }

  override def reset() {
    _count.set(0)
    _totalTimeInMillis.set(0)
  }

  def asJson = StatusMetric(
    group = group,
    master = master map {
      _.definition
    },
    name = name,
    `type` = "timer",
    title = title,
    description = description,
    count = Some(count.toString),
    totalTime = Some(totalTimeInMillis.toString)
  )

  def totalTimeInMillis = _totalTimeInMillis.get

  def count = _count.get

  def measure[T](block: => T) = {
    val s = new StopWatch
    val result = block
    recordTimeSpent(s.elapsed)
    result
  }
}

case class StatusMetric(group: String = "application",
                        master: Option[Definition] = None,
                        // name should be brief and underscored not camel case
                        name: String,
                        `type`: String,
                        // a short (<40 chars) title for this metric
                        title: String,
                        // an as-long-as-you-like description of what this metric means
                        // (used, e.g. on mouse over)
                        description: String,
                        // NB: these are deliberately strings - some json parsers have issues
                        // with big numbers, see https://dev.twitter.com/docs/twitter-ids-json-and-snowflake
                        value: Option[String] = None,
                        count: Option[String] = None,
                        totalTime: Option[String] = None,
                        units: Option[String] = None)

case class StatusResponseJson(application: String,
                              time: Long = new Date().getTime,
                              metrics: Seq[StatusMetric] = Nil)




















