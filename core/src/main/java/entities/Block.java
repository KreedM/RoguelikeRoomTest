package entities;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Block extends Entity {	
	
	public Block(float x, float y, int width, int height, World world) {
		super(x, y, width, height);
		createBody(world);
	}

	public void createBody(World world) {
		BodyDef tileDef = new BodyDef();
		
		tileDef.type = BodyType.StaticBody;
		
		tileDef.position.set(getX() + getWidth() / 2, getY() + getHeight() / 2);
		
		Body tile = world.createBody(tileDef);
		
		tile.setUserData(this);
		
		PolygonShape tileShape = new PolygonShape();
		tileShape.setAsBox(getWidth() / 2, getWidth() / 2);
		
		Fixture tileFixture = tile.createFixture(tileShape, 0);
		
		Filter tileFilter = new Filter();
		tileFilter.categoryBits = 2;
		tileFixture.setFilterData(tileFilter);
		
		tileShape.dispose();
		
		setBody(tile);
	}

	public void draw(Batch batch) {}

	public void act(float delta) {}
	
	public void processCollision(Entity entity) {}

	public void dispose() {}
}
