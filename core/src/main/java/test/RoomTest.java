package test;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.FitViewport;
import entities.Block;
import entities.Bullet;
import entities.Door;
import entities.Dummy;
import entities.Entity;
import entities.Gun;
import entities.Interactable;
import entities.Player;

public class RoomTest extends ApplicationAdapter {
	private static final int BOX2D_VELOCITY_ITERATIONS = 8, BOX2D_POSITION_ITERATIONS = 3;
	private static final float BOX2D_TIME_STEP = 1 / 60f;
	
	public FitViewport viewport;
	
	private SpriteBatch batch;
	
	private TiledMap testRoom;
	private OrthogonalTiledMapRenderer renderer;
	
	public World world;
	private ArrayList<Body> interacting;
	private ArrayList<Body> removing;
	private ArrayList<Contact> contacts;
	private InteractCallback callback;
	private Box2DDebugRenderer box2dDebugRenderer;
	private float box2DTime;
	
	private Texture cursor;
	private Vector3 cursorPos;
	
	private Player player;
	private Dummy dummy;
	private Door door;
	private Gun gun;
	
	public Pool<Bullet> bulletPool;
	public ArrayList<Bullet> bullets;
	
	public void create() {
		Gdx.input.setCursorCatched(true);
		
		viewport = new FitViewport(16, 9);
		
		batch = new SpriteBatch();
		
		Box2D.init();
		world = new World(new Vector2(0, 0), true);
		interacting = new ArrayList<Body>();
		removing = new ArrayList<Body>();
		callback = new InteractCallback(interacting);
		box2dDebugRenderer = new Box2DDebugRenderer();
		contacts = new ArrayList<Contact>();
		world.setContactListener(new ContactListener(contacts));
		
		testRoom = new TmxMapLoader().load("maps/testroom.tmx");
		renderer = new OrthogonalTiledMapRenderer(testRoom, 1 / 16f, batch);
		
		TiledMapTileLayer layer = (TiledMapTileLayer) testRoom.getLayers().get("Background");
		for (int i = 0; i < layer.getWidth(); i++) {
			for (int j = 0; j < layer.getHeight(); j++) {
				if (layer.getCell(i, j).getTile().getProperties().containsKey("blocked"))
					new Block(i, j, 1, 1, world);
			}
		}
		
		cursor = new Texture("cursor.png");
		cursorPos = new Vector3();
		
		bullets = new ArrayList<Bullet>();
		bulletPool = new Pool<Bullet>() {
		    protected Bullet newObject() {
		        return new Bullet(bulletPool, bullets, world, null); //Give bullet atlas at some point
		    }
		};
		
		player = new Player(1, 1, 2, 2, this);
		door = new Door(8, 4, 2, 2, world);
		dummy = new Dummy(17, 4, 2, 2, world);
		gun = new Gun(3, 3, 0.5f, 0.5f, this);
		
		Gdx.input.setInputProcessor(player);
	}
	
	public void render() {
		float time = Gdx.graphics.getDeltaTime();

		box2DTime += time;
		
		if(box2DTime > 1)
			box2DTime = 0;
		else {
			while (box2DTime >= BOX2D_TIME_STEP) {
				processActs(BOX2D_TIME_STEP);
	
				contacts.clear();
	
				world.step(BOX2D_TIME_STEP, BOX2D_VELOCITY_ITERATIONS, BOX2D_POSITION_ITERATIONS);
	
				processCollisions();
	
				processPositions();
	
				box2DTime -= BOX2D_TIME_STEP;
			}
		}
		
		cursorPos.x = Gdx.input.getX();
		cursorPos.y = Gdx.input.getY();
		viewport.getCamera().unproject(cursorPos);
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		viewport.getCamera().position.set(player.getX() + player.getWidth() / 2, player.getY() + player.getHeight() / 2, 0);
		viewport.getCamera().update();
		
		renderer.setView((OrthographicCamera) viewport.getCamera());
		renderer.render();
		
		batch.begin();
		dummy.draw(batch);
		gun.draw(batch);
		player.draw(batch);
		door.draw(batch);		
		
		for(Bullet bullet : bullets)
			bullet.draw(batch);
			
		batch.draw(cursor, cursorPos.x - 0.5f, cursorPos.y - 0.5f, 1, 1);
		batch.end();
		
		box2dDebugRenderer.render(world, viewport.getCamera().combined);
	}
	
	private void processActs(float time) {
		player.act(time);
		dummy.act(time);
		door.act(time);
		gun.act(time);
		
		for (Bullet bullet : bullets)
			bullet.act(time);
	}
	
	private void processPositions() {
		player.updatePosition();
	}
	
	public void processCollisions() {
		removing.clear();
		
		for (Contact contact : contacts) { //A vs B collisions MUST BE DEFINED for it to work
			Entity a = (Entity) contact.getFixtureA().getBody().getUserData(), b = (Entity) contact.getFixtureB().getBody().getUserData();
			
			if(a instanceof Bullet && b instanceof Bullet) {
				((Bullet) a).processCollision(b);
				if(!removing.contains(contact.getFixtureA().getBody()))
					removing.add(contact.getFixtureA().getBody());
				((Bullet) b).processCollision(a);
				if(!removing.contains(contact.getFixtureB().getBody()))
					removing.add(contact.getFixtureB().getBody());
			}
			else if (a instanceof Bullet) {
				((Bullet) a).processCollision(b);
				if(!removing.contains(contact.getFixtureA().getBody()))
					removing.add(contact.getFixtureA().getBody());
			}
			else if(b instanceof Bullet) {
				((Bullet) b).processCollision(a);
				if(!removing.contains(contact.getFixtureB().getBody()))
					removing.add(contact.getFixtureB().getBody());
			}			
		}
		
		for (Body body : removing)
			world.destroyBody(body);
		
		if (player.getInteracting()) {
			interacting.clear();

			world.QueryAABB(callback, player.getBody().getPosition().x - 2, player.getBody().getPosition().y - 2, player.getBody().getPosition().x + 2, player.getBody().getPosition().y + 2);
			
			float distance = Float.MAX_VALUE, dist2 = 0, x = player.getBody().getPosition().x, y = player.getBody().getPosition().y;
			Interactable interactor = null;
			
			for (Body body : interacting) {
				dist2 = Vector2.dst2(x, y, body.getPosition().x, body.getPosition().y);
				
				if (dist2 < distance) {
					distance = dist2;
					interactor = (Interactable) body.getUserData();
				}
			}
			
			if(interactor != null)
				interactor.interact(player);
				
			player.setInteracting(false);
		}
	}
	
	public void resize(int width, int height) {
		viewport.update(width, height);
	}
	
	
	public void dispose() {
		batch.dispose();
		testRoom.dispose();
		renderer.dispose();
		world.dispose();
		box2dDebugRenderer.dispose();
	}	
	
	public static Animation<TextureRegion> makeAnimation(Texture tex, float frameDuration, int rows, int columns, int cellWidth, int cellHeight) {
		TextureRegion[][] split = TextureRegion.split(tex, cellWidth, cellHeight);
		
		TextureRegion[] reel = new TextureRegion[rows * columns];
		
		int index = 0;
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				reel[index++] = split[i][j]; 
			}
		}
		
		return new Animation<TextureRegion>(frameDuration, reel);
	}
}