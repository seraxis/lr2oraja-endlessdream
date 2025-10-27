package bms.player.beatoraja.skin.property;

import com.badlogic.gdx.utils.IntMap;
import org.luaj.vm2.LuaNumber;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

/**
 * Endless Dream only skin properties. There're several designs to avoid producing conflicts with old skin system:
 * <ul>
 *     <li>
 *     Old skin system uses magic number as the id for each property. Therefore, we need a dynamic allocate id here.
 *     Meanwhile, we cannot use the old way to reference a property. Instead, we expose some functions that return
 *     the specific property's dynamic id. In below example, main_state.rival() would return the corresponding id.
 *     <pre><code>table.insert(skin.text, { id = "rival", ref = main_state.rival() })</pre>
 *     </li>
 *     <li>
 *     Another way to reference the skin property is using an OneArgFunction, for example some skins access the text
 *     property by calling the text function:
 *     <pre><code>local rival = main_state.text(2)</pre>
 *     Therefore, we need to allow user write code like:
 *     <pre><code>local rival = main_state.text(main_state.rival())</pre>
 *     In order to achieve this we need to change the implementation of StringPropertyFactory a little bit: we provide
 *     the same functions(getStringProperty) and let it "fallback" to here when it cannot find property
 *     </li>
 * </ul>
 *
 * @author Catizard, Arctice
 */
public class EndlessDreamPropertyFactory {
	private static final IntMap<StringType> stringPropertyMap = new IntMap<>();
	private static final IntMap<BooleanType> booleanPropertyMap = new IntMap<>();
	private static final IntMap<FloatType> floatPropertyMap = new IntMap<>();

	public static StringProperty getStringProperty(int id) {
		StringType r = stringPropertyMap.get(id);
		return r == null ? null : r.property;
	}

	public static StringProperty getStringProperty(String name) {
		for (StringType stringType : StringType.values()) {
			if (stringType.name().equalsIgnoreCase(name)) {
				return stringType.property;
			}
		}
		return null;
	}

	public static FloatProperty getFloatProperty(int id) {
		FloatType r = floatPropertyMap.get(id);
		return r == null ? null : r.property;
	}

	public static FloatProperty getFloatProperty(String name) {
		for (FloatType floatType : FloatType.values()) {
			if (floatType.name().equalsIgnoreCase(name)) {
				return floatType.property;
			}
		}
		return null;
	}

	public static BooleanProperty getBooleanProperty(int optionId) {
		int id = Math.abs(optionId);
		// NOTE: We don't have to revert the negative option id result here, it would be handled inside BooleanPropertyFactory
		BooleanType b = booleanPropertyMap.get(id);
		return b == null ? null : b.property;
	}

	/**
	 * Initialize the dynamic id and expose them to lua env
	 * @param table main_state table
	 */
	public static void initialize(LuaTable table) {
		int stringMaximumID = getStringPropertyMaximumID();
		for (StringType stringType : StringType.values()) {
			int nextID = ++stringMaximumID;
			stringPropertyMap.put(nextID, stringType);
			table.set(stringType.name(), new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					return LuaNumber.valueOf(nextID);
				}
			});
		}
		int floatMaximumID = getFloatPropertyMaximumID();
		for (FloatType floatType : FloatType.values()) {
			int nextID = ++floatMaximumID;
			floatPropertyMap.put(nextID, floatType);
			table.set(floatType.name(), new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					return LuaNumber.valueOf(nextID);
				}
			});
		}
		int booleanMaximumID = getBooleanPropertyMaximumID();
		for (BooleanType booleanType : BooleanType.values()) {
			int nextID = ++booleanMaximumID;
			booleanPropertyMap.put(nextID, booleanType);
			table.set(booleanType.name(), new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					return LuaNumber.valueOf(nextID);
				}
			});
		}
	}

	public enum StringType {
		currentBGMPath((state) -> state.main.getSoundManager().getBGMPath().toString()),
		currentSoundPath((state) -> state.main.getSoundManager().getSoundPath().toString());

		/**
		 * String Property
		 */
		private final StringProperty property;

		StringType(StringProperty property) {
			this.property = property;
		}

		public StringProperty getProperty() {
			return property;
		}
	}

	public enum BooleanType {
		;

		/**
		 * Boolean Property
		 */
		private final BooleanProperty property;

		BooleanType(BooleanProperty property) {
			this.property = property;
		}

		public BooleanProperty getProperty() {
			return property;
		}
	}

	public enum FloatType {
		;

		/**
		 * Float Property
		 */
		private final FloatProperty property;

		FloatType(FloatProperty property) {
			this.property = property;
		}

		public FloatProperty getProperty() {
			return property;
		}
	}

	private static int getStringPropertyMaximumID() {
		int r = 0;
		for (StringPropertyFactory.StringType stringType : StringPropertyFactory.StringType.values()) {
			r = Math.max(r, stringType.getId());
		}
		return r;
	}

	private static int getFloatPropertyMaximumID() {
		int r = 0;
		// Call FloatPropertyFactory.FloatType.values() always leads to a crash, no idea why
		for (FloatPropertyFactory.FloatType floatType : FloatPropertyFactory.FloatTypeValues) {
			r = Math.max(r, floatType.getId());
		}
		return r;
	}

	private static int getBooleanPropertyMaximumID() {
		int r = 0;
		for (BooleanPropertyFactory.BooleanType booleanType : BooleanPropertyFactory.BooleanType.values()) {
			r = Math.max(r, booleanType.getId());
		}
		return r;
	}
}
