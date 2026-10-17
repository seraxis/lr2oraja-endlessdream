package bms.player.beatoraja;

import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShaderManager {

	private static final Logger logger = LoggerFactory.getLogger(ShaderManager.class);
	private static HashMap<String, ShaderProgram> shaders = new HashMap();

	public static ShaderProgram getShader(String name) {
		if (!shaders.containsKey(name)) {
			ShaderProgram shader = new ShaderProgram(Gdx.files.classpath("glsl/" + name + ".vert"),
					Gdx.files.classpath("glsl/" + name + ".frag"));
			if(shader.isCompiled()) {
				shaders.put(name, shader);
				return shader;				
			} else {
				logger.error(shader.getLog());
				return null;
			}
		}
		return shaders.get(name);
	}
	
	public static void dispose() {
		for(Entry<String, ShaderProgram> e : shaders.entrySet()) {
			if(e.getValue() != null) {
				e.getValue().dispose();
			}
		}
	}
}
