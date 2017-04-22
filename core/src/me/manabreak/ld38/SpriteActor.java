package me.manabreak.ld38;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class SpriteActor extends Actor {

    public Sprite sprite;

    public SpriteActor(Sprite sprite) {
        super();
        this.sprite = sprite;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        sprite.draw(batch);
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        sprite.setPosition(x, y);
    }

    @Override
    public void setScaleX(float scaleX) {
        super.setScaleX(scaleX);
        sprite.setScale(scaleX, sprite.getScaleY());
    }

    @Override
    public void setScaleY(float scaleY) {
        super.setScaleY(scaleY);
        sprite.setScale(sprite.getScaleX(), scaleY);
    }

    @Override
    public void setScale(float scaleXY) {
        super.setScale(scaleXY);
        sprite.setScale(scaleXY);
    }

    @Override
    public void setScale(float scaleX, float scaleY) {
        super.setScale(scaleX, scaleY);
        sprite.setScale(scaleX, scaleY);
    }

    @Override
    public void setX(float x) {
        super.setX(x);
        sprite.setX(x);
    }

    @Override
    public void setY(float y) {
        super.setY(y);
        sprite.setY(y);
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        sprite.setSize(width, height);
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        sprite.setSize(width, sprite.getHeight());
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);
        sprite.setSize(sprite.getWidth(), height);
    }

    @Override
    public void setRotation(float degrees) {
        super.setRotation(degrees);
        sprite.setRotation(degrees);
    }

    public void setOriginCenter() {
        sprite.setOriginCenter();
        super.setOrigin(sprite.getOriginX(), sprite.getOriginY());
    }

    @Override
    public void setOriginY(float originY) {
        super.setOriginY(originY);
        sprite.setOrigin(sprite.getOriginX(), originY);
    }

    @Override
    public void setOrigin(float originX, float originY) {
        super.setOrigin(originX, originY);
        sprite.setOrigin(0f, 0f);
    }
}
