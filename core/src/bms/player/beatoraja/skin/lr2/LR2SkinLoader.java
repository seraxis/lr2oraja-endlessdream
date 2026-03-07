package bms.player.beatoraja.skin.lr2;

import java.io.File;
import java.util.Deque;
import java.util.LinkedList;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.ObjectMap;

import bms.player.beatoraja.MainState;
import bms.player.beatoraja.skin.SkinLoader;
import bms.player.beatoraja.skin.property.*;
import com.badlogic.gdx.utils.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LR2スキンローダー
 * 
 * @author exch
 */
public abstract class LR2SkinLoader extends SkinLoader {
	private static Logger logger = LoggerFactory.getLogger(LR2SkinLoader.class);

	private Array<Command> commands = new Array<Command>();

	protected IntIntMap op = new IntIntMap();

	public LR2SkinLoader() {
	}

	protected void addCommandWord(Command cm) {
		commands.add(cm);
	}

	protected void addCommandWord(Command... cm) {
		commands.addAll(cm);
	}

	protected void processLine(Context ctx, String line, MainState state) {
		if (!line.startsWith("#") ) {
			return;
		}
		String[] str = line.split(",", -1);
		if (str.length == 0) {
			return ;
		}
		String head = str[0].toUpperCase();
		switch (head) {
			case "#IF" -> {
				boolean ok = true;
				for (int i = 1; i < str.length; ++i) {
					ok &= parseOneCond(str[i], state);
				}
				ctx.push(ok);
			}
			case "#ELSEIF" -> {
				Context.State top = ctx.top();
				if (top != null) {
					if (top.matched) {
						top.active = false;
					} else {
						boolean ok = true;
						for (int i = 1; i < str.length; i++) {
							ok &= parseOneCond(str[i], state);
						}
						top.matched = ok;
						top.active = ok;
					}
				}
			}
			case "#ELSE" -> {
				Context.State top = ctx.top();
				if (top != null) {
					if (top.matched) {
						top.active = false;
					} else {
						top.matched = true;
						top.active = true;
					}
				}
			}
			case "#ENDIF" -> ctx.pop();
			default -> {
				Context.State top = ctx.top();
				if (top == null || top.active) {
					if (str[0].equalsIgnoreCase("#SETOPTION")) {
						int index = Integer.parseInt(str[1]);
						op.put(index, Integer.parseInt(str[2]) >= 1 ? 1 : 0);
					}

					Command command = null;
					for (Command cm : commands) {
						if (str[0].substring(1).equalsIgnoreCase(cm.name())) {
							command = cm;
							break;
						}
					}
					if (command != null) {
						command.execute(this, str);
					}
				}
			}
		}
	}

	protected boolean parseOneCond(String cond, MainState state) {
		if (cond == null || cond.isEmpty())	{
			return true;
		}
		try {
			int opt = Integer.parseInt(cond.replace('!', '-').replaceAll("[^0-9-]", ""));
			if (opt >= 0) {
				if (op.get(opt, -1) == 1) {
					return true;
				}
			} else {
				if (op.get(-opt, -1) == 0) {
					return true;
				}
			}
			if (!op.containsKey(Math.abs(opt)) && state != null) {
				BooleanProperty draw = BooleanPropertyFactory.getBooleanProperty(opt);
				if (draw != null) {
					return draw.get(state);
				}
			}
		} catch (NumberFormatException e) {
			logger.warn("Failed to parse a condition: ", e);
		}
		return true;
	}

	public IntIntMap getOption() {
		return op;
	}
	
	protected static File getPath(String skinpath, String imagepath, ObjectMap<String, String> filemap) {
		File file = SkinLoader.getPath(imagepath.replace("LR2files\\Theme", skinpath).replace("\\", "/"), filemap);
		// If the file doesn't exist, we'll try to see if it's inside a dxa file
		if (!file.exists()) {
			File parentFile = file.getParentFile();
			if (parentFile != null && !parentFile.exists()) {
				File parentDXA = new File(parentFile.getPath() + ".dxa");
				if (parentDXA.exists()) {
					// We support the dxa file by uncompressing it to disk eagerly
					logger.info("file at {} cannot be found, but a dxa file is being found: {}", file, parentDXA);
					try {
						DXADecoder.extractToSameDirectory(parentDXA.getAbsolutePath(), null);
					} catch (Exception e) {
						logger.error("Failed to extract dxa file correctly", e);
					}
				}
			}
		}
		return file;
	}

	public abstract class CommandWord implements Command<LR2SkinLoader> {

		public final String str;

		public String name() {
			return str;
		}
		
		public CommandWord(String str) {
			this.str = str;
		}

		public void execute(LR2SkinLoader loader, String[] values) {
			execute(values);
		}

		public abstract void execute(String[] values);

	}
	
	public interface Command<T extends LR2SkinLoader> {
		
		public abstract String name();
		public abstract void execute(T loader, String[] values);		
	}

	/**
	 * Context of loading a lr2 skin
	 *
	 * @implNote Dangling states like isolatede #ELSE is being ignored. E.g. dangling #ELSE will be executed no matter
	 *  how, same as dangling #ELSEIF. The very one exception is a #IF command without #ENDIF is going to be persisted
	 *  in the stack and anything could happen after.
	 */
	protected static class Context {
		private Deque<State> stack;

		/**
		 * Skin config options
		 */
		public IntIntMap op;

		public Context(IntIntMap op) {
			this.op = op;
			stack = new LinkedList<>();
		}

		public void push(boolean cond) {
			stack.push(new State(cond, cond));
		}

		public void pop() {
			if (!stack.isEmpty()) {
				stack.pop();
			}
		}

		public State top() {
			return stack.isEmpty() ? null : stack.getLast();
		}

		/**
		 * A simple pair of boolean:
		 *  matched: whether there's a branch that has been matched
		 *  active: whether we're in an 'active' branch (we need to execute the lines within) or not
		 */
		public static class State {
			public boolean matched;
			public boolean active;

			public State(boolean matched, boolean active) {
				this.matched = matched;
				this.active = active;
			}
		}
	}
}
