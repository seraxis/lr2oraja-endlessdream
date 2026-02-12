package bms.player.beatoraja.skin.lr2;

import java.io.File;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.ObjectMap;

import bms.player.beatoraja.MainState;
import bms.player.beatoraja.skin.SkinLoader;
import bms.player.beatoraja.skin.property.*;
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
		// Extra mode off
		op.put(52, 1);
		op.put(53, 0);
		// TODO: If we support battle mode in the future, remove this
		op.put(10, 0); // Current mode is Double or Double Battle
		op.put(11, 0); // Current mode is Battle
		op.put(12, 0); // Current mode is Double or Battle or Double Battle
		op.put(13, 0); // Current mode is Ghost Battle or Battle
	}

	protected void addCommandWord(Command cm) {
		commands.add(cm);
	}

	protected void addCommandWord(Command... cm) {
		commands.addAll(cm);
	}

	boolean skip = false;
	boolean ifs = false;

	protected void processLine(String line, MainState state) {
		if (!line.startsWith("#") ) {
			return;
		}
		String[] str = line.split(",", -1);
		if (str.length > 0) {
			if (str[0].equalsIgnoreCase("#IF")) {
				ifs = true;
				for (int i = 1; i < str.length; i++) {
					boolean b = false;
					if (str[i].length() == 0) {
						continue;
					}
					try {
						int opt = Integer.parseInt(str[i].replace('!', '-').replaceAll("[^0-9-]", ""));
						if(opt >=  0) {
							if(op.get(opt, -1) == 1) {
								b = true;
							}
						} else {
							if(op.get(-opt, -1) == 0) {
								b = true;
							}
						}
						if (!b && !op.containsKey(Math.abs(opt)) && state != null) {
							BooleanProperty draw = BooleanPropertyFactory.getBooleanProperty(opt);
							if(draw != null) {
								b = draw.get(state);								
							}
						}
						if (!b) {
							ifs = false;
							break;
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
						break;
					}
				}

				skip = !ifs;
			} else if (str[0].equalsIgnoreCase("#ELSEIF")) {
				if (ifs) {
					skip = true;
				} else {
					ifs = true;
					for (int i = 1; i < str.length; i++) {
						boolean b = false;
						try {
							int opt = Integer.parseInt(str[i].replace('!', '-').replaceAll("[^0-9-]", ""));
							if(opt >=  0) {
								if(op.get(opt, -1) == 1) {
									b = true;
								}
							} else {
								if(op.get(-opt, -1) == 0) {
									b = true;
								}
							}
							if (!b && !op.containsKey(Math.abs(opt)) && state != null) {
								BooleanProperty draw = BooleanPropertyFactory.getBooleanProperty(opt);
								if(draw != null) {
									b = draw.get(state);								
								}
							}
							if (!b) {
								ifs = false;
								break;
							}
						} catch (NumberFormatException e) {
							break;
						}
					}

					skip = !ifs;
				}
			} else if (str[0].equalsIgnoreCase("#ELSE")) {
				skip = ifs;
			} else if (str[0].equalsIgnoreCase("#ENDIF")) {
				skip = false;
				ifs = false;
			}
			if (!skip) {
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
				if(command != null) {
					command.execute(this, str);					
				}
			}
		}
	}

	public IntIntMap getOption() {
		return op;
	}
	
	protected static File getPath(String skinpath, String imagepath, ObjectMap<String, String> filemap) {
		File file = SkinLoader.getPath(imagepath.replace("LR2files\\Theme", skinpath).replace("\\", "/"), filemap);
		// If the file doesn't exist, we'll try to see if it's inside a dxa file
		if (!file.exists()) {
			File parentFile = file.getParentFile();
			File parentDXA = new File(parentFile.getPath() + ".dxa");
			if (!parentFile.exists() && parentDXA.exists()) {
				// We support the dxa file by uncompressing it to disk eagerly
				logger.info("file at {} cannot be found, but a dxa file is being found: {}", file, parentDXA);
				try {
					DXADecoder.extractToSameDirectory(parentDXA.getAbsolutePath(), null);
				} catch (Exception e) {
					logger.error("Failed to extract dxa file correctly", e);
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
}
