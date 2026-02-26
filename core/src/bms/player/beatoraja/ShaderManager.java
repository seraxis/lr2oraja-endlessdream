package bms.player.beatoraja;

import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShaderManager {
	private static final Logger logger = LoggerFactory.getLogger(ShaderManager.class);

	private static HashMap<String, ShaderProgram> shaders = new HashMap();

	public static ShaderProgram getShader(String name) {
		if (!shaders.containsKey(name)) {
			FileHandle vertFile = loadShaderFile(name, ".vert");
			FileHandle fragFile = loadShaderFile(name, ".frag");
			ShaderProgram shader = new ShaderProgram(vertFile, fragFile);
			if(shader.isCompiled()) {
				shaders.put(name, shader);
				return shader;				
			} else {
				logger.error("Failed to compile shader, vert at {}, frag at {}", vertFile.path(), fragFile.path());
				logger.error("Shader compilation error: {}", shader.getLog());
			}
		}
		return shaders.get(name);
	}

	private static FileHandle loadShaderFile(String name, String suffix) {
		if (UIUtils.isMac || UIUtils.isLinux) {
			FileHandle unixVariant = Gdx.files.classpath("glsl/" + name + "_unix" + suffix);
			if (unixVariant.exists()) {
				return unixVariant;
			}
		}
		return Gdx.files.classpath("glsl/" + name + suffix);
	}
	
	public static void dispose() {
		for(Entry<String, ShaderProgram> e : shaders.entrySet()) {
			if(e.getValue() != null) {
				e.getValue().dispose();
			}
		}
	}
}
