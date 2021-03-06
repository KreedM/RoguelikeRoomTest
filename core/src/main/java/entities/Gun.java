package entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Pool;
import test.RoomTest;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;

public class Gun extends Entity implements Interactable, Item {
	private static float COOLDOWN = 0.125f;
	
	private Texture gun;
	private World world;
	
	private Pool<Bullet> bulletPool;
	
	private boolean claimed;
	
	private float coolDownTime;

	public Gun(float x, float y, float width, float height, RoomTest test) {
		super(x, y, width, height);
		
		world = test.world;
		
		bulletPool = test.bulletPool;
		
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
		
		CircleShape gunCircle = new CircleShape();
		gunCircle.setRadius(getWidth() / 2);

		Fixture gunFixture = gun.createFixture(gunCircle, 0);
		
		Filter gunFilter = new Filter();
		gunFilter.categoryBits = 2;
		gunFilter.maskBits = 0;
		gunFixture.setFilterData(gunFilter);
		
		gunCircle.dispose();
		
		setBody(gun);
	}

	public void draw(Batch batch) {
		if (!claimed) //Kind of an efficient system, would do better if it was removed from Entity array somehow
			batch.draw(gun, getX(), getY(), getWidth(), getHeight());
	}

	public void act(float delta) {
		coolDownTime += delta;
	} 
	
	public void use(Player player) {
		if (coolDownTime >= COOLDOWN) {
			Bullet newBullet = bulletPool.obtain();
			newBullet.init(player.getX() + player.getWidth() / 2, player.getY() + player.getHeight() / 2, 6, player.getCursorDirection(), null, world); //placeholder
			
			coolDownTime = 0;
		}
	}
	
	public void drawPortrait(Batch batch, float x, float y) { //May add size args
		batch.draw(gun, x, y, 1, 1);
	}

	public void dispose() {
		gun.dispose();
	}
	
	public void processCollision(Entity entity) {}
}
