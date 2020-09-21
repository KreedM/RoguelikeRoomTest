package entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;

public class Gun extends Entity implements Interactable, Item {
	
	private Texture gun;
	private World world;
	
	private boolean claimed;

	public Gun(float x, float y, float width, float height, World world) {
		super(x, y, width, height);
		
		this.world = world;
		
		gun = new Texture("entities/gun/gun.png");
		
		createBody(world);
	}

	public void interact(Player player) {
		player.addItem(this);
		world.destroyBody(getBody());
		claimed = true;
	}

	public void createBody(World world) { 
		BodyDef gunDef = new BodyDef();
		gunDef.type = BodyType.StaticBody;
		gunDef.position.set(getX() + getWidth() / 2, getY() + getHeight() / 2);
		
		Body gun = world.createBody(gunDef);
		gun.setUserData(this);
		
		PolygonShape gunBox = new PolygonShape();
		gunBox.setAsBox(getWidth() / 2, getHeight() / 2);

		Fixture gunFixture = gun.createFixture(gunBox, 0);
		Filter gunFilter = new Filter();
		gunFilter.maskBits = 1;
		gunFixture.setFilterData(gunFilter);
		
		gunBox.dispose();
		
		setBody(gun);
	}

	public void draw(Batch batch) {
		if (!claimed) //Kind of an efficient system, would do better if it was removed from Entity array somehow
			batch.draw(gun, getX(), getY(), getWidth(), getHeight());
	}

	public void act(float delta) {} //Updates animation time if items have idle pickup animations
	
	public void use(Player player) {
		
	}
	
	public void drawPortrait(Batch batch, float x, float y) { //May add size args
		batch.draw(gun, x, y, 1, 1);
	}

	public void dispose() {
		gun.dispose();
	}
	
	public void processCollision() {}
}
