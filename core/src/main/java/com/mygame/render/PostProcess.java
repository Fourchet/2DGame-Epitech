package com.mygame.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

// whole screen filter: the game draws into a texture, then it goes to the screen through post.frag
public class PostProcess {
    private final ShaderProgram shader;
    private final SpriteBatch batch = new SpriteBatch(); // its own batch so the game's camera isn't touched
    private FrameBuffer fbo;
    private TextureRegion frame;
    private float time; // for the grain

    public PostProcess() {
        ShaderProgram.pedantic = false; // unused uniforms don't crash
        shader = new ShaderProgram(Gdx.files.internal("shaders/post.vert"), Gdx.files.internal("shaders/post.frag"));
        if (!shader.isCompiled()) throw new GdxRuntimeException("post.frag: " + shader.getLog()); // tells you the shader line that's wrong
        batch.setShader(shader);
        batch.disableBlending(); // the frame is opaque anyway
        resize();
    }

    // call from GameScreen.resize, the texture has to match the window
    public void resize() {
        int w = Gdx.graphics.getBackBufferWidth();
        int h = Gdx.graphics.getBackBufferHeight();
        if (w <= 0 || h <= 0) return; // window minimized
        if (fbo != null) fbo.dispose();
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
        fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest); // stays crisp
        frame = new TextureRegion(fbo.getColorBufferTexture());
        frame.flip(false, true); // framebuffers come out upside down
        batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
    }

    // everything drawn after this goes into the texture instead of the screen
    public void begin() {
        fbo.begin();
    }

    // draws the texture on screen with the effect
    public void end(float delta) {
        fbo.end();
        time = (time + delta) % 100f; // kept small so the grain doesn't lose precision
        Gdx.gl.glViewport(0, 0, fbo.getWidth(), fbo.getHeight());
        batch.begin();
        shader.setUniformf("u_time", time);
        batch.draw(frame, 0, 0, fbo.getWidth(), fbo.getHeight());
        batch.end();
    }

    public void dispose() {
        if (fbo != null) fbo.dispose();
        shader.dispose();
        batch.dispose();
    }
}