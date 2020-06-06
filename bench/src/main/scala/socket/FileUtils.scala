package bench
import com.github.ghik.silencer.silent

@silent("discarded non-Unit value")
object FileUtils {
  import java.nio.file.{ Files, Paths }

  val root  = "/tmp"
  val bench = "/tmp/bench/"

  def newDir(dir: String): Unit = {
    val dest = root + "/" + dir
    val path = Paths.get(dest)

    if (!Files.exists(path))
      Files.createDirectory(path)
  }

  def newFile(file: String): Unit = {
    val dest = bench + file
    val path = Paths.get(dest)

    if (!Files.exists(path))
      Files.createFile(path)

  }

  def wrFile(file: String, data: String): Unit = {
    val dest = bench + file
    val path = Paths.get(dest)

    if (Files.exists(path))
      Files.writeString(path, data)
  }

  def rdFile(file: String): Option[String] = {
    val dest = bench + file
    val path = Paths.get(dest)

    Files.exists(path) match {
      case true  => Some(Files.readString(path))
      case false => None
    }
  }

  def delFile(file: String): Unit = {
    val dest = bench + file
    val path = Paths.get(dest)
    Files.deleteIfExists(path)
  }
}
