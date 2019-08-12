package space.crab8012.testgame;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {
    // Window Handle
    private long window;

    //Store the connected joysticks
    private ArrayList<Integer> joysticks = new ArrayList();
    private ArrayList<Float> joystickAxes = new ArrayList(5);
    private String title = "";

    public void run(){
        System.out.println("Running LWJGL " + Version.getVersion());

        init();
        loop();
        exit();
    }

    private void init(){
        //Setup an error callback
        //Prints to System.err
        GLFWErrorCallback.createPrint(System.err).set();

        //Initialize GLFW

        if(!glfwInit()){
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        //Configure GLFW
        glfwDefaultWindowHints(); //current window hints are already default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); //Window stays hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); //The window will be resizable

        //Create the window
        window = glfwCreateWindow(1280, 720, "Test Program", NULL, NULL);
        if(window == NULL){
            throw new RuntimeException("Failed to create the GLFW window");
        }

        //List all joysticks
        for(int i = 0; i <= GLFW_JOYSTICK_LAST; i++) {
            String joyName = glfwGetJoystickName(i);
            if (joyName == null) {
                System.out.println(i + ") NULL JOYSTICK");
            } else {
                System.out.println(i + ") " + joyName);
                joysticks.add(i);
            }
        }

        //Fill the joystickAxes array
        for(int i = 0; i < 6; i++){
            joystickAxes.add(0.0f);
        }



        //Setup a key callback, which will be called every time a key is pressed, repeated, or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE){ //Determine if the escape key has been released.
                glfwSetWindowShouldClose(window, true); //Detected in window render loop.
            }
        });

        //Get the thread stack and push a new frame
        try(MemoryStack stack = stackPush()){
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            //Get the window size passed to the createWindow method
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0))/2, (vidmode.height() - pHeight.get(0))/2); //Center the window
            glfwMakeContextCurrent(window); //Make the opengl context current
            glfwSwapInterval(1); //Enable V-Sync
            glfwShowWindow(window); //Make the window visible
        }
        System.out.println("END INIT");
    }

    private void loop(){
        GL.createCapabilities(); //Critical line for externally managed OpenGL context (like GLFW's). Also makes OpenGL bindings available
        getJoysticks();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f); //Set the clear color (the empty background color)
        while (!glfwWindowShouldClose(window)){ // Run the rendering loop until the escape key has been pressed or the window has been closed.
            render();
        }
    }

    private void render(){
        //Easies to read variables for colors from joysticks
        float red = Math.abs(joystickAxes.get(1));
        if(red < 0.02f){
            red = 0.0f;
        }

        float green = Math.abs(joystickAxes.get(4) - joystickAxes.get(5));

        float blue = Math.abs(joystickAxes.get(3));
        if(blue < 0.04f){
            blue = 0.0f;
        }

        title = "TestGame - Red:" + red + " Green:" + green + " Blue:" + blue; //Format a title string with the color values
        glfwSetWindowTitle(window, title); //Set title to show us the current color

        glClearColor(red, green, blue, 0.0f); //Set the clear color to the joystick axes
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); //Clear the framebuffer
        glfwSwapBuffers(window); //Swap the color buffers
        glfwPollEvents(); //Poll for window events (like the key callbacks)

        getJoysticks(); //Get the joystick information
    }

    private void getJoysticks(){
        FloatBuffer b = glfwGetJoystickAxes(joysticks.get(0));

        //printJoysticks(b);

        joystickAxes.set(0, b.get(0));
        joystickAxes.set(1, b.get(1));
        joystickAxes.set(2, b.get(2));
        joystickAxes.set(3, b.get(3));
        joystickAxes.set(4, b.get(4));
        joystickAxes.set(5, b.get(5));
    }

    private void printJoysticks(FloatBuffer buffer){
        String buff = "";
        while(buffer.hasRemaining()){
            buff += " " + buffer.get();
        }
        System.out.println(buff);
    }

    private void exit(){
        //Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        //Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public static void main(String[] args){
        new Main().run();
    }
}
