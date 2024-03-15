package bms.player.beatoraja.controller;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import org.lwjgl.glfw.GLFW;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerManager;
//import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.utils.Array;

public class Lwjgl3ControllerManager implements ControllerManager {

	private final long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();

	private boolean focused = true;
	final Array<Controller> controllers = new Array<Controller>();
	final Array<Integer> blacklistedControllers = new Array<Integer>();
	final Array<Controller> polledControllers = new Array<Controller>();
	final Array<ControllerListener> listeners = new Array<ControllerListener>();
	
	public Lwjgl3ControllerManager() {
		GLFW.glfwSetWindowFocusCallback(windowHandle, this::setUnfocused);
		pollState();
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run () {
				if (focused) {
					pollState();
				}
				Gdx.app.postRunnable(this);
			}
		});
	}
	
	void pollState() {

		for(int i = GLFW.GLFW_JOYSTICK_1; i < GLFW.GLFW_JOYSTICK_LAST; i++) {
			if (blacklistedControllers.contains(i, true)) {
				continue;
			}
			if(GLFW.glfwJoystickPresent(i)) {
				boolean alreadyUsed = false;
				for(int j = 0; j < controllers.size; j++) {
					if(((Lwjgl3Controller)controllers.get(j)).index == i) {
						alreadyUsed = true;
						break;
					}
				}
				if(!alreadyUsed) {
					try {
						Lwjgl3Controller controller = new Lwjgl3Controller(this, i);
						connected(controller);
					} catch (Exception e) {
						blacklistedControllers.add(i);
					}

				}
			}
		}
		
		polledControllers.addAll(controllers);
		for(Controller controller: polledControllers) {
			((Lwjgl3Controller)controller).pollState();
		}
		polledControllers.clear();
	}
	
	@Override
	public Array<Controller> getControllers () {
		pollState();
		return controllers;
	}

	@Override
	public Controller getCurrentController() {
		return null;
	}

	@Override
	public void addListener (ControllerListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener (ControllerListener listener) {
		listeners.removeValue(listener, true);
	}

	@Override
	public void clearListeners () {
		listeners.clear();
	}
	
	void connected (Lwjgl3Controller controller) {
		controllers.add(controller);
		for(ControllerListener listener: listeners) {
			listener.connected(controller);
		}
	}

	void disconnected (Lwjgl3Controller controller) {	
		controllers.removeValue(controller, true);
		for(ControllerListener listener: listeners) {
			listener.disconnected(controller);
		}
	}
	
	void axisChanged (Lwjgl3Controller controller, int axisCode, float value) {
		for(ControllerListener listener: listeners) {
			if (listener.axisMoved(controller, axisCode, value)) break;
		}
	}
	
	void buttonChanged (Lwjgl3Controller controller, int buttonCode, boolean value) {
		for(ControllerListener listener: listeners) {
			if(value) {
				if (listener.buttonDown(controller, buttonCode)) break;
			} else {
				if (listener.buttonUp(controller, buttonCode)) break;
			}
		}
	}

/*	void hatChanged (Lwjgl3Controller controller, int hatCode, PovDirection value) {
		for(ControllerListener listener: listeners) {
			if (listener.povMoved(controller, hatCode, value)) break;
		}
	}*/

	@Override
	public Array<ControllerListener> getListeners () {
		return listeners;
	}

	void setUnfocused (long win, boolean isFocused) {
		this.focused = isFocused;
	}
}
