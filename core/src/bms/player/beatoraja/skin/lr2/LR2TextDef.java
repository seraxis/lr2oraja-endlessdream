package bms.player.beatoraja.skin.lr2;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * text st definitions
 */
public enum LR2TextDef {
	Rival("Target/Rival Name", 1),
	Player("Player Name", 2),
	CurrentSongTitle("Current Song Title", 10),
	CurrentSongSubTitle("Current Song Sub Title", 11),
	CurrentSongFullTitle("Current Song Full Title", 12),
	CurrentSongGenre("Current Song Genre", 13),
	CurrentSongArtist("Current Song Artist", 14),
	CurrentSongSubArtist("Current Song Subartist", 15),
	CurrentSongSearchText("Current Song Search Text", 16),
	CurrentSongPlayLevel("Current Song Play Level", 17),
	CurrentSongDifficultyNumber("Current Song Difficulty Number", 18),

	// For editing
	EditingCurrentSongTitle("Editing Current Song Title", 20),
	EditingCurrentSongSubTitle("Editing Current Song Sub Title", 21),
	EditingCurrentSongFullTitle("Editing Current Song Full Title", 22),
	EditingCurrentSongGenre("Editing Current Song Genre", 23),
	EditingCurrentSongArtist("Editing Current Song Artist", 24),
	EditingCurrentSongSubartist("Editing Current Song Subartist", 25),
	EditingCurrentSongSearchQuery("Editing Current Song Search query", 26),
	EditingCurrentSongPlayLevel("Editing Current Song play level", 27),
	EditingCurrentSongDifficultyNumber("Editing Current Song Difficulty Number", 28),
	EditingCurrentSongInsaneLevel("Editing Current Song Insane level", 29),
	EditingSearchTitleCurrentFolder("Search Title/Current Folder", 30),

	KeybindSlot1("Keybind slot 1", 40),
	KeybindSlot2("Keybind slot 2", 41),
	KeybindSlot3("Keybind slot 3", 42),
	KeybindSlot4("Keybind slot 4", 43),
	KeybindSlot5("Keybind slot 5", 44),
	KeybindSlot6("Keybind slot 6", 45),
	KeybindSlot7("Keybind slot 7", 46),
	KeybindSlot8("Keybind slot 8", 47),

	SkinName("Skin name", 50),
	SkinAuthor("Skin author", 51),

	KeyMode("Key mode", 60),
	SongSort("Song sort", 61),
	Difficulty("Difficulty", 62),
	Option1P("Option 1P", 63),
	Option2P("Option 2P", 64),
	Gauge1P("Gauge 1P", 65),
	Gauge2P("Gauge 1P", 66),
	AutoScratch1P("Auto Scratch 1P", 67),
	AutoScratch2P("Auto Scratch 2P", 68),
	Battle("Battle", 69),
	DPFlip("DP Flip", 70),
	ScoreGraph("Score graph", 71),
	Ghost("Ghost", 72),
	LaneCover("Lane cover", 73),
	HiSpeedFix("Hi-speed fix", 74),
	BGASize("BGA size", 75),
	BGADisplay("BGA display", 76),
	ColorMode("Color mode", 77),
	Vsync("Vsync", 78),
	ScreenMode("Screen mode", 79),
	JudgeAutoAdjust("Judge Auto Adjust", 80),
	ReplaySave("Replay save", 81),
	TrialLine1("Trial Line 1", 82),
	TrialLine2("Trial Line 2", 83),
	FX1P("FX 1P", 84),
	FX2P("FX 2P", 85),

	FirstSkinCustomizationOption("1st skin customization option", 100),
	SecondSkinCustomizationOption("2nd skin customization option", 101),
	ThirdSkinCustomizationOption("3rd skin customization option", 102),
	FourthSkinCustomizationOption("4th skin customization option", 103),
	FifthSkinCustomizationOption("5th skin customization option", 104),
	SixthSkinCustomizationOption("6th skin customization option", 105),
	SeventhSkinCustomizationOption("7th skin customization option", 106),
	EighthSkinCustomizationOption("8th skin customization option", 107),
	NinthSkinCustomizationOption("9th skin customization option", 108),
	TenthSkinCustomizationOption("10th skin customization option", 109),

	FirstSkinCustomizationCurrentOption("1st skin customization current option", 110),
	SecondSkinCustomizationCurrentOption("2nd skin customization current option", 111),
	ThirdSkinCustomizationCurrentOption("3rd skin customization current option", 112),
	FourthSkinCustomizationCurrentOption("4th skin customization current option", 113),
	FifthSkinCustomizationCurrentOption("5th skin customization current option", 114),
	SixthSkinCustomizationCurrentOption("6th skin customization current option", 115),
	SeventhSkinCustomizationCurrentOption("7th skin customization current option", 116),
	EighthSkinCustomizationCurrentOption("8th skin customization current option", 117),
	NinthSkinCustomizationCurrentOption("9th skin customization current option", 118),
	TenthSkinCustomizationCurrentOption("10th skin customization current option", 119),

	Leaderboard1stPlayerName("Leaderboard 1st player name", 120),
	Leaderboard2ndPlayerName("Leaderboard 2nd player name", 121),
	Leaderboard3rdPlayerName("Leaderboard 3rd player name", 122),
	Leaderboard4thPlayerName("Leaderboard 4th player name", 123),
	Leaderboard5thPlayerName("Leaderboard 5th player name", 124),
	Leaderboard6thPlayerName("Leaderboard 6th player name", 125),
	Leaderboard7thPlayerName("Leaderboard 7th player name", 126),
	Leaderboard8thPlayerName("Leaderboard 8th player name", 127),
	Leaderboard9thPlayerName("Leaderboard 9th player name", 128),
	Leaderboard10thPlayerName("Leaderboard 10th player name", 129),

	// Courses
	Course1stStageTitle("Course 1st stage Title", 150),
	Course2ndStageTitle("Course 2nd stage Title", 151),
	Course3rdStageTitle("Course 3rd stage Title", 152),
	Course4thStageTitle("Course 4th stage Title", 153),
	Course5thStageTitle("Course 5th stage Title", 154),
	Course6thStageTitle("Course 6th stage Title", 155),
	Course7thStageTitle("Course 7th stage Title", 156),
	Course8thStageTitle("Course 8th stage Title", 157),
	Course9thStageTitle("Course 9th stage Title", 158),
	Course10thStageTitle("Course 10th stage Title", 159),

	Course1stStageSubtitle("Course 1st stage Subtitle", 160),
	Course2ndStageSubtitle("Course 2nd stage Subtitle", 161),
	Course3rdStageSubtitle("Course 3rd stage Subtitle", 162),
	Course4thStageSubtitle("Course 4th stage Subtitle", 163),
	Course5thStageSubtitle("Course 5th stage Subtitle", 164),
	Course6thStageSubtitle("Course 6th stage Subtitle", 165),
	Course7thStageSubtitle("Course 7th stage Subtitle", 166),
	Course8thStageSubtitle("Course 8th stage Subtitle", 167),
	Course9thStageSubtitle("Course 9th stage Subtitle", 168),
	Course10thStageSubtitle("Course 10th stage Subtitle", 169),

	CourseName("Course name", 170),

	CourseStage1To2Transition("Course stage 1-2 transition", 171),
	CourseStage2To3Transition("Course stage 2-3 transition", 172),
	CourseStage3To4Transition("Course stage 3-4 transition", 173),
	CourseStage4To5Transition("Course stage 4-5 transition", 174),
	CourseStage5To6Transition("Course stage 5-6 transition", 175),
	CourseStage6To7Transition("Course stage 6-7 transition", 176),
	CourseStage7To8Transition("Course stage 7-8 transition", 177),
	CourseStage8To9Transition("Course stage 8-9 transition", 178),
	CourseStage9To10Transition("Course stage 9-10 transition", 179),

	CourseSoflan("Course Soflan", 180),
	CourseGaugeType("Course gauge type", 181),
	CourseBannedOptions("Course banned options", 182),
	CourseIR("Course IR", 183),

	RandomCourseOptimalLevel("Random Course optimal level", 190),
	RandomCourseMaxLevel("Random Course Max level", 191),
	RandomCourseMinLevel("Random Course Min level", 192),
	RandomCourseBPMRange("Random Course BPM range", 193),
	RandomCourseMaxBPM("Random Course Max BPM", 194),
	RandomCourseMinBPM("Random Course Min BPM", 195),
	RandomCourseStageCount("Random Course Stage count", 196),
	DefaultCourseTransition("Default Course transition", 198),
	DefaultCourseGauge("Default Course gauge", 199),

	// Arena Extension
	ArenaStatus("ARENA status", 260),
	Arena1PName("ARENA 1P name", 261),
	Arena2PName("ARENA 2P name", 262),
	Arena3PName("ARENA 3P name", 263),
	Arena4PName("ARENA 4P name", 264),
	Arena5PName("ARENA 5P name", 265),
	Arena6PName("ARENA 6P name", 266),
	Arena7PName("ARENA 7P name", 267),
	Arena8PName("ARENA 8P name", 268),

	PlayerModifier("Player modifier", 270),
	Arena1PModifiers("ARENA 1P modifiers", 271),
	Arena2PModifiers("ARENA 2P modifiers", 272),
	Arena3PModifiers("ARENA 3P modifiers", 273),
	Arena4PModifiers("ARENA 4P modifiers", 274),
	Arena5PModifiers("ARENA 5P modifiers", 275),
	Arena6PModifiers("ARENA 6P modifiers", 276),
	Arena7PModifiers("ARENA 7P modifiers", 277),
	Arena8PModifiers("ARENA 8P modifiers", 278),

	PlayerModifierShort("Player modifier short", 280),
	Arena1PModifiersShort("ARENA 1P modifiers short", 281),
	Arena2PModifiersShort("ARENA 2P modifiers short", 282),
	Arena3PModifiersShort("ARENA 3P modifiers short", 283),
	Arena4PModifiersShort("ARENA 4P modifiers short", 284),
	Arena5PModifiersShort("ARENA 5P modifiers short", 285),
	Arena6PModifiersShort("ARENA 6P modifiers short", 286),
	Arena7PModifiersShort("ARENA 7P modifiers short", 287),
	Arena8PModifiersShort("ARENA 8P modifiers short", 288);

	private final String name;
	private final int value;

	LR2TextDef(String name, int value) {
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
	public static LR2TextDef valueOf(int v) {
		return Arrays.stream(LR2TextDef.values()).filter(def -> def.value == v).findAny().orElse(null);
	}
}
