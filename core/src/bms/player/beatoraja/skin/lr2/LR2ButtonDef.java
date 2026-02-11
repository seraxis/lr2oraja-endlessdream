package bms.player.beatoraja.skin.lr2;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * LR2 button types
 */
public enum LR2ButtonDef {
	// Panel button
	Panel1("Open Panel 1", 1),
	Panel2("Open Panel 2", 2),
	Panel3("Open Panel 3", 3),
	Panel4("Open Panel 4", 4),
	Panel5("Open Panel 5", 5),
	Panel6("Open Panel 6", 6),
	Panel7("Open Panel 7", 7),
	Panel8("Open Panel 8", 8),
	Panel9("Open Panel 9", 9),

	// Song Select
	DifficultyFilter("Difficulty filter", 10),
	KeyModeFilter("Key mode filter", 11),
	ChartSorting("Chart sorting", 12),
	KeyConfig("Key Config", 13),
	SkinSelect("Skin Select", 14),
	Start("Start", 15),
	Autoplay("Autoplay", 16),
	OpenREADME("Open README", 17),
	ResetTag("Reset Tag", 18),
	Replay("Replay", 19),

	// FX Options
	FX0Mode("FX0 mode", 20),
	FX1Mode("FX1 mode", 21),
	FX2Mode("FX2 mode", 22),
	FX0Toggle("FX0 toggle", 23),
	FX1Toggle("FX1 toggle", 24),
	FX2Toggle("FX2 toggle", 25),
	FX0Target("FX0 target", 26),
	FX1Target("FX1 target", 27),
	FX2Target("FX2 target", 28),
	Equalizer("Equalizer", 29),
	EqualizerPreset("Equalizer preset (Doesn't work in LV)", 30),
	AllowVolumeControl("Allow volume control", 31),
	PITCHToggle("PITCH toggle", 32),
	PITCHType("PITCH type", 33),

	// Play Options
	Gauge1P("Gauge 1P", 40),
	Gauge2P("Gauge 2P", 41),
	Lane1P("Lane 1P", 42),
	Lane2P("Lane 2P", 43),
	AutoScratch1P("Auto-Scratch 1P", 44),
	AutoScratch2P("Auto-Scratch 2P", 45),
	Shutters("Shutters (lane cover?)", 46),
	OnePGreenNumber("1P Green number", 47),
	TwoPGreenNumber("2P Green number", 48),
	OnePLaneEffect("1P Lane Effect", 50),
	TwoPLaneEffect("2P Lane Effect", 51),
	DPFlip("DP Flip", 54),
	HiSpeedFix("Hi-Speed fix", 55),
	Battle("Battle", 56),
	HiSpeed1PChange("Hi-Speed 1P change", 57),
	HiSpeed2PChange("Hi-Speed 2P change", 58),

	// Options screen
	ScoreGraph("Score graph", 70),
	GhostPosition("Ghost position", 71),
	BGA("BGA", 72),
	BGASize("BGA size", 73),
	TimingOffsetChange("Timing offset change", 74),
	TimingOffsetAutoAdjust("Timing offset Auto-adjust", 75),
	DefaultTargetRate("Default target rate", 76),
	Target("Target", 77),
	ScreenModeLR2Only("Screen mode (LR2 only)", 80),
	ColorMode("Color mode", 81),
	Vsync("Vsync", 82),
	ReplaySavePriority("Replay save priority", 83),
	FavoriteOptions("Add favorite/Add ignore/Remove favorite/Remove ignore", 90),
	AllDifficultyFilter("All difficulty filter", 91),
	BeginnerFilter("Beginner filter", 92),
	NormalFilter("Normal filter", 93),
	HyperFilter("Hyper filter", 94),
	AnotherFilter("Another filter", 95),
	InsaneFilter("Insane filter", 96),

	// Key Config
	Key1_1P("1P Key 1", 101),
	Key2_1P("1P Key 2", 102),
	Key3_1P("1P Key 3", 103),
	Key4_1P("1P Key 4", 104),
	Key5_1P("1P Key 5", 105),
	Key6_1P("1P Key 6", 106),
	Key7_1P("1P Key 7", 107),
	Key8_1P("1P Key 8", 108),
	Key9_1P("1P Key 9", 109),
	ScratchUp_1P("1P Scratch Up", 110),
	ScratchDown_1P("1P Scratch Down", 111),
	Start_1P("1P Start", 112),
	Select_1P("1P Select", 113),
	ScratchAbsolute_1P("1P Scratch Absolute", 116),
	Key1_2P("2P Key 1", 121),
	Key2_2P("2P Key 2", 122),
	Key3_2P("2P Key 3", 123),
	Key4_2P("2P Key 4", 124),
	Key5_2P("2P Key 5", 125),
	Key6_2P("2P Key 6", 126),
	Key7_2P("2P Key 7", 127),
	Key8_2P("2P Key 8", 128),
	Key9_2P("2P Key 9", 129),
	ScratchUp_2P("2P Scratch Up", 130),
	ScratchDown_2P("2P Scratch Down", 131),
	Start_2P("2P Start", 132),
	Select_2P("2P Select", 133),
	ScratchAbsolute_2P("2P Scratch Absolute", 136),
	KeyChange7Keys("Key change (7 Keys)", 140),
	KeyChange9Keys("Key change (9 Keys)", 141),
	KeyChange5Keys("Key change (5 Keys)", 142),
	KeyModeSwitch("Keymode switch", 143),
	KeyConfigSlot1("Key Config slot 1", 150),
	KeyConfigSlot2("Key Config slot 2", 151),
	KeyConfigSlot3("Key Config slot 3", 152),
	KeyConfigSlot4("Key Config slot 4", 153),
	KeyConfigSlot5("Key Config slot 5", 154),
	KeyConfigSlot6("Key Config slot 6", 155),
	KeyConfigSlot7("Key Config slot 7", 156),
	KeyConfigSlot8("Key Config slot 8", 157),

	// Skin Select
	SkinSelect7Keys("Skin Select 7 Keys", 170),
	SkinSelect5Keys("Skin Select 5 Keys", 171),
	SkinSelect14Keys("Skin Select 14 Keys", 172),
	SkinSelect10Keys("Skin Select 10 Keys", 173),
	SkinSelect9Keys("Skin Select 9 Keys", 174),
	SkinSelectSelect("Skin Select Select", 175),
	SkinSelectDecide("Skin Select Decide", 176),
	SkinSelectResult("Skin Select Result", 177),
	SkinSelectKeyConfig("Skin Select Key Config", 178),
	SkinSelectSkinSelect("Skin Select Skin Select", 179),
	SkinSelectSoundSet("Skin Select Sound set", 180),
	SkinSelectTheme("Skin Select Theme", 181),
	SkinSelect7KeysBattle("Skin Select 7 Keys Battle", 182),
	SkinSelect5KeysBattle("Skin Select 5 Keys Battle", 183),
	SkinSelect9KeysBattle("Skin Select 9 Keys Battle", 184),
	SkinSelectCourseResults("Skin Select Course Results", 185),
	SwitchBetweenSkinsInSkinSelect("Switch between skins in Skin Select", 190),

	// Help file (Must be defined with #HELPFILE)
	HelpFile1("Help file 1", 200),
	HelpFile2("Help file 2", 201),
	HelpFile3("Help file 3", 202),
	HelpFile4("Help file 4", 203),
	HelpFile5("Help file 5", 204),
	HelpFile6("Help file 6", 205),
	HelpFile7("Help file 7", 206),
	HelpFile8("Help file 8", 207),
	HelpFile9("Help file 9", 208),
	HelpFile10("Help file 10", 209),
	IRChartPageOpen("IR chart page open", 210),

	// Skin Customization
	SkinCustomizationItem1("Skin Customization Item 1", 220),
	SkinCustomizationItem2("Skin Customization Item 2", 221),
	SkinCustomizationItem3("Skin Customization Item 3", 222),
	SkinCustomizationItem4("Skin Customization Item 4", 223),
	SkinCustomizationItem5("Skin Customization Item 5", 224),
	SkinCustomizationItem6("Skin Customization Item 6", 225),
	SkinCustomizationItem7("Skin Customization Item 7", 226),
	SkinCustomizationItem8("Skin Customization Item 8", 227),
	SkinCustomizationItem9("Skin Customization Item 9", 228),
	SkinCustomizationItem10("Skin Customization Item 10", 229),

	// Course Select
	CourseSelectDecide("Course Select Decide", 230),
	CourseSelectCancel("Course Select Cancel", 231),
	CourseSelectCourseEdit("Course Select Course Edit", 232),
	CourseSelectDelete("Course Select Delete", 233),

	// Course Option
	CourseOptionStage1To2Transition("Course Option Stage 1-2 transition", 240),
	CourseOptionStage2To3Transition("Course Option Stage 2-3 transition", 241),
	CourseOptionStage3To4Transition("Course Option Stage 3-4 transition", 242),
	CourseOptionStage4To5Transition("Course Option Stage 4-5 transition", 243),
	CourseOptionStage5To6Transition("Course Option Stage 5-6 transition", 244),
	CourseOptionStage6To7Transition("Course Option Stage 6-7 transition", 245),
	CourseOptionStage7To8Transition("Course Option Stage 7-8 transition", 246),
	CourseOptionStage8To9Transition("Course Option Stage 8-9 transition", 247),
	CourseOptionStage9To10Transition("Course Option Stage 9-10 transition", 248),
	CourseOptionStage10To11Transition("Course Option Stage 10-11 transition", 249),
	CourseSoflanToggle("Course Soflan toggle", 250),
	CourseGaugeToggle("Course gauge toggle", 251),
	CourseOptionToggle("Course option toggle", 252),
	CourseIRAvailabilityToggle("Course IR availability toggle", 253),

	// Random Course
	RandomCourseOptimalLevel("Random Course optimal level", 260),
	RandomCourseMaximumLevel("Random Course maximum level", 261),
	RandomCourseMinimumLevel("Random Course minimum level", 262),
	RandomCourseBPMChange("Random Course BPM change", 263),
	RandomCourseMaxBPM("Random Course max BPM", 264),
	RandomCourseMinBPM("Random Course min BPM", 265),
	RandomCourseStageCount("Random Course Stage count", 266),
	CourseDefaultTransitionChange("Course Default transition change", 268),
	CourseDefaultGaugeChange("Course Default gauge change", 269),

	// Clear Type
	OnePClearType("1P Clear Type", 270),
	TwoPClearType("2P Clear Type", 271),

	// ARENA Clear Type
	Arena1PClearType("ARENA 1P Clear Type", 301),
	Arena2PClearType("ARENA 2P Clear Type", 302),
	Arena3PClearType("ARENA 3P Clear Type", 303),
	Arena4PClearType("ARENA 4P Clear Type", 304),
	Arena5PClearType("ARENA 5P Clear Type", 305),
	Arena6PClearType("ARENA 6P Clear Type", 306),
	Arena7PClearType("ARENA 7P Clear Type", 307),
	Arena8PClearType("ARENA 8P Clear Type", 308),

	// ARENA player ranking
	ArenaPlayerRanking("ARENA player ranking", 310),
	Arena1PRanking("ARENA 1P ranking", 311),
	Arena2PRanking("ARENA 2P ranking", 312),
	Arena3PRanking("ARENA 3P ranking", 313),
	Arena4PRanking("ARENA 4P ranking", 314),
	Arena5PRanking("ARENA 5P ranking", 315),
	Arena6PRanking("ARENA 6P ranking", 316),
	Arena7PRanking("ARENA 7P ranking", 317),
	Arena8PRanking("ARENA 8P ranking", 318);

	private final String name;
	private final int value;

	LR2ButtonDef(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}

	@Nullable
	public static LR2ButtonDef valueOf(int v) {
		return Arrays.stream(LR2ButtonDef.values()).filter(def -> def.value == v).findAny().orElse(null);
	}
}
