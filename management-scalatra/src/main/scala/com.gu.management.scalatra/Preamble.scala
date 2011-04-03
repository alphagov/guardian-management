package com.gu.management.scalatra

import com.gu.management.manifest.{ApplicationFileProvider, Manifest => WebAppManifest}
import com.gu.management.switching.SwitchableState
import javax.servlet.ServletContext

object Preamble {
  implicit def webappManifest2getReloadedManifestInformation(manifest: WebAppManifest) = new {
    def getReloadedManifestInformation = {
      manifest.reload
      manifest.getManifestInformation
    }
  }
}

object ManifestFactory {
  def apply(fileProvider: ApplicationFileProvider) = new WebAppManifest(fileProvider)

  def apply(fileProvider: ApplicationFileProvider, manifestFilePath: String) = {
    val m = new WebAppManifest(fileProvider)
    m setManifestFilePath manifestFilePath
    m
  }
}

object ApplicationFileProviderFactory {
  def apply(servletContext: ServletContext) = {
    val provider = new ApplicationFileProvider
    provider setServletContext servletContext
    provider
  }
}

object SwitchableStateFactory {
  def apply(wordsForUrl: String, description: String, initiallySwitchedOn: Boolean) = {
    val switchable = new SwitchableState {
      def getDescription: String = description
      def getWordForUrl: String = wordsForUrl
    }
    if (initiallySwitchedOn) switchable.switchOn else switchable.switchOff
    switchable
  }
}