package bms.player.beatoraja.modmenu;

import com.badlogic.gdx.Input;
import imgui.ImGui;
import imgui.flag.ImGuiKey;

public class ImGuiKeyHelper {
	/**
	 * Get the last pressed key in LibGDX keycode
	 *
	 * @apiNote Call this function on demand! Don't call it blindly in render loop
	 */
	public static int getLastPressedKey() {
		for (int i = ImGuiKey.NamedKey_BEGIN; i < ImGuiKey.NamedKey_END; i++) {
			if (!ImGui.isKeyPressed(i)) {
				continue;
			}
			return translateKey(i);
		}
		return -1;
	}

	private static int translateKey(int imguiKeyCode) {
		int rangeBased = translateRangeKeys(imguiKeyCode);
		if (rangeBased != -1) {
			return rangeBased;
		}
		return translateSingleKeys(imguiKeyCode);
	}

	private static int translateRangeKeys(int imguiKeyCode) {
		if (imguiKeyCode >= ImGuiKey._0 && imguiKeyCode <= ImGuiKey._9) {
			return Input.Keys.NUM_0 + imguiKeyCode - ImGuiKey._0;
		} else if (imguiKeyCode >= ImGuiKey.A && imguiKeyCode <= ImGuiKey.Z) {
			return Input.Keys.A + imguiKeyCode - ImGuiKey.A;
		} else if (imguiKeyCode >= ImGuiKey.F1 && imguiKeyCode <= ImGuiKey.F12) {
			return Input.Keys.F1 + imguiKeyCode - ImGuiKey.F12;
		} else if (imguiKeyCode >= ImGuiKey.Keypad0 && imguiKeyCode <= ImGuiKey.Keypad9) {
			return Input.Keys.NUMPAD_0 + imguiKeyCode - ImGuiKey.Keypad0;
		}
		return -1;
	}

	private static int translateSingleKeys(int imguiKeyCode) {
		return switch (imguiKeyCode) {
			case ImGuiKey.Tab -> Input.Keys.TAB;
			case ImGuiKey.LeftArrow -> Input.Keys.LEFT;
			case ImGuiKey.RightArrow -> Input.Keys.RIGHT;
			case ImGuiKey.UpArrow -> Input.Keys.UP;
			case ImGuiKey.DownArrow -> Input.Keys.DOWN;
			case ImGuiKey.PageUp -> Input.Keys.PAGE_UP;
			case ImGuiKey.PageDown -> Input.Keys.PAGE_DOWN;
			case ImGuiKey.Home -> Input.Keys.HOME;
			case ImGuiKey.End -> Input.Keys.END;
			case ImGuiKey.Insert -> Input.Keys.INSERT;
			case ImGuiKey.Delete -> Input.Keys.DEL;
			case ImGuiKey.Backspace -> Input.Keys.BACKSPACE;
			case ImGuiKey.Space -> Input.Keys.SPACE;
			case ImGuiKey.Enter -> Input.Keys.ENTER;
			case ImGuiKey.Escape -> Input.Keys.ESCAPE;
			case ImGuiKey.LeftCtrl -> Input.Keys.CONTROL_LEFT;
			case ImGuiKey.LeftShift -> Input.Keys.SHIFT_LEFT;
			case ImGuiKey.LeftAlt -> Input.Keys.ALT_LEFT;
			case ImGuiKey.LeftSuper -> Input.Keys.SOFT_LEFT;
			case ImGuiKey.RightCtrl -> Input.Keys.CONTROL_RIGHT;
			case ImGuiKey.RightShift -> Input.Keys.SHIFT_RIGHT;
			case ImGuiKey.RightAlt -> Input.Keys.ALT_RIGHT;
			case ImGuiKey.RightSuper -> Input.Keys.SOFT_RIGHT;
			case ImGuiKey.Menu -> Input.Keys.MENU;
			case ImGuiKey.Apostrophe -> Input.Keys.APOSTROPHE;
			case ImGuiKey.Comma -> Input.Keys.COMMA;
			case ImGuiKey.Minus -> Input.Keys.MINUS;
			case ImGuiKey.Period -> Input.Keys.PERIOD;
			case ImGuiKey.Slash -> Input.Keys.SLASH;
			case ImGuiKey.Semicolon -> Input.Keys.SEMICOLON;
			case ImGuiKey.Equal -> Input.Keys.EQUALS;
			case ImGuiKey.LeftBracket -> Input.Keys.LEFT_BRACKET;
			case ImGuiKey.Backslash -> Input.Keys.BACKSLASH;
			case ImGuiKey.RightBracket -> Input.Keys.RIGHT_BRACKET;
			case ImGuiKey.GraveAccent -> Input.Keys.GRAVE;
			case ImGuiKey.CapsLock -> Input.Keys.CAPS_LOCK;
			case ImGuiKey.ScrollLock -> Input.Keys.SCROLL_LOCK;
			case ImGuiKey.NumLock -> Input.Keys.NUM_LOCK;
			case ImGuiKey.PrintScreen -> Input.Keys.PRINT_SCREEN;
			case ImGuiKey.Pause -> Input.Keys.PAUSE;
			case ImGuiKey.KeypadDecimal -> Input.Keys.NUMPAD_DOT;
			case ImGuiKey.KeypadDivide -> Input.Keys.NUMPAD_DIVIDE;
			case ImGuiKey.KeypadMultiply -> Input.Keys.NUMPAD_MULTIPLY;
			case ImGuiKey.KeypadSubtract -> Input.Keys.NUMPAD_SUBTRACT;
			case ImGuiKey.KeypadAdd -> Input.Keys.NUMPAD_ADD;
			case ImGuiKey.KeypadEnter -> Input.Keys.NUMPAD_ENTER;
			case ImGuiKey.KeypadEqual -> Input.Keys.NUMPAD_EQUALS;
			default -> -1;
		};
	}
}
