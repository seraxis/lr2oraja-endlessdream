package bms.player.beatoraja;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import de.damios.guacamole.gdx.graphics.ShaderCompatibilityHelper;
import de.damios.guacamole.gdx.graphics.ShaderProgramFactory;

/**
 * Hack for macos<br>
 * See <a href="https://github.com/libgdx/libgdx/issues/6897">libgdx #6897</a>
 */
public class SpriteBatchHelper {
    public static ShaderProgram createSpriteBatchShader() {
        // @formatter:off
        String vertexShader = "#version 150\n" //
                + "in vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "in vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "in vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "uniform mat4 u_projTrans;\n" //
                + "out vec4 v_color;\n" //
                + "out vec2 v_texCoords;\n" //
                + "\n" //
                + "void main()\n" //
                + "{\n" //
                + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "   v_color.a = v_color.a * (255.0/254.0);\n" //
                + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE
                + "0;\n" //
                + "   gl_Position =  u_projTrans * "
                + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "}\n";
        String fragmentShader = "#version 150\n" //
                + "#ifdef GL_ES\n" //
                + "#define LOWP lowp\n" //
                + "precision mediump float;\n" //
                + "#else\n" //
                + "#define LOWP \n" //
                + "#endif\n" //
                + "in LOWP vec4 v_color;\n" //
                + "in vec2 v_texCoords;\n" //
                + "uniform sampler2D u_texture;\n" //
                + "out vec4 fragColor;\n" + "void main()\n"//
                + "{\n" //
                + "  fragColor = v_color * texture(u_texture, v_texCoords);\n" //
                + "}";
        // @formatter:on
        return ShaderProgramFactory.fromString(vertexShader, fragmentShader,
                true, true);
    }

    public static SpriteBatch createSpriteBatch() {
        return new SpriteBatch(1000,
                ShaderCompatibilityHelper.mustUse32CShader()
                        ? createSpriteBatchShader()
                        : null);
    }
}
