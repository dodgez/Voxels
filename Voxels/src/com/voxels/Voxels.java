

package com.voxels;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_NICEST;
import static org.lwjgl.opengl.GL11.GL_PERSPECTIVE_CORRECTION_HINT;
import static org.lwjgl.opengl.GL11.GL_POLYGON_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glHint;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import com.voxels.Block.BlockType;
import com.voxels.graphics.GraphicsRoutines;

public class Voxels {
	private boolean[] keys = new boolean[65536];
	private Chunk spawn = null;
	private float zPosition = -100;
	private float yPosition = 0;
	private float xPosition = 0;
	private long window = 0;
	
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	public static final int speed = 1;
	
	public Voxels() {
		
	}
	
	public void run() {
		try {
			// Initialize OpenGL
			initOpenGL();
			
			// Initialize game
			init();
			
			// Start the game loop
			loop();
			
			// Free the callback routines for the window
			glfwFreeCallbacks(window);
			
			// Destroy the window
			glfwDestroyWindow(window);
		} catch (Exception e) {
			// Print out the message
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		} finally {
			// Terminate GLFW
			glfwTerminate();
			
			// Free the error callback
			glfwSetErrorCallback(null).free();
		}
	}
	
	public void initOpenGL() throws Exception {
		// Set System.err as the output for error messages
		GLFWErrorCallback.createPrint(System.err).set();
		
		// Initialize GLFW. (Most GLFW require this)
		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}
		
		// Set the window hints to default
		glfwDefaultWindowHints();
		
		// Make the window hidden on creation
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		
		// Make the window static size
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		
		// Create the window
		window = glfwCreateWindow(WIDTH, HEIGHT, "Voxels", NULL, NULL);
		
		// Make sure the window was created
		if (window == NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}
		
		// Set handleKey as the function to handle key presses
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			handleKey(window, key, scancode, action, mods);
		});
		
		// Get the resolution of the primary monitor
		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		
		// Position the window in the center of the primary monitor
		glfwSetWindowPos(window, (vidmode.width() - WIDTH) / 2, (vidmode.height() - HEIGHT) / 2);
		
		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		
		// Enable v-sync
		glfwSwapInterval(1);
		
		// Make the window visible
		glfwShowWindow(window);
		
		// Create new capabilities for the new context
		GL.createCapabilities();
		
		// Change to projection matrix mode
		glMatrixMode(GL_PROJECTION);
		
		// Load the identity matrix
		glLoadIdentity();
		
		// Set up the projection matrix
		GraphicsRoutines.gluPerspective(90f, (float)WIDTH/HEIGHT, 1f, 10000f);
		
		// Change to model-view matrix mode
		glMatrixMode(GL_MODELVIEW);
		
		// Load the identity matrix
		glLoadIdentity();
		
		// Enable depth testing
		glEnable(GL_DEPTH_TEST);
		
		// Accept fragment if depth is less than or equal to another
		glDepthFunc(GL_LEQUAL);
		
		// Use the highest quality option for implementation of perspective correction
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		
		// Load the identity matrix
		glLoadIdentity();
		
		glEnable(GL_POLYGON_SMOOTH);
		
		// Change how polygons are rasterized
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
	}
	
	public void init() throws Exception {
		// Load all textures
		GraphicsRoutines.loadTextures();
		
		// Create a test (spawn) Chunk
		spawn = new Chunk();
		
		// Loop through the Blocks in the Chunk and create a test biome
		for (int x = 0; x < Chunk.CHUNK_SIZE; ++x) {
			for (int y = 0; y < Chunk.CHUNK_SIZE; ++y) {
				for (int z = 0; z < Chunk.CHUNK_SIZE; ++z) {
					if (x == 0) {
						// Spawn Sand to left
						spawn.setBlock(x, y, z, new Block(spawn, null, BlockType.Sand));
					} else if (y == 0) {
						// Spawn Grass on top
						spawn.setBlock(x, y, z, new Block(spawn, null, BlockType.Grass));
					} else {
						// Spawn Dirt below
						spawn.setBlock(x, y, z, new Block(spawn, null, BlockType.Dirt));
					}
				}
			}
		}
		
		// Update the Chunk (update display list)
		spawn.update();
	}
	
	public void loop() {
		// Enable 2d textures
		glEnable(GL_TEXTURE_2D);
		
		// Set the clear color
		glClearColor(0.0f, 1.0f, 1.0f, 0.0f);
		
		// Do a render loop until the window is to be closed
		while (!glfwWindowShouldClose(window)) {
			// Render the scene
			render();
			
			// Check for events and act on them
			update();
		}
	}
	
	public void render() {
		// Clear the frame buffer
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		// Set the viewport and load the identity matrix
		glViewport(0, 0, 800, 600);
		glLoadIdentity();
		
		// Move to the Chunk's position
		glTranslatef(xPosition, yPosition, zPosition);
		
		// Render the spawn Chunk
		spawn.render();
		
		// Swap the buffers
		glfwSwapBuffers(window);
	}
	
	public void update() {
		// Poll for any window events
		glfwPollEvents();

		// Move the cube based on WASD
		if (keys[GLFW_KEY_W]) {
			zPosition += speed;
		}
		
		if (keys[GLFW_KEY_S]) {
			zPosition -= speed;
		}
		
		if (keys[GLFW_KEY_A]) {
			xPosition += speed;
		}
		
		if (keys[GLFW_KEY_D]) {
			xPosition -= speed;
		}
		
		if (keys[GLFW_KEY_SPACE]) {
			yPosition -= speed;
		}
		
		if (keys[GLFW_KEY_LEFT_SHIFT]) {
			yPosition += speed;
		}
	}
	
	public void handleKey(long window, int key, int scancode, int action, int mods) {
		// If the user wishes to exit, signal GLFW to do so
		if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
			glfwSetWindowShouldClose(window, true);
		}
		
		// Set the corresponding entry in keys to the changed key state
		keys[key] = action != GLFW_RELEASE;
	}
	
	public static void main(String[] args) {
		// Start the game
		new Voxels().run();
	}
}
