package com.gu.management.logback

import com.gu.management.{Postable, HtmlManagementPage}
import javax.servlet.http.HttpServletRequest
import collection.JavaConverters._
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.{Logger, Level, LoggerContext}
import xml.{NodeSeq, Text}

class LogbackLevelPage extends HtmlManagementPage with Postable {
  val myLogger = LoggerFactory.getLogger(getClass)

  val path = "/management/logback"

  private val levels = List(
    "ERROR" -> Level.ERROR, "WARN" -> Level.WARN, "INFO" -> Level.INFO,
    "DEBUG" -> Level.DEBUG, "TRACE" -> Level.TRACE, "OFF" ->Level.OFF,
    "(default)" -> null)

  // the above is a list because I care about order in dropdowns
  private val levelMap = levels.toMap



  def body(r: HttpServletRequest) = LoggerFactory.getILoggerFactory match {
    case logback: LoggerContext =>
      <form method="POST">
        <input type="submit" value="update" />
        <table border="1">
          <tr>
            <th>Level</th><th>Effective Level</th><th></th>
          </tr>
          {
            for (logger <- logback.getLoggerList.asScala) yield {
              <tr>
                <td>{logger.getName}</td>
                <td>{logger.getEffectiveLevel}</td>
                <td>
                  {dropDown(logger)}
                  <input type="submit" value="update all" />
                </td>
              </tr>
            }
          }
        </table>
        <input type="submit" value="update" />
      </form>
    case other =>
      <p>Expected a logback LoggerContext but found a {other.getClass}; are you sure you're using logback?</p>
  }

  private def dropDown(logger: Logger) =
    <select name={logger.getName}>
      {
        for ( (value, level) <- levels) yield {
          <option value={value}
                  selected={ if (logger.getLevel == level) Some(Text("selected")) else None }>{value}</option>
        }
      }
    </select>

  def title = "Logback Configuration"

  def post(r: HttpServletRequest) = {
    myLogger.info("Processing POST...")

    val logback = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    for {
      logger <- logback.getLoggerList.asScala
      param <- Option(r.getParameter(logger.getName))
      level <- levelMap.get(param)
    } {
      myLogger.info("updating %s -> %s" format (logger.getName, level))
      logger.setLevel(level)
    }
  }


}
