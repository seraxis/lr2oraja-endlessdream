package bms.player.beatoraja.modmenu;

import java.util.*;
import java.util.stream.Stream;
import java.io.IOException;
import java.nio.file.*;
import java.awt.Desktop;

import bms.player.beatoraja.skin.*;
import bms.player.beatoraja.skin.json.JSONSkinLoader;
import bms.player.beatoraja.skin.lr2.LR2SkinHeaderLoader;
import bms.player.beatoraja.skin.lua.LuaSkinLoader;
import static bms.player.beatoraja.skin.SkinProperty.OPTION_RANDOM_VALUE;

import bms.player.beatoraja.MainState;
import bms.player.beatoraja.MainController;
import bms.player.beatoraja.SkinConfig;
import bms.player.beatoraja.PlayerConfig;

import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.select.BarRenderer;

import imgui.ImGui;
import imgui.ImColor;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.flag.*;
import com.badlogic.gdx.Gdx;

public class SkinMenu {
    private static MainController main = null;
    private static PlayerConfig playerConfig;

    public static void init(MainController mainState, PlayerConfig config) {
        main = mainState;
        playerConfig = config;
    }

    private static boolean ready = false;
    private static ImBoolean liveEditing = new ImBoolean(true);
    private static ImBoolean freezeTimers = new ImBoolean(false);

    private static MainState observedState;
    private static SkinType currentSkinType;
    private static SkinHeader currentSkin;
    private static HashMap<String, Integer> setOptions = null;
    private static HashMap<String, List<String>> availableFiles = null;
    private static HashMap<String, String> setFiles = null;
    private static HashMap<String, Offset> setOffsets = null;

    private static class Offset {
        int[] x = {0};
        int[] y = {0};
        int[] w = {0};
        int[] h = {0};
        int[] r = {0};
        int[] a = {0};
        protected Offset(int x, int y, int w, int h, int r, int a) {
            this.x[0] = x;
            this.y[0] = y;
            this.w[0] = w;
            this.h[0] = h;
            this.r[0] = r;
            this.a[0] = a;
        }
    }

    private static List<SkinHeader> skins;

    public static void show(ImBoolean showSkinMenu) {
        if (main == null) { return; }
        if (observedState != main.getCurrentState()) { invalidate(); }
        if (!ready) { refresh(); }

        int windowHeight = Gdx.graphics.getHeight();
        ImGui.setNextWindowSize(0.f, windowHeight * 0.3f, ImGuiCond.FirstUseEver);

        if (ImGui.begin("Skin", showSkinMenu)) {
            menuHeader();
            ImGui.separator();
            ImGui.pushID(currentSkin.getName());
            if (currentSkin != null) { skinConfigMenu(); }
            ImGui.popID();
        }
        ImGui.end();
    }

    private static void menuHeader() {
        if (ImGui.arrowButton("##skin-select-left", 0)) {
            int index = skins.indexOf(currentSkin);
            index = (index + skins.size() - 1) % skins.size();
            switchCurrentSceneSkin(skins.get(index));
        }

        ImGui.sameLine();
        if (ImGui.beginCombo("##skin-select-combo", currentSkin.getName(),
                             ImGuiComboFlags.HeightLarge)) {
            for (var header : skins) {
                String skinPath = header.getPath().toString();
                ImGui.pushID(skinPath);
                if (ImGui.selectable(header.getName())) { switchCurrentSceneSkin(header); }
                ImGui.popID();
            }
            ImGui.endCombo();
        }
        ImGui.sameLine();

        if (ImGui.arrowButton("##skin-select-right", 1)) {
            int index = skins.indexOf(currentSkin);
            index = (index + 1) % skins.size();
            switchCurrentSceneSkin(skins.get(index));
        }

        if (ImGui.button("Open##open-skin-location")) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(currentSkin.getPath().getParent().toFile());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        ImGui.sameLine();
        ImGui.textDisabled(String.format("> %s", currentSkin.getPath().toString()));

        boolean saveAvailable = dirtyConfig && !liveEditing.get();
        ImGui.beginDisabled(!saveAvailable);
        boolean saveRequested = ImGui.button(" Save ##reload-current-skin");
        ImGui.endDisabled();
        if (saveRequested || (dirtyConfig && liveEditing.get())) {
            switchCurrentSceneSkin(currentSkin);
        }
        ImGui.sameLine();
        dirty(ImGui.checkbox("Live Editing###live-edit-mode", liveEditing));

        if (ImGui.button(" Reset ##skin-setting-reset-request")) {
            ImGui.openPopup("skin-setting-reset-confirmation");
        }
        if (ImGui.beginPopup("skin-setting-reset-confirmation",
                             ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.text("Reset current skin's settings to default");
            ImGui.text("ARE YOU SURE?");
            ImGui.sameLine();
            if (ImGui.button(" Confirm ##skin-setting-reset-execute")) {
                resetCurrentSkinConfig();
                switchCurrentSceneSkin(currentSkin);
                ImGui.closeCurrentPopup();
            }
            ImGui.textDisabled("(click outside popup to close)");
            ImGui.endPopup();
        }

        ImGui.sameLine();
        if (ImGui.checkbox("Freeze timers###freeze-mode", freezeTimers)) {
            main.getTimer().setFrozen(freezeTimers.get());
        }
    }

    private static void skinConfigMenu() {
        HashSet<String> shown = new HashSet<String>();

        boolean tabbar = 0 < currentSkin.getCustomCategories().length;
        if (tabbar) { ImGui.beginTabBar("#tab-bar"); }
        for (var category : currentSkin.getCustomCategories()) {
            boolean tabOpen = ImGui.beginTabItem(category.name + "##category-tab");
            if (tabOpen) { ImGui.beginChild("skin-config", 0, 0, true); }
            for (var item : category.items) {
                // safety check?
                shown.add(item.name);
                if (!tabOpen) { continue; }
                if (item instanceof SkinHeader.CustomOption) {
                    skinConfigOption((SkinHeader.CustomOption)item);
                }
                else if (item instanceof SkinHeader.CustomFile) {
                    skinConfigFile((SkinHeader.CustomFile)item);
                }
                else if (item instanceof SkinHeader.CustomOffset) {
                    skinConfigOffset((SkinHeader.CustomOffset)item);
                }
            }
            if (tabOpen) {
                ImGui.endChild();
                ImGui.endTabItem();
            }
        }

        boolean otherTab = tabbar && ImGui.beginTabItem("Other##category-tab");
        if (!tabbar || otherTab) {
            if (!tabbar) ImGui.beginChild("skin-config", 0, 0, true);
            SkinHeader.CustomOption[] options = currentSkin.getCustomOptions();
            for (var option : options) {
                if (shown.contains(option.name)) { continue; }
                skinConfigOption(option);
            }
            SkinHeader.CustomFile[] files = currentSkin.getCustomFiles();
            for (var file : files) {
                if (shown.contains(file.name)) { continue; }
                skinConfigFile(file);
            }
            SkinHeader.CustomOffset[] offsets = currentSkin.getCustomOffsets();
            for (var offset : offsets) {
                if (shown.contains(offset.name)) { continue; }
                skinConfigOffset(offset);
            }
            if (!tabbar) ImGui.endChild();
        }

        if (otherTab) { ImGui.endTabItem(); }
        if (tabbar) { ImGui.endTabBar(); }
    }

    private static void skinConfigOption(SkinHeader.CustomOption option) {
        // we pretend 'Random' is at the end of the list
        int optionsCount = (option.contents.length + 1);
        // for options with 2 choices (3 with random), show a radio instead
        if (3 == optionsCount) {
            skinConfigOptionRadio(option);
            return;
        }

        ImGui.pushID(option.name);

        int value = getOptionSetting(option);
        int selected = optionIndex(option, value);
        String chosen = selected == OPTION_RANDOM_VALUE ? "Random" : option.contents[selected];

        boolean arrowChangeSelection = false;
        if (ImGui.arrowButton("##option-left", 0)) {
            selected = (selected + optionsCount - 1) % optionsCount;
            arrowChangeSelection = true;
        }

        ImGui.sameLine();
        ImGui.setNextItemWidth(ImGui.getContentRegionAvail().x / 3.5f);
        if (ImGui.beginCombo("##combo", String.format("%s", chosen))) {
            for (int i = 0; i < option.contents.length; ++i) {
                if (ImGui.selectable(option.contents[i])) {
                    setOptions.put(option.name, option.option[i]);
                    dirty(true);
                }
            }
            if (ImGui.selectable("Random")) {
                setOptions.put(option.name, OPTION_RANDOM_VALUE);
                dirty(true);
            }
            ImGui.endCombo();
        }

        ImGui.sameLine();
        if (ImGui.arrowButton("##option-right", 1)) {
            selected = (selected + 1) % optionsCount;
            arrowChangeSelection = true;
        }

        if (arrowChangeSelection) {
            if (selected == option.contents.length)
                setOptions.put(option.name, OPTION_RANDOM_VALUE);
            else
                setOptions.put(option.name, option.option[selected]);
            dirty(true);
        }

        ImGui.sameLine();
        ImGui.text(option.name);

        ImGui.popID();
    }

    private static void skinConfigOptionRadio(SkinHeader.CustomOption option) {
        ImGui.pushID(option.name);
        ImGui.text(option.name);

        int value = getOptionSetting(option);
        ImInt radio = new ImInt(value);
        ImGui.indent(10);
        for (int i = 0; i < option.contents.length; ++i) {
            ImGui.radioButton(option.contents[i], radio, option.option[i]);
            ImGui.sameLine();
        }
        ImGui.radioButton("Random", radio, OPTION_RANDOM_VALUE);

        if (radio.get() != value) {
            setOptions.put(option.name, radio.get());
            dirty(true);
        }

        ImGui.unindent(10);
        ImGui.popID();
    }

    private static int optionIndex(SkinHeader.CustomOption option, int value) {
        for (int i = 0; i < option.option.length; ++i) {
            if (option.option[i] == value) { return i; }
        }
        return OPTION_RANDOM_VALUE;
    }

    private static void skinConfigFile(SkinHeader.CustomFile file) {
        String selection = getFileSetting(file);
        if (selection == null || availableFiles.get(file.name) == null) { return; }
        ImGui.pushStyleColor(ImGuiCol.FrameBg, ImColor.rgb(30, 95, 110));
        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, ImColor.rgb(40, 120, 140));

        ImGui.pushID(file.name);

        var choices = availableFiles.get(file.name);
        int index = choices.indexOf(selection);
        int max = choices.size();

        if (ImGui.arrowButton("##option-left", 0)) {
            index = (index + max - 1) % max;
            setFiles.put(file.name, choices.get(index));
            dirty(true);
        }

        ImGui.sameLine();
        ImGui.setNextItemWidth(ImGui.getContentRegionAvail().x / 3.f);
        if (ImGui.beginCombo("##file-combo", selection)) {
            for (var path : choices) {
                if (ImGui.selectable(path)) {
                    setFiles.put(file.name, path);
                    dirty(true);
                }
            }
            ImGui.endCombo();
        }

        ImGui.sameLine();
        if (ImGui.arrowButton("##option-right", 1)) {
            index = (index + 1) % max;
            setFiles.put(file.name, choices.get(index));
            dirty(true);
        }

        ImGui.sameLine();
        ImGui.text(file.name);
        String normalizedPath =
            file.path.replace("*", "_WILDCARDESCAPE_").replace("|", "_PIPEESCAPE_");
        normalizedPath = Paths.get(normalizedPath).normalize().toString();
        normalizedPath =
            normalizedPath.replace("_WILDCARDESCAPE_", "*").replace("_PIPEESCAPE_", "|");
        ImGui.textDisabled(String.format("  > %s", normalizedPath));

        ImGui.popID();
        ImGui.popStyleColor(2);
    }

    private static void spawnDragInt(String name, boolean offset, int[] value) {
        if (offset) {
            ImGui.dragInt(String.format("##%s", name), value, 0.166f, 0, 0, name + " = %d");
            dirty(ImGui.isItemDeactivatedAfterEdit());
        }
        else { ImGui.dummy(100, 0); }
    }

    private static void skinConfigOffset(SkinHeader.CustomOffset offset) {
        Offset value = getOffsetSetting(offset);
        int width = 100;

        ImGui.text(offset.name);
        ImGui.pushID(offset.name);
        ImGui.pushItemWidth(width);
        ImGui.indent();

        if (offset.x || offset.w || offset.a) {
            spawnDragInt("X", offset.x, value.x);
            ImGui.sameLine();
            spawnDragInt("W", offset.w, value.w);
            ImGui.sameLine();
            spawnDragInt("a", offset.a, value.a);
        }

        if (offset.y || offset.h || offset.r) {
            spawnDragInt("Y", offset.y, value.y);
            ImGui.sameLine();
            spawnDragInt("H", offset.h, value.h);
            ImGui.sameLine();
            spawnDragInt("R", offset.r, value.r);
        }

        ImGui.unindent();
        ImGui.popItemWidth();
        ImGui.popID();
    }

    public static void invalidate() { ready = false; }

    private static void refresh() {
        setOptions = null;
        availableFiles = null;
        setFiles = null;
        setOffsets = null;

        observedState = main.getCurrentState();
        SkinHeader currentSceneSkin = observedState.getSkin().header;
        currentSkinType = currentSceneSkin.getSkinType();
        currentSkin = null;
        switchCurrentSceneSkin(currentSceneSkin);
        skins = loadAllSkins(currentSkinType);
        ready = true;
    }

    private static List<SkinHeader> loadAllSkins(SkinType type) {
        List<Path> paths = new ArrayList<>();
        Path skinsDir = Paths.get("skin");
        scanSkins(skinsDir, paths);
        List<SkinHeader> skins = new ArrayList<SkinHeader>();
        for (Path path : paths) {
            String pathString = path.toString().toLowerCase();
            SkinHeader header = null;

            if (path.equals(currentSkin.getPath())) { header = currentSkin; }
            else if (pathString.endsWith(".json")) {
                JSONSkinLoader loader = new JSONSkinLoader();
                header = loader.loadHeader(path);
            }
            else if (pathString.endsWith(".luaskin")) {
                LuaSkinLoader loader = new LuaSkinLoader();
                header = loader.loadHeader(path);
            }
            else if (pathString.endsWith(".lr2skin")) {
                LR2SkinHeaderLoader loader = new LR2SkinHeaderLoader(main.getConfig());
                try {
                    header = loader.loadSkin(path, null);

                    // 7/14key skinは5/10keyにも加える
                    if (type == SkinType.PLAY_5KEYS &&
                        header.getSkinType() == SkinType.PLAY_7KEYS &&
                        header.getType() == SkinHeader.TYPE_LR2SKIN) {
                        header = loader.loadSkin(path, null);
                        header.setSkinType(SkinType.PLAY_5KEYS);
                        if (!header.getName().toLowerCase().contains("7key")) {
                            header.setName(header.getName() + " (7KEYS) ");
                        }
                    }
                    if (type == SkinType.PLAY_10KEYS &&
                        header.getSkinType() == SkinType.PLAY_14KEYS &&
                        header.getType() == SkinHeader.TYPE_LR2SKIN) {
                        header = loader.loadSkin(path, null);
                        header.setSkinType(SkinType.PLAY_10KEYS);
                        if (!header.getName().toLowerCase().contains("14key")) {
                            header.setName(header.getName() + " (14KEYS) ");
                        }
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (header != null && header.getSkinType() == type) { skins.add(header); }
        }

        return skins;
    }

    private static void scanSkins(Path path, List<Path> paths) {
        if (Files.isDirectory(path)) {
            try (Stream<Path> sub = Files.list(path)) {
                sub.forEach((Path t) -> { scanSkins(t, paths); });
            }
            catch (IOException e) {
            }
        }
        else if (path.getFileName().toString().toLowerCase().endsWith(".lr2skin") ||
                 path.getFileName().toString().toLowerCase().endsWith(".luaskin") ||
                 path.getFileName().toString().toLowerCase().endsWith(".json")) {
            paths.add(path);
        }
    }

    private static List<String> parseCustomFile(SkinHeader.CustomFile file) {
        List<String> fileSelection = new ArrayList<String>();

        final int lastSlash = file.path.lastIndexOf('/');
        String name = file.path.substring(lastSlash + 1);
        if (file.path.contains("|")) {
            if (file.path.length() > file.path.lastIndexOf('|') + 1) {
                name = file.path.substring(lastSlash + 1, file.path.indexOf('|')) +
                       file.path.substring(file.path.lastIndexOf('|') + 1);
            }
            else { name = file.path.substring(lastSlash + 1, file.path.indexOf('|')); }
        }

        final Path dirpath =
            lastSlash != -1 ? Paths.get(file.path.substring(0, lastSlash)) : Paths.get(file.path);
        if (!Files.exists(dirpath)) { return null; }

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(
                 dirpath, "{" + name.toLowerCase() + "," + name.toUpperCase() + "}")) {
            for (Path p : paths) {
                fileSelection.add(p.getFileName().toString());
            }
            fileSelection.add("Random");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return fileSelection;
    }

    private static void loadSavedSkinSettings(SkinHeader header) {
        String skinPath = header.getPath().toString();
        SkinConfig.Property savedProperties = null;

        SkinConfig liveConfig = playerConfig.getSkin()[header.getSkinType().getId()];
        if (liveConfig != null && skinPath.equals(liveConfig.getPath())) {
            savedProperties = liveConfig.getProperties();
        }
        else {
            for (SkinConfig savedConfig : playerConfig.getSkinHistory()) {
                if (savedConfig.getPath().equals(skinPath)) {
                    savedProperties = savedConfig.getProperties();
                    break;
                }
            }
        }

        if (savedProperties != null) {
            for (var option : savedProperties.getOption()) {
                setOptions.put(option.name, option.value);
            }
            for (var file : savedProperties.getFile()) {
                setFiles.put(file.name, file.path);
            }

            for (var offset : savedProperties.getOffset()) {
                setOffsets.put(offset.name, new Offset(offset.x, offset.y, offset.w, offset.h,
                                                       offset.r, offset.a));
            }
        }
    }

    private static int getOptionSetting(SkinHeader.CustomOption option) {
        Integer value = setOptions.get(option.name);
        if (value != null) { return value; }
        return option.getDefaultOption();
    }

    private static String getFileSetting(SkinHeader.CustomFile file) {
        String path = setFiles.get(file.name);
        if (path != null) { return path; }
        return file.def;
    }

    private static Offset getOffsetSetting(SkinHeader.CustomOffset offset) {
        if (!setOffsets.containsKey(offset.name)) {
            Offset offsetProperty = new Offset(0, 0, 0, 0, 0, 0);
            setOffsets.put(offset.name, offsetProperty);
        }
        return setOffsets.get(offset.name);
    }

    private static SkinConfig.Property completeProperty(SkinHeader header) {
        // default out all unset properties
        // and collect everything into the property object
        // analogous to getProperty in SkinConfigurationView
        List<SkinConfig.Option> options = new ArrayList<>();
        List<SkinConfig.FilePath> files = new ArrayList<>();
        List<SkinConfig.Offset> offsets = new ArrayList<>();

        for (var option : header.getCustomOptions()) {
            SkinConfig.Option set = new SkinConfig.Option();
            set.name = option.name;
            set.value = getOptionSetting(option);
            setOptions.put(set.name, set.value);
            options.add(set);
        }

        for (var file : header.getCustomFiles()) {
            List<String> fileSelection = parseCustomFile(file);
            if (fileSelection == null) {
                fileSelection = new ArrayList<String>();
                fileSelection.add("Random");
            }
            availableFiles.put(file.name, fileSelection);

            String selection = setFiles.get(file.name);
            if (selection == null && file.def != null) {
                // デフォルト値のファイル名またはそれに拡張子を付けたものが存在すれば使用する
                for (String filename : fileSelection) {
                    if (filename.equalsIgnoreCase(file.def)) {
                        selection = filename;
                        break;
                    }
                    int point = filename.lastIndexOf('.');
                    if (point != -1 && filename.substring(0, point).equalsIgnoreCase(file.def)) {
                        selection = filename;
                        break;
                    }
                }
            }

            // fileSelection[0] always present due to inserted 'Random'
            if (selection == null) { selection = fileSelection.get(0); }
            setFiles.put(file.name, selection);

            SkinConfig.FilePath set = new SkinConfig.FilePath();
            set.name = file.name;
            set.path = selection;
            setFiles.put(set.name, set.path);
            files.add(set);
        }

        for (var offset : header.getCustomOffsets()) {
            SkinConfig.Offset set = new SkinConfig.Offset();
            Offset value = getOffsetSetting(offset);
            set.x = value.x[0];
            set.y = value.y[0];
            set.w = value.w[0];
            set.h = value.h[0];
            set.r = value.r[0];
            set.a = value.a[0];
            set.name = offset.name;
            offsets.add(set);
        }

        SkinConfig.Property property = new SkinConfig.Property();
        property.setOption(options.toArray(new SkinConfig.Option[0]));
        property.setFile(files.toArray(new SkinConfig.FilePath[0]));
        property.setOffset(offsets.toArray(new SkinConfig.Offset[0]));
        return property;
    }

    private static boolean dirtyConfig = false;

    private static void dirty(boolean flag) {
        if (flag) dirtyConfig = true;
    }

    private static void saveCurrentConfig(SkinHeader nextSkin) {
        dirtyConfig = false;

        String skinPath = currentSkin.getPath().toString();
        SkinConfig.Property property = completeProperty(currentSkin);
        SkinConfig config = new SkinConfig();
        config.setPath(skinPath);
        config.setProperties(property);

        if (nextSkin == currentSkin) {
            playerConfig.getSkin()[currentSkinType.getId()] = config;
            return;
        }

        for (int i = 0; i < playerConfig.getSkinHistory().length; ++i) {
            SkinConfig savedConfig = playerConfig.getSkinHistory()[i];
            if (savedConfig.getPath().equals(skinPath)) {
                playerConfig.getSkinHistory()[i] = config;
                return;
            }
        }

        // this skin hasn't been in the config history before, add it
        SkinConfig[] history =
            Arrays.copyOf(playerConfig.getSkinHistory(), playerConfig.getSkinHistory().length + 1);
        history[history.length - 1] = config;
        playerConfig.setSkinHistory(history);
    }

    private static void resetCurrentSkinConfig() {
        setOptions = new HashMap<String, Integer>();
        availableFiles = new HashMap<String, List<String>>();
        setFiles = new HashMap<String, String>();
        setOffsets = new HashMap<String, Offset>();
    }

    private static void switchCurrentSceneSkin(SkinHeader header) {
        if (currentSkin != null) saveCurrentConfig(header);

        resetCurrentSkinConfig();

        loadSavedSkinSettings(header);
        currentSkin = header;
        SkinConfig.Property property = completeProperty(header);

        String skinPath = header.getPath().toString();
        SkinConfig config = new SkinConfig();
        config.setPath(skinPath);
        config.setProperties(property);
        config.validate();

        MainState scene = main.getCurrentState();

        Skin skin = SkinLoader.load(scene, currentSkinType, config);
        if (skin == null) {
            // fallback to default skin in case of loading errors
            config = new SkinConfig();
            config.setPath(SkinConfig.Default.get(currentSkinType).path);
            config.validate();
            skin = SkinLoader.load(scene, currentSkinType, config);
        }

        playerConfig.getSkin()[currentSkinType.getId()] = config;
        scene.setSkin(skin);
        skin.prepare(scene);

        if (scene instanceof MusicSelector) {
            ((MusicSelector)scene).getBarRender().updateBarText();
        }
    }
}

// known bugs
// occasionally appears to cause skins to load images wrong
//   needs more testing / an exact reproduction (i might be imagining it)
//   might also be related to improperly set custom image values

// technical quirks / considerations / future work
// skin config does not get written to disk until beatoraja is properly closed
//   only happens when forcibly closing the game, which already fails to save some stuff
//   this skin menu itself makes it easy to recreate any customizations, so this seems fine
// block inputs after ctrl+click on drag widget
//   currently typing numbers into the widget conflicts with select scene keyboard inputs
//   users can still use mouse + fixing this risks creating input processing bugs
// it's possible to close the menu and leave the timers frozen
//   difficult to do by accident, useful for viewing changes when done intentionally
// highlighting non-default values
//   not sure how to this correctly, not sure how to do it efficiently, not sure if its useful
// preloading all skin settings when first opening the menu or changing the scene
// can be slow, up to a few seconds on an older machine with lots of skins
//   possible fix: show abbreviated paths instead of unnecessarily loading all skin headers
//   might not be fine to leave as-is, this load doesn't happen all that often
//   and most people won't have that many skins
// support for a skin customization overlay file
//   it might be possible to use the skin debugger to export a skin mod json file
//   that we could detect and load after the skin to apply the changes
//   this menu could allow toggling such files, resetting them, listing the changes
