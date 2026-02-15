package bms.player.beatoraja.skin.lr2;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * LR2 destination options
 */
public enum LR2DestinationOptions {
	// DST op values
	AlwaysTrue("Always true", 0),
	BarFocusedIsFolder("Bar focused is a folder", 1),
	BarFocusedIsSong("Bar focused is a song", 2),
	BarFocusedIsCourse("Bar focused is a course", 3),
	BarFocusedIsNewCourse("Bar focused is a new course", 4),
	BarFocusedIsPlayable("Bar focused is playable", 5),

	CurrentModeIsDoubleOrDoubleBattle("Current mode is Double or Double Battle", 10),
	CurrentModeIsBattle("Current mode is Battle", 11),
	CurrentModeIsDoubleOrBattleOrDoubleBattle("Current mode is Double or Battle or Double Battle", 12),
	CurrentModeIsGhostBattleOrBattle("Current mode is Ghost Battle or Battle", 13),

	AllPanelsOff("All panels off", 20),
	Panel1On("Panel 1 on", 21),
	Panel2On("Panel 2 on", 22),
	Panel3On("Panel 3 on", 23),
	Panel4On("Panel 4 on", 24),
	Panel5On("Panel 5 on", 25),
	Panel6On("Panel 6 on", 26),
	Panel7On("Panel 7 on", 27),
	Panel8On("Panel 8 on", 28),
	Panel9On("Panel 9 on", 29),

	BGANormal("BGA Normal", 30),
	BGAExtend("BGA extend", 31),
	AutoplayOff("Autoplay off", 32),
	AutoplayOn("Autoplay on", 33),
	GhostOff("Ghost off", 34),
	GhostTypeA("Ghost Type A (technically used for Fast/Slow)", 35),
	GhostTypeB("Ghost Type B", 36),
	GhostTypeC("Ghost Type C", 37),
	ScoreGraphOff("Score Graph off", 38),
	ScoreGraphOn("Score Graph on", 39),
	BGAOff("BGA off", 40),
	BGAOn("BGA on", 41),

	HardOff_1P("1P Hard Off (normal gauge)", 42),
	HardOn_1P("1P Hard On (hard gauge)", 43),
	HardOff_2P("2P Hard Off (normal gauge)", 44),
	HardOn_2P("2P Hard On (hard gauge)", 45),

	DifficultyFilterOn("Difficulty filter on", 46),
	DifficultyFilterOff("Difficulty filter off", 47),

	EXHARD_1P("1P EX-HARD", 48),
	EXHARD_2P("2P EX-HARD", 49),

	LR2IROffline("LR2IR offline", 50),
	LR2IROnline("LR2IR online", 51),

	ExtraModeOff("Extra Mode off", 52),
	ExtraModeOn("Extra Mode on", 53),

	AutoScratchOff_1P("1P Auto-scratch off", 54),
	AutoScratchOn_1P("1P Auto-scratch on", 55),
	AutoScratchOff_2P("2P Auto-scratch off", 56),
	AutoScratchOn_2P("2P Auto-scratch on", 57),

	ScoreSavingNotAllowed("Score saving not allowed", 60),
	ScoreSavingAllowed("Score saving allowed", 61),
	SavesAsFailedOrNoPlay("Saves as failed or no play (Clear saving not allowed)", 62),
	SelectedAssistEasyEasyGauge("Selected Assist Easy/Easy gauge (saves as Easy)", 63),
	SelectedNormalGauge("Selected Normal gauge (saves as Clear)", 64),
	SelectedHardExHardGATKGauge("Selected Hard/Ex-Hard/G-ATK gauge (saves as Hard Clear)", 65),
	SelectedDeathPATKGauge("Selected Death/P-ATK gauge (saves as Full Combo)", 66),

	// Max level value: Lv.9 for 5/10 Keys, Lv.12 for 7/14 Keys, Lv.42 for 9 Keys
	BeginnerLevelDoesNotExceedMax("The level of the beginner chart in the same folder doesn't exceed the max value", 70),
	NormalLevelDoesNotExceedMax("Normal", 71),
	HyperLevelDoesNotExceedMax("Hyper", 72),
	AnotherLevelDoesNotExceedMax("Another", 73),
	InsaneLevelDoesNotExceedMax("Insane", 74),
	BeginnerLevelExceedsMax("The level of the beginner chart in the same folder exceeds the max value", 75),
	NormalLevelExceedsMax("Normal", 76),
	HyperLevelExceedsMax("Hyper", 77),
	AnotherLevelExceedsMax("Another", 78),
	InsaneLevelExceedsMax("Insane", 79),

	LoadingInProgress("Loading in progress", 80),
	LoadingCompleted("Loading completed", 81),

	ReplaySavingOff("Replay saving off", 82),
	ReplaySavingOn("Replay saving on", 83),
	PlayingReplay("Playing Replay", 84),

	ResultClear("Result Clear", 90),
	ResultsFailed("Results Failed", 91),

	// Song select
	NoPlay("No Play", 100),
	Failed("Failed", 101),
	EasyClear("Easy Clear", 102),
	Clear("Clear", 103),
	HardClear("Hard Clear", 104),
	FullCombo("Full Combo", 105),
	ExHardClear("Ex-Hard Clear", 106),
	Perfect("Perfect", 107),
	MAX("MAX", 108),
	AssistEasy("Assist Easy", 109),

	AAA("AAA", 110),
	AA("AA", 111),
	A("A", 112),
	B("B", 113),
	C("C", 114),
	D("D", 115),
	E("E", 116),
	F("F", 117),

	// Cleared gauge flags
	GrooveCleared("Groove", 118),
	HardCleared("Hard", 119),
	HazardCleared("Hazard", 120),
	EasyCleared("Easy", 121),
	PAttackCleared("P-Attack", 122),
	GAttackCleared("G-Attack", 123),

	// Cleared lane flags
	NormalCleared("Normal", 126),
	MirrorCleared("Mirror", 127),
	RandomCleared("Random", 128),
	SRandomCleared("S-Random", 129),
	HRandomCleared("H-Random (Scatter)", 130),
	AllScratchCleared("All Scratch (Converge)", 131),

	// Cleared effect flags
	NoneEffect("None", 134),
	HiddenEffect("Hidden", 135),
	SuddenEffect("Sudden", 136),
	HiddenSuddenEffect("Hidden + Sudden", 137),

	// Other cleared flags
	AutoScratchCleared("Auto-scratch (Disappears if you have cleared without it)", 142),
	ExtraModeCleared("Extra Mode", 143),
	DoubleBattleCleared("Double Battle", 144),
	SPToDPCleared("SP to DP (Might be shared with DP to SP and 9 to 7?)", 145),

	EasyClearFlag("Easy Clear", 146),
	HardClearFlag("Hard Clear", 147),
	FullComboFlag("Full Combo", 148),

	Undefined("Undefined", 150),
	Beginner("Beginner", 151),
	Normal("Normal", 152),
	Hyper("Hyper", 153),
	Another("Another", 154),
	Insane("Insane", 155),

	// Original keycount
	Original7Keys("7 Keys", 160),
	Original5Keys("5 Keys", 161),
	Original14Keys("14 Keys", 162),
	Original10Keys("10 Keys", 163),
	Original9Keys("9 Keys", 164),

	// Keycount after modifications
	Modified7Keys("7 Keys", 165),
	Modified5Keys("5 Keys", 166),
	Modified14Keys("14 Keys", 167),
	Modified10Keys("10 Keys", 168),
	Modified9Keys("9 Keys", 169),

	NoBGA("No BGA", 170),
	BGAAvailable("BGA available", 171),

	NoLN("No LN", 172),
	HasLN("Has LN", 173),

	NoREADME("No README", 174),
	HasREADME("README", 175),

	NoBPMChanges("No BPM changes", 176),
	HasBPMChanges("Has BPM changes", 177),

	NoRANDOM("No #RANDOM", 178),
	HasRANDOM("Has #RANDOM", 179),

	JudgeRankVeryHard("Judge rank Very Hard", 180),
	JudgeRankHard("Judge rank Hard", 181),
	JudgeRankNormal("Judge rank Normal", 182),
	JudgeRankEasy("Judge rank Easy", 183),

	PlayLevelDoesNotExceedLimits("Play level of the current chart doesn't exceed limits (Check above)", 185),
	PlayLevelExceedsLimits("Play level of the current chart exceed limits", 186),

	NoSTAGEFILE("No STAGEFILE", 190),
	HasSTAGEFILE("Has STAGEFILE", 191),
	NoBANNER("No BANNER", 192),
	HasBANNER("Has BANNER", 193),
	NoBACKBMP("No BACKBMP", 194),
	HasBACKBMP("Has BACKBMP", 195),

	NoReplayFile("No replay file", 196),
	HasReplayFile("Has replay file", 197),

	// Play skin
	AAA_1P("1P AAA", 200),
	AA_1P("1P AA", 201),
	A_1P("1P A", 202),
	B_1P("1P B", 203),
	C_1P("1P C", 204),
	D_1P("1P D", 205),
	E_1P("1P E", 206),
	F_1P("1P F", 207),

	AAA_2P("2P AAA", 210),
	AA_2P("2P AA", 211),
	A_2P("2P A", 212),
	B_2P("2P B", 213),
	C_2P("2P C", 214),
	D_2P("2P D", 215),
	E_2P("2P E", 216),
	F_2P("2P F", 217),

	AAAReached("AAA reached", 220),
	AAReached("AA reached", 221),
	AReached("A reached", 222),
	BReached("B reached", 223),
	CReached("C reached", 224),
	DReached("D reached", 225),
	EReached("E reached", 226),
	FReached("F reached", 227),

	GaugeX_1P("1P gauge x%", 230),
	Gauge1X_1P("1P gauge 1x%", 231),
	Gauge2X_1P("1P gauge 2x%", 232),
	Gauge3X_1P("1P gauge 3x%", 233),
	Gauge4X_1P("1P gauge 4x%", 234),
	Gauge5X_1P("1P gauge 5x%", 235),
	Gauge6X_1P("1P gauge 6x%", 236),
	Gauge7X_1P("1P gauge 7x%", 237),
	Gauge8X_1P("1P gauge 8x%", 238),
	Gauge9X_1P("1P gauge 9x%", 239),
	Gauge100_1P("1P gauge 100%", 240),

	PGREAT_1P("1P PGREAT", 241),
	GREAT_1P("1P GREAT", 242),
	GOOD_1P("1P GOOD", 243),
	BAD_1P("1P BAD", 244),
	POOR_1P("1P POOR", 245),
	EmptyPOOR_1P("1P Empty POOR", 246),

	POORBGALayerEnd_1P("1P POORBGA layer end", 247),
	POORBGALayerStart_1P("1P POORBGA layer start", 248),

	GaugeX_2P("2P gauge x%", 250),
	Gauge1X_2P("2P gauge 1x%", 251),
	Gauge2X_2P("2P gauge 2x%", 252),
	Gauge3X_2P("2P gauge 3x%", 253),
	Gauge4X_2P("2P gauge 4x%", 254),
	Gauge5X_2P("2P gauge 5x%", 255),
	Gauge6X_2P("2P gauge 6x%", 256),
	Gauge7X_2P("2P gauge 7x%", 257),
	Gauge8X_2P("2P gauge 8x%", 258),
	Gauge9X_2P("2P gauge 9x%", 259),
	Gauge100_2P("2P gauge 100%", 260),

	PGREAT_2P("2P PGREAT", 261),
	GREAT_2P("2P GREAT", 262),
	GOOD_2P("2P GOOD", 263),
	BAD_2P("2P BAD", 264),
	POOR_2P("2P POOR", 265),
	EmptyPOOR_2P("2P Empty POOR", 266),

	POORBGALayerEnd_2P("2P POORBGA layer end", 267),
	POORBGALayerStart_2P("2P POORBGA layer start", 268),

	ChangingSUD_1P("1P changing SUD+", 270),
	ChangingSUD_2P("2P changing SUD+", 271),
	ChangingHiSpeed_1P("1P changing Hi-speed", 272),
	ChangingHiSpeed_2P("2P changing Hi-speed", 273),
	UsesSUD_1P("1P uses SUD+", 274),
	UsesSUD_2P("2P uses SUD+", 275),
	UsesHIDOrLIFT_1P("1P uses HID+/LIFT", 276),
	UsesHIDOrLIFT_2P("2P uses HID+/LIFT", 277),

	CourseStage1("Course Stage 1", 280),
	CourseStage2("Course Stage 2", 281),
	CourseStage3("Course Stage 3", 282),
	CourseStage4("Course Stage 4", 283),
	CourseFinalStage("Course Final Stage", 289),

	Course("Course", 290),
	Nonstop("Nonstop", 291),
	Expert("Expert", 292),
	Dan("Dan", 293),

	// Results
	ResultAAA_1P("1P AAA", 300),
	ResultAA_1P("1P AA", 301),
	ResultA_1P("1P A", 302),
	ResultB_1P("1P B", 303),
	ResultC_1P("1P C", 304),
	ResultD_1P("1P D", 305),
	ResultE_1P("1P E", 306),
	ResultF_1P("1P F", 307),
	Result0Percent_1P("1P 0%", 308),

	ResultAAA_2P("2P AAA", 310),
	ResultAA_2P("2P AA", 311),
	ResultA_2P("2P A", 312),
	ResultB_2P("2P B", 313),
	ResultC_2P("2P C", 314),
	ResultD_2P("2P D", 315),
	ResultE_2P("2P E", 316),
	ResultF_2P("2P F", 317),
	Result0Percent_2P("2P 0%", 318),

	AAABeforeUpdate("AAA before Update", 320),
	AABeforeUpdate("AA before Update", 321),
	ABeforeUpdate("A before Update", 322),
	BBeforeUpdate("B before Update", 323),
	CBeforeUpdate("C before Update", 324),
	DBeforeUpdate("D before Update", 325),
	EBeforeUpdate("E before Update", 326),
	FBeforeUpdate("F before Update", 327),

	ScoreUpdated("Score updated", 330),
	MaxComboUpdated("Max Combo updated", 331),
	BadPoorUpdated("Bad+Poor updated", 332),
	TrialUpdated("Trial updated", 333),
	IRRankingUpdated("IR ranking updated", 334),
	ScoreRankingUpdated("Score ranking updated", 335),

	AfterUpdateAAA("After update AAA", 340),
	AfterUpdateAA("After update AA", 341),
	AfterUpdateA("After update A", 342),
	AfterUpdateB("After update B", 343),
	AfterUpdateC("After update C", 344),
	AfterUpdateD("After update D", 345),
	AfterUpdateE("After update E", 346),
	AfterUpdateF("After update F", 347),

	ResultFlipDisabled("Result flip disabled (no #FLIPRESULT or has #DISABLEFLIP)", 350),
	ResultFlipEnabled("Result flip eabled (has #FLIPRESULT)", 351),

	Win_1P_Lose_2P("1P Win, 2P Lose", 352),
	Win_2P_Lose_1P("2P Win, 1P Lose", 353),
	Draw("Draw", 354),

	// Key Config
	Keys_7_14("7/14 Keys", 400),
	Keys_9("9 Keys", 401),
	Keys_5_10("5/10 Keys", 402),

	// Misc
	BeginnerChartsNotExist("Beginner charts don't exist in folder", 500),
	NormalChartsNotExist("Normal charts don't exist in folder", 501),
	HyperChartsNotExist("Hyper charts don't exist in folder", 502),
	AnotherChartsNotExist("Another charts don't exist in folder", 503),
	InsaneChartsNotExist("Insane charts don't exist in folder", 504),

	BeginnerChartsExist("Beginner charts exist in folder", 505),
	NormalChartsExist("Normal charts exist in folder", 506),
	HyperChartsExist("Hyper charts exist in folder", 507),
	AnotherChartsExist("Another charts exist in folder", 508),
	InsaneChartsExist("Insane charts exist in folder", 509),

	Exactly1BeginnerChart("Exactly 1 Beginner chart exist in folder", 510),
	Exactly1NormalChart("Exactly 1 Normal chart exist in folder", 511),
	Exactly1HyperChart("Exactly 1 Hyper chart exist in folder", 512),
	Exactly1AnotherChart("Exactly 1 Another chart exist in folder", 513),
	Exactly1InsaneChart("Exactly 1 Insane chart exist in folder", 514),

	MultipleBeginnerCharts("Multiple Beginner charts exist in folder", 515),
	MultipleNormalCharts("Multiple Normal charts exist in folder", 516),
	MultipleHyperCharts("Multiple Hyper charts exist in folder", 517),
	MultipleAnotherCharts("Multiple Another charts exist in folder", 518),
	MultipleInsaneCharts("Multiple Insane charts exist in folder", 519),

	// Doesn't work in LV/LVF.
	LevelBarBeginnerNoPlay("Level bar Beginner No Play", 520),
	LevelBarBeginnerFailed("Level bar Beginner Failed", 521),
	LevelBarBeginnerEasyClear("Level bar Beginner Easy Clear", 522),
	LevelBarBeginnerClear("Level bar Beginner Clear", 523),
	LevelBarBeginnerHardClear("Level bar Beginner Hard Clear", 524),
	LevelBarBeginnerFullCombo("Level bar Beginner Full Combo", 525),

	LevelBarNormalNoPlay("Level bar Normal No Play", 530),
	LevelBarNormalFailed("Level bar Normal Failed", 531),
	LevelBarNormalEasyClear("Level bar Normal Easy Clear", 532),
	LevelBarNormalClear("Level bar Normal Clear", 533),
	LevelBarNormalHardClear("Level bar Normal Hard Clear", 534),
	LevelBarNormalFullCombo("Level bar Normal Full Combo", 535),

	LevelBarHyperNoPlay("Level bar Hyper No Play", 540),
	LevelBarHyperFailed("Level bar Hyper Failed", 541),
	LevelBarHyperEasyClear("Level bar Hyper Easy Clear", 542),
	LevelBarHyperClear("Level bar Hyper Clear", 543),
	LevelBarHyperHardClear("Level bar Hyper Hard Clear", 544),
	LevelBarHyperFullCombo("Level bar Hyper Full Combo", 545),

	LevelBarAnotherNoPlay("Level bar Another No Play", 550),
	LevelBarAnotherFailed("Level bar Another Failed", 551),
	LevelBarAnotherEasyClear("Level bar Another Easy Clear", 552),
	LevelBarAnotherClear("Level bar Another Clear", 553),
	LevelBarAnotherHardClear("Level bar Another Hard Clear", 554),
	LevelBarAnotherFullCombo("Level bar Another Full Combo", 555),

	LevelBarInsaneNoPlay("Level bar Insane No Play", 560),
	LevelBarInsaneFailed("Level bar Insane Failed", 561),
	LevelBarInsaneEasyClear("Level bar Insane Easy Clear", 562),
	LevelBarInsaneClear("Level bar Insane Clear", 563),
	LevelBarInsaneHardClear("Level bar Insane Hard Clear", 564),
	LevelBarInsaneFullCombo("Level bar Insane Full Combo", 565),

	SelectingCourse("Selecting a course", 571),
	NotSelectingCourse("Not selecting a course", 572),

	FirstStageOrHigher("1st stage or higher", 580),
	SecondStageOrHigher("2nd stage or higher", 581),
	ThirdStageOrHigher("3rd stage or higher", 582),
	FourthStageOrHigher("4th stage or higher", 583),
	FifthStageOrHigher("5th stage or higher", 584),
	SixthStageOrHigher("6th stage or higher", 585),
	SeventhStageOrHigher("7th stage or higher", 586),
	EighthStageOrHigher("8th stage or higher", 587),
	NinthStageOrHigher("9th stage or higher", 588),
	TenthStageOrHigher("10th stage or higher", 589),

	SelectingStage1OfCourse("Selecting Stage 1 of Course", 590),
	SelectingStage2OfCourse("Selecting Stage 2 of Course", 591),
	SelectingStage3OfCourse("Selecting Stage 3 of Course", 592),
	SelectingStage4OfCourse("Selecting Stage 4 of Course", 593),
	SelectingStage5OfCourse("Selecting Stage 5 of Course", 594),
	SelectingStage6OfCourse("Selecting Stage 6 of Course", 595),
	SelectingStage7OfCourse("Selecting Stage 7 of Course", 596),
	SelectingStage8OfCourse("Selecting Stage 8 of Course", 597),
	SelectingStage9OfCourse("Selecting Stage 9 of Course", 598),
	SelectingStage10OfCourse("Selecting Stage 10 of Course", 599),

	// LR2 Internet Ranking
	IRNotAvailable("IR Not available", 600),
	FetchingIR("Fetching IR", 601),
	IRFetchComplete("IR Fetch complete", 602),
	NoPlayersOnIR("No players on IR", 603),
	FailedToConnectToIR("Failed to connect to IR", 604),
	BlacklistedChart("Blacklisted (BAN) chart", 605),
	UploadingToIR("Uploading to IR", 606),
	AccessingIR("Accessing IR", 607),
	IRTimedOut("IR timed out", 608),

	NotDisplayingRanking("Not displaying ranking", 620),
	DisplayingRanking("Displaying ranking", 621),

	NotInGhostBattle("Not in a Ghost Battle", 622),
	InGhostBattle("In a Ghost Battle", 623),

	NotComparingScoreToRival("Not comparing score to rival", 624),
	ComparingScoreToRival("Comparing score to rival", 625),

	// I'm not sure whose data these reads from.
	NoPlayData("No Play", 640),
	FailedData("Failed", 641),
	EasyClearData("Easy Clear", 642),
	ClearData("Clear", 643),
	HardClearData("Hard Clear", 644),
	FullComboData("Full Combo", 645),

	AAAData("AAA", 650),
	AAData("AA", 651),
	AData("A", 652),
	BData("B", 653),
	CData("C", 654),
	DData("D", 655),
	EData("E", 656),
	FData("F", 657),

	// Course Stage 1
	Stage1Undefined("Stage 1 Undefined", 700),
	Stage1Beginner("Stage 1 Beginner", 701),
	Stage1Normal("Stage 1 Normal", 702),
	Stage1Hyper("Stage 1 Hyper", 703),
	Stage1Another("Stage 1 Another", 704),
	Stage1Insane("Stage 1 Insane", 705),

	Stage2Undefined("Stage 2 Undefined", 710),
	Stage2Beginner("Stage 2 Beginner", 711),
	Stage2Normal("Stage 2 Normal", 712),
	Stage2Hyper("Stage 2 Hyper", 713),
	Stage2Another("Stage 2 Another", 714),
	Stage2Insane("Stage 2 Insane", 715),

	Stage3Undefined("Stage 3 Undefined", 720),
	Stage3Beginner("Stage 3 Beginner", 721),
	Stage3Normal("Stage 3 Normal", 722),
	Stage3Hyper("Stage 3 Hyper", 723),
	Stage3Another("Stage 3 Another", 724),
	Stage3Insane("Stage 3 Insane", 725),

	Stage4Undefined("Stage 4 Undefined", 730),
	Stage4Beginner("Stage 4 Beginner", 731),
	Stage4Normal("Stage 4 Normal", 732),
	Stage4Hyper("Stage 4 Hyper", 733),
	Stage4Another("Stage 4 Another", 734),
	Stage4Insane("Stage 4 Insane", 735),

	Stage5Undefined("Stage 5 Undefined", 740),
	Stage5Beginner("Stage 5 Beginner", 741),
	Stage5Normal("Stage 5 Normal", 742),
	Stage5Hyper("Stage 5 Hyper", 743),
	Stage5Another("Stage 5 Another", 744),
	Stage5Insane("Stage 5 Insane", 745),

	LaneCoverOn_1P("1P Lane Cover on", 800),
	NoteTimeLockOn_1P("1P note time lock on", 801),
	LaneCoverOn_2P("2P Lane Cover on", 810),
	NoteTimeLockOn_2P("2P note time lock on", 811),

	// 900-998 and 999 is used for skin customization options.
	AlwaysFalse("Always false", 999),

	ArenaOnline("Arena online", 1000),

	ArenaPresent_1P("Arena 1P present", 1001),
	ArenaPresent_2P("Arena 2P present", 1002),
	ArenaPresent_3P("Arena 3P present", 1003),
	ArenaPresent_4P("Arena 4P present", 1004),
	ArenaPresent_5P("Arena 5P present", 1005),
	ArenaPresent_6P("Arena 6P present", 1006),
	ArenaPresent_7P("Arena 7P present", 1007),
	ArenaPresent_8P("Arena 8P present", 1008),

	AAA_Arena_1P("Arena 1P AAA", 1010),
	AA_Arena_1P("Arena 1P AA", 1011),
	A_Arena_1P("Arena 1P A", 1012),
	B_Arena_1P("Arena 1P B", 1013),
	C_Arena_1P("Arena 1P C", 1014),
	D_Arena_1P("Arena 1P D", 1015),
	E_Arena_1P("Arena 1P E", 1016),
	F_Arena_1P("Arena 1P F", 1017),

	BorderAAA_Arena_1P("Arena 1P Border AAA", 1021),
	BorderAA_Arena_1P("Arena 1P Border AA", 1022),
	BorderA_Arena_1P("Arena 1P Border A", 1023),
	BorderB_Arena_1P("Arena 1P Border B", 1024),
	BorderC_Arena_1P("Arena 1P Border C", 1025),
	BorderD_Arena_1P("Arena 1P Border D", 1026),
	BorderE_Arena_1P("Arena 1P Border E", 1027),
	BorderF_Arena_1P("Arena 1P Border F", 1028),

	GaugeX_Arena_1P("Arena 1P x% gauge", 1030),
	Gauge1X_Arena_1P("Arena 1P 1x% gauge", 1031),
	Gauge2X_Arena_1P("Arena 1P 2x% gauge", 1032),
	Gauge3X_Arena_1P("Arena 1P 3x% gauge", 1033),
	Gauge4X_Arena_1P("Arena 1P 4x% gauge", 1034),
	Gauge5X_Arena_1P("Arena 1P 5x% gauge", 1035),
	Gauge6X_Arena_1P("Arena 1P 6x% gauge", 1036),
	Gauge7X_Arena_1P("Arena 1P 7x% gauge", 1037),
	Gauge8X_Arena_1P("Arena 1P 8x% gauge", 1038),
	Gauge9X_Arena_1P("Arena 1P 9x% gauge", 1039),
	Gauge100_Arena_1P("Arena 1P 100% gauge", 1040),

	Failed_Arena_1P("Arena 1P failed", 1041),
	Easy_Arena_1P("Arena 1P easy", 1042),
	Clear_Arena_1P("Arena 1P clear", 1043),
	Hard_Arena_1P("Arena 1P hard", 1044),
	FullCombo_Arena_1P("Arena 1P Full Combo", 1045),
	EXHard_Arena_1P("Arena 1P EX-Hard", 1046),
	AssistEasy_Arena_1P("Arena 1P Assist Easy", 1047),

	// 1050-1089 Arena 2P, 1110-1149 Arena 3P, 1150-1189 Arena 4P, 1210-1249 Arena 5P, 1250-1289 Arena 6P, 1310-1349 Arena 7P, 1350-1389 Arena 8P

	ArenaPlayerReady("Arena Player ready", 1400),
	ArenaReady_1P("Arena 1P ready", 1401),
	ArenaReady_2P("Arena 2P ready", 1402),
	ArenaReady_3P("Arena 3P ready", 1403),
	ArenaReady_4P("Arena 4P ready", 1404),
	ArenaReady_5P("Arena 5P ready", 1405),
	ArenaReady_6P("Arena 6P ready", 1406),
	ArenaReady_7P("Arena 7P ready", 1407),
	ArenaReady_8P("Arena 8P ready", 1408);


	private final String name;
	private final int value;

	LR2DestinationOptions(String name, int value) {
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
	public static LR2DestinationOptions valueOf(int v) {
		return Arrays.stream(LR2DestinationOptions.values()).filter(def -> def.value == v).findAny().orElse(null);
	}

	/**
	 * Some of the lr2 definitions cannot be mapped directly because their place has been taken or it's implemented
	 *  elsewhere. This function helps converting them. The original value would be returned if it's no need to be
	 *  converted or don't know how.
	 */
	public static int convert(int v) {
		return switch (v) {
			default -> v;
		};
	}
}
