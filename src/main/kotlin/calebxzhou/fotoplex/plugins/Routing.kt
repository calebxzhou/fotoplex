package calebxzhou.fotoplex.plugins

import calebxzhou.fotoplex.IMAGE_DIR
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.coobird.thumbnailator.Thumbnails
import java.io.File
import java.util.Base64

enum class FileType { DIR, IMAGE }
@Serializable
data class FileInfo(val type: FileType, val data: String)
data class RequestContext(
    val responseFileInfoList: MutableList<FileInfo>,
    val requestDir: File,
    val ktorCall : ApplicationCall
)
const val THUMBNAIL_FILE_NAME_PREFIX = "__thumbnail_"
fun Application.configureRouting() {
    routing {
        get("/{...}") {
            val uri = call.request.uri
            val requestDir = File(IMAGE_DIR, uri)
            val fileInfoData = arrayListOf<FileInfo>()
            val ctx = RequestContext(fileInfoData,requestDir,call)
            requestDir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    fileInfoData += FileInfo(FileType.DIR, file.name)
                } else {
                    handleFile(ctx,file)
                }
            }
            call.respond(Json.encodeToString(fileInfoData))
        }
    }
}

fun handleFile(ctx : RequestContext, file: File) {
    if (file.extension == "jpg" || file.extension == "JPG") {
            handleJpg(ctx,file)
    }
}

fun handleJpg(ctx: RequestContext, imageFile: File) {
    val imageB64 = readThumbnailB64(ctx,imageFile)
    ctx.responseFileInfoList += FileInfo(FileType.IMAGE,imageB64)
}

fun readThumbnailB64(ctx: RequestContext,imageFile: File) : String{
    val thumbnailFile = imageFile.name.replace(THUMBNAIL_FILE_NAME_PREFIX,"").let { fileName ->
        File(ctx.requestDir, "${THUMBNAIL_FILE_NAME_PREFIX}$fileName")
    }
    if (!thumbnailFile.exists()) {
        Thumbnails.of(imageFile).size(128,128).outputFormat("jpg").toFile(thumbnailFile)
    }
    val imageBytes = thumbnailFile.readBytes()
    val imageB64 = Base64.getEncoder().encodeToString(imageBytes)
        return imageB64

}