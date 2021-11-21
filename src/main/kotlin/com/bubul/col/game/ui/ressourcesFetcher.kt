import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import java.nio.file.Paths

fun getGameRessource(resPath : String) : FileHandle?
{
    return Gdx.files.absolute(Paths.get("", resPath).toAbsolutePath().toString())
}