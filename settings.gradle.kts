rootProject.name = "enigma"
include("common")
include("secretapp")
include("command")



val archivaHostId: String? by settings
val archivaPort: String? by settings

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        archivaPort?.let {
            archivaHostId?.let {
                maven {
                    isAllowInsecureProtocol = true
                    url = uri("http://$archivaHostId:$archivaPort/repository/snapshots")
                }
            }
        }
    }
}
