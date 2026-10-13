import com.android.build.gradle.internal.coverage.JacocoReportTask
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.api.file.ArchiveOperations
import javax.inject.Inject

interface CoverageServices {
  @get:Inject val archives: ArchiveOperations
}

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.serialization)
  jacoco
}

// AGP's built-in coverage tasks use JacocoReportTask, not Gradle's JacocoReport.
// Keep these report-only exclusions scoped to generated classes; application
// classes (including nested classes and companions) remain in coverage.
val generatedCoverageClasses = listOf(
  "**/R.class",
  "**/R\$*.class",
  "**/BuildConfig.class",
  "**/BR.class",
  "androidx/databinding/**",
  "**/databinding/**",
  "**/DataBinderMapperImpl*.class",
  "**/DataBindingTriggerClass.class",
  "**/generated/callback/**",
  "**/*\$\$serializer.class",
)

// AGP locks its report inputs, so filtered reports reuse them in separate tasks.
// This AGP task type is internal; check this integration when upgrading AGP.
listOf("UnitTest", "AndroidTest").forEach { testType ->
  tasks.register<JacocoReport>("jacocoDebug${testType}Report") {
    group = "verification"
    description = "Generates debug $testType coverage excluding generated classes."
    val androidReport = tasks.named<JacocoReportTask>("createDebug${testType}CoverageReport")
    dependsOn(androidReport)
    // Android tests expose per-device coverage directories; JaCoCo needs the
    // execution data files inside them. Unit tests expose .exec files directly.
    executionData.from(androidReport.map {
      it.coverageFiles.asFileTree.matching {
        include("**/*.ec", "**/*.exec")
      }
    })
    jacocoClasspath = files(androidReport.map { it.jacocoClasspath })
    sourceDirectories.from("src/main/java", "src/main/kotlin", "src/debug/java", "src/debug/kotlin")

    val archives = objects.newInstance<CoverageServices>().archives
    val projectLayout = layout
    val exclusions = generatedCoverageClasses
    classDirectories.from(androidReport.flatMap { it.classFileCollection.elements }.map { locations ->
      locations.map { location ->
        val classes = if (location.asFile.extension == "jar") {
          archives.zipTree(location.asFile)
        } else {
          projectLayout.files(location.asFile).asFileTree
        }
        classes.matching { exclude(exclusions) }
      }
    })
    reports {
      html.required = true
      xml.required = true
      csv.required = false
      html.outputLocation = layout.buildDirectory.dir("reports/jacoco/$name/html")
      xml.outputLocation = layout.buildDirectory.file("reports/jacoco/$name/report.xml")
    }
  }
}

tasks.register<JacocoReport>("jacocoDebugCombinedReport") {
  group = "verification"
  description = "Runs debug unit and connected Android tests and generates combined coverage."
  val unitReport = tasks.named<JacocoReport>("jacocoDebugUnitTestReport")
  val androidReport = tasks.named<JacocoReport>("jacocoDebugAndroidTestReport")
  dependsOn(unitReport, androidReport)

  executionData.from(unitReport.map { it.executionData }, androidReport.map { it.executionData })
  // Both suites exercise the same application classes; analyze them only once.
  classDirectories.from(unitReport.map { it.classDirectories })
  sourceDirectories.from(unitReport.map { it.sourceDirectories })
  jacocoClasspath = files(unitReport.map { it.jacocoClasspath })
  reports {
    html.required = true
    xml.required = true
    csv.required = false
    html.outputLocation = layout.buildDirectory.dir("reports/jacoco/$name/html")
    xml.outputLocation = layout.buildDirectory.file("reports/jacoco/$name/report.xml")
  }
}

android {
  namespace = "edu.ucsc.cse118.assignment2"
  compileSdk {
    version = release(37)
  }

  defaultConfig {
    applicationId = "edu.ucsc.cse118.assignment2"
    minSdk = 33
    targetSdk = 37
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    debug {
      enableUnitTestCoverage = true
      enableAndroidTestCoverage = true
    }
    release {
      optimization {
        enable = false
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  buildFeatures {
    dataBinding = true
    viewBinding = true
  }
}

dependencies {
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.androidx.navigation.fragment.ktx)
  implementation(libs.androidx.navigation.ui.ktx)
  implementation(libs.androidx.activity.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.core.ktx)
  implementation(libs.material)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation("com.google.protobuf:protobuf-java:4.36.2")
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.espresso.contrib)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.barista)
}
