package me.manabreak.ld38;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

public class Level {

    protected final GameStage stage;
    protected final List<Body> staticBodies = new ArrayList<>();
    private final float playerStartAngle;
    private float playerStartY;
    private float playerStartX;
    private Physics physics;

    public Level(GameStage stage, String jsonFile) {
        this.stage = stage;
        this.physics = stage.getPhysics();

        FileHandle f = Gdx.files.internal("levels/0001.json");
        JsonReader reader = new JsonReader();
        JsonValue value = reader.parse(f);
        playerStartX = value.getFloat("start_x");
        playerStartY = value.getFloat("start_y");
        playerStartAngle = value.getFloat("start_angle");

        JsonValue statics = value.get("statics");
        for (int i = 0; i < statics.size; ++i) {
            JsonValue stat = statics.get(i);
            createStatic(stat);
        }
    }

    private void createStatic(JsonValue value) {
        Body body = null;
        String shape = value.getString("shape");
        float x = value.getFloat("x", 0f);
        float y = value.getFloat("y", 0f);
        float angle = value.getFloat("angle", 0f);
        if ("circle".equals(shape)) {
            float radius = value.getFloat("radius");
            body = physics.createSphere(radius, BodyDef.BodyType.StaticBody);
        } else if ("rect".equals(shape)) {
            float width = value.getFloat("width");
            float height = value.getFloat("height");
            body = physics.createBox(width, height, BodyDef.BodyType.StaticBody);
        }

        if (body != null) {
            physics.setBodyPosition(body, x, y);
            body.setTransform(body.getPosition(), angle * MathUtils.degRad);
        } else {
            System.out.println("Couldn't parse static from value: " + value.toString());
        }
    }

    public float getPlayerStartY() {
        return playerStartY;
    }

    public float getPlayerStartX() {
        return playerStartX;
    }
}
