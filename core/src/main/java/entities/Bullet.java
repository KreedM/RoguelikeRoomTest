package entities;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

public class Bullet extends Entity implements Poolable {
	//private World world;
	private Texture placeholder;
	private Pool<Bullet> bulletPool;
	private ArrayList<Bullet> bullets;
	private TextureAtlas bulletsAtlas;
	private BodyDef bulletDef;
	private Filter bulletFilter;
	//private TextureRegion bulletImage; //This and type are used in conjunction with texture atlas
	
	private boolean removed;
	
	public Bullet(Pool<Bullet> bulletPool, ArrayList<Bullet> bullets, TextureAtlas bulletAtlas) {
		this.bulletPool = bulletPool;
		this.bullets = bullets;
		this.bulletsAtlas = bulletAtlas;
		placeholder = new Texture("particles/bullet/bullet.png");
		
		bulletDef = new BodyDef();
		bulletDef.type = BodyType.DynamicBody;
		bulletDef.bullet = true;
		bulletFilter = new Filter();
	}
	
	public void init(float x, float y, float velocity, float direction, String type, World world) {
		setBounds(x - 0.25f, y - 0.25f, 0.5f, 0.5f); //Placeholder, values decided later based on type
		
		bulletFilter.categoryBits = 2;
		bulletFilter.maskBits = 2;
		
		createBody(world, velocity, direction);
	
		removed = false;
		
		bullets.add(this);
	}

	public void processCollision(Entity entity) {
		if(removed)
			return;

		bullets.remove(this);
		bulletPool.free(this);
		reset();
	}

	public void createBody(World world, float velocity, float direction) {
		bulletDef.position.set(getX() + getWidth() / 2, getY() + getHeight() / 2);
		
		Body bullet = world.createBody(bulletDef);
		bullet.setUserData(this);
		/*
		CircleShape bulletCircle = new CircleShape();
		bulletCircle.setRadius(getWidth() / 2);

		Fixture bulletFixture = bullet.createFixture(bulletCircle, 0);
		*/
		
		PolygonShape bulletBox = new PolygonShape();
		bulletBox.setAsBox(getWidth() / 2, getHeight() / 2);
		
		Fixture bulletFixture = bullet.createFixture(bulletBox, 0);
	
		bulletFixture.setFilterData(bulletFilter);
		
		//bulletCircle.dispose();
		bulletBox.dispose();
		
		bullet.setLinearVelocity(new Vector2(velocity, 0).rotate(direction));
		
		setBody(bullet);
	}
	
	public void createBody(World world) {}

	public void draw(Batch batch) {
		batch.draw(placeholder, getX(), getY(), getWidth(), getHeight()); //placeholder, usually method with rotation for textureregion
	}

	public void act(float delta) {
		Vector2 position = getBody().getPosition();
		
		setPosition(position.x - getWidth() / 2, position.y - getHeight() / 2);
	}
	
	public void dispose() {}

	public void reset() {
		removed = true;
	} //Maybe something will be added later...
}
