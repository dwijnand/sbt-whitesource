package sbtwhitesource

import sbt._, Keys._

import org.whitesource.agent.client.ClientConstants

// TODO: Decide on GitHub org (change scmInfo)
// TODO: must have: Need to distinguish direct vs transitive dependencies
// TODO: git diff should be clean after running
// TODO: nice to have: parameter to fail the build
// TODO: Create an issue about Proxy Support
// TODO: Default wire tasks to package, or similar
object WhiteSourcePlugin extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger  = allRequirements

  object autoImport {
    val whitesourceCheckPolicies: TaskKey[Unit] =
      taskKey("Sends a check policies request of open source software usage information to WhiteSource " +
        "and then generates a report.")

    val whitesourceUpdate: TaskKey[Unit] =
      taskKey("Sends an update request of open source software usage information to WhiteSource " +
        "and then generates a report. See also `whitesourceCheckPoliciesBeforeUpdate`.")

    val whitesourceOrgToken: SettingKey[String] =
      settingKey("Uniquely identifies a WhiteSource organization.")

    val whitesourceServiceUrl: SettingKey[URI] =
      settingKey("Specifies the WhiteSource Service URL (or IP) to use, for on-premise installations.")

    val whitesourceCheckPoliciesBeforeUpdate: SettingKey[Boolean] =
      settingKey("If set to true a check policies request will be sent before any update request, " +
        "and if any new dependency doesn't comply with your organization's policies " +
        "the update request will not be sent.")

    val whitesourceForceCheckAllDependencies: SettingKey[Boolean] =
      settingKey("If set to true all dependencies, as opposed to only new dependencies, " +
        "will be checked against the policies. " +
        "Only used if `whitesourceCheckPoliciesBeforeUpdate` is set to true.")

    val whitesourceForceUpdate: SettingKey[Boolean] =
      settingKey("Forces the organization inventory to be updated regardless of policy violations.")

    val whitesourceProjectToken: SettingKey[String] =
      settingKey("Uniquely identifies a WhiteSource project.")

    val whitesourceProduct: SettingKey[String] =
      settingKey("The WhiteSource product name or token.")

    val whitesourceProductVersion: SettingKey[String] =
      settingKey("The WhiteSource product version, used to override the version in each project.")

    val whitesourceIncludes: SettingKey[Vector[String]] =
      settingKey("Only modules with an artifactId matching one of these patterns will be processed.")

    val whitesourceExcludes: SettingKey[Vector[String]] =
      settingKey("Modules with an artifactId matching any of these patterns will not be processed.")

    // TODO: Consider "skip in <something>"
    val whitesourceIgnore: SettingKey[Boolean] =
      settingKey("If set to true this module will be ignored. Overrides any include patterns.")

    val whitesourceIgnoreTestScopeDependencies: SettingKey[Boolean] =
      settingKey("If set to false test scope dependencies are included.")

    val whitesourceIgnoredScopes: SettingKey[Vector[String]] =
      settingKey("Defines the Maven scopes of direct dependencies to ignore.")

    val whitesourceFailOnError: SettingKey[Boolean] =
      settingKey("If set to true the build will fail if there are errors.")

    // TODO: Consider "skip in <something>"
    // TODO: Understand difference with whitesourceIgnore
    val whitesourceSkip: SettingKey[Boolean] =
      settingKey("Set to true to skip the execution.")

    val whitesourceReportAsJson: SettingKey[Boolean] =
      settingKey("If set to true the check policies report will be created in a text file in JSON format " +
        "instead of the regular HTML format report.")

    val whitesourceResolveInHouseDependencies: SettingKey[Boolean] =
      settingKey("Set to true to recursively resolve and send transitive dependencies to WhiteSource. " +
        "Should only be set to true if any internal (in-house) dependencies are used in the project " +
        "and In-House rules exist in your WhiteSource account.")

    val whitesourceAggregateModules: SettingKey[Boolean] =
      settingKey("If set to true all modules will be combined into a single WhiteSource project " +
        "with an aggregated dependency flat list (no hierarchy).")

    val whitesourceAggregateProjectName: SettingKey[String] =
      settingKey("The project name that will appear in WhiteSource. " +
        "If not explicitly set and no project token defined, defaults to the root project artifactId. " +
        "Will only apply if `whitesourceAggregateModules` is set to true.")

    val whitesourceAggregateProjectToken: SettingKey[String] =
      settingKey("Unique identifier of the White Source project to update, " +
        "overrides `whitesourceAggregateProjectName`. " +
        "If omitted, default to the root project artifactId. " +
        "Will only apply if `whitesourceAggregateModules` is set to true.")
    // TODO: Validate that defaulting to the root project artifactId is indeed the behaviour

    val whitesourceRequesterEmail: SettingKey[String] =
      settingKey("The provided email will be matched with an existing WhiteSource account. " +
        "Requests for new libraries will be created with the matched account as the requester.")

//    val whitesourceAutoDetectProxySettings: SettingKey[Boolean] =
//      settingKey("Indicates whether to try to detect proxy configuration in the underlying machine " +
//        "(e.g. in OS proxy settings, in JVM system properties etc.)")
  }
  import autoImport._

  override def globalSettings = Seq(
    whitesourceServiceUrl                  := uri(ClientConstants.DEFAULT_SERVICE_URL),
    whitesourceCheckPoliciesBeforeUpdate   := false,
    whitesourceForceCheckAllDependencies   := false,
    whitesourceForceUpdate                 := false,
    whitesourceIncludes                    := Vector.empty,
    whitesourceExcludes                    := Vector.empty,
    whitesourceIgnore                      := false,
    whitesourceIgnoreTestScopeDependencies := true,
    whitesourceIgnoredScopes               := Vector("test", "provided"),
    whitesourceFailOnError                 := false,
    whitesourceSkip                        := false,
    whitesourceReportAsJson                := false,
    whitesourceResolveInHouseDependencies  := false,
    whitesourceAggregateModules            := false,
    whitesourceAggregateProjectToken       := (moduleName in LocalRootProject).value,
    whitesourceAggregateProjectName        := whitesourceAggregateProjectToken.value
//  whitesourceAutoDetectProxySettings     := false
  )

  override def buildSettings = Seq(
  )

  override def projectSettings = Seq(
    whitesourceProjectToken   := moduleName.value, // TODO: Validate this is the right default
    whitesourceProduct        := moduleName.value,
    whitesourceProductVersion := version.value
  )
}