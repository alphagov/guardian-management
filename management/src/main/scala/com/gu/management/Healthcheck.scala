package com.gu.management

import javax.servlet.http.HttpServletRequest

class HealthcheckManagementPage(healthcheckSwitch: Switch = Healthcheck.switch) extends ManagementPage {
  val path = "/management/healthcheck"

  def get(req: HttpServletRequest) = {
    healthcheckSwitch opt {
      PlainTextResponse("OK")
    } getOrElse {
      ErrorResponse(503, "Service unavailable: healthcheck-enable switch is OFF")
    }
  }
}

object Healthcheck {
  val switch = new DefaultSwitch("healthcheck-enable", "Health check enable", initiallyOn = true)
}