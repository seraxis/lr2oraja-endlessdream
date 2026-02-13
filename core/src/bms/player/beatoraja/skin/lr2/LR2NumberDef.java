package bms.player.beatoraja.skin.lr2;

import bms.player.beatoraja.skin.property.IntegerPropertyFactory;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

import static bms.player.beatoraja.skin.SkinProperty.*;

/**
 * LR2 number definition
 */
public enum LR2NumberDef {
	// Play Options
	HiSpeed_1P("1P Hi-speed", 10),
	HiSpeed_2P("2P Hi-speed", 11),

	CurrentJudgeTimingOffset("Current judge timing offset", 12),
	DefaultTargetRate("Default target rate", 13),

	SUDValue_1P("1P SUD+ value", 14),
	SUDValue_2P("2P SUD+ value", 15),

	// Date and Time
	FPS("FPS", 20),
	Year("Year", 21),
	Month("Month", 22),
	Day("Day", 23),
	Hours("Hours", 24),
	Minutes("Minutes", 25),
	Seconds("Seconds", 26),

	// For stats panel
	TotalPlaycount("Total playcount", 30),
	TotalClears("Total clears", 31),
	TotalFails("Total fails", 32),

	TotalPGREATs("Total PGREATs", 33),
	TotalGREATs("Total GREATs", 34),
	TotalGOODs("Total GOODs", 35),
	TotalBADs("Total BADs", 36),
	TotalPOORs("Total POORs (both Empty POORs and Miss POORs)", 37),

	RunningCombo("Running combo (does not reset every chart, only on CBs)", 38),
	MaxRunningCombo("Max Running combo", 39),

	TrialLevel("Trial level (only 0)", 40),
	PreviousTrialLevel("Previous trial level", 41),

	LevelBeginnerInFolder("Level of Beginner chart in Folder", 45),
	LevelNormalInFolder("Level of Normal chart in Folder", 46),
	LevelHyperInFolder("Level of Hyper chart in Folder", 47),
	LevelAnotherInFolder("Level of Another chart in Folder", 48),
	LevelInsaneInFolder("Level of Insane chart in Folder", 49),

	// Effects panel
	Hz62("62Hz", 50),
	Hz160("160Hz", 51),
	Hz400("400Hz", 52),
	Hz1K("1Khz", 53),
	Hz2_5K("2.5Khz", 54),
	Hz6_3K("6.3Khz", 55),
	Hz16K("16Khz", 56),

	MasterVolume("Master Volume", 57),
	KeyVolume("Key Volume", 58),
	BGMVolume("BGM Volume", 59),

	FX0_1P("FX0 1P", 60),
	FX0_2P("FX0 2P", 61),

	FX1_1P("FX1 1P", 62),
	FX1_2P("FX1 2P", 63),

	FX2_1P("FX2 1P", 64),
	FX2_2P("FX2 2P", 65),

	FreqPitchSpeedValue("FREQ/PITCH/SPEED value", 66),

	// Selecting songs
	MoneyScore("Money score", 70),
	EXScore("EX score", 71),
	TargetEXScore("Target EX score", 72),
	RatePercent("Rate %", 73),
	TotalNotes("Total notes", 74),
	MaxCombo("Max Combo", 75),
	BadPoor("Bad + Poor", 76),
	// Only shows 0 or 1 in LVF.
	Plays("Plays", 77),
	Clears("Clears", 78),
	Fails("Fails", 79),

	PGREATCount("PGREAT count", 80),
	GREATCount("GREAT count", 81),
	GOODCount("GOOD count", 82),
	BADCount("BAD count", 83),
	POORCount("POOR count", 84),
	PGREATPercent("PGREAT %", 85),
	GREATPercent("GREAT %", 86),
	GOODPercent("GOOD %", 87),
	BADPercent("BAD %", 88),
	POORPercent("POOR %", 89),

	MaxBPM("Max BPM", 90),
	MinBPM("Min BPM", 91),

	IRRank("IR rank", 92),
	IRPlayers("IR players", 93),
	IRClearRate("IR clear rate", 94),
	IRRivalScoreDifference("IR rival score difference", 95),

	// Playing 1P
	MoneyScore_1P("Money score", 100),
	EXScore_1P("EX score", 101),
	Rate_1P("Rate", 102),
	RateDecimal_1P("Rate decimal value (2 digits)", 103),
	CurrentCombo_1P("Current combo", 104),
	MaxCombo_1P("Max combo", 105),
	TotalNotes_1P("Total notes", 106),
	GrooveGauge_1P("Groove gauge", 107),
	EXScoreDifferenceFrom2P_1P("EX score difference from 2P", 108),

	PGREATs_1P("PGREATs", 110),
	GREATs_1P("GREATs", 111),
	GOODs_1P("GOODs", 112),
	BADs_1P("BADs", 113),
	POORs_1P("POORs", 114),
	IncrementalRate_1P("Incremental Rate", 115),
	IncrementalRateDecimal_1P("Incremental Rate decimal part (2 digits)", 116),

	// Playing 2P
	MoneyScore_2P("Money score", 120),
	EXScore_2P("EX score", 121),
	Rate_2P("Rate", 122),
	RateDecimal_2P("Rate decimal value (2 digits)", 123),
	CurrentCombo_2P("Current combo", 124),
	MaxCombo_2P("Max combo", 125),
	TotalNotes_2P("Total notes", 126),
	GrooveGauge_2P("Groove gauge", 127),
	EXScoreDifferenceFrom1P_2P("EX score difference from 1P", 128),

	PGREATs_2P("PGREATs", 130),
	GREATs_2P("GREATs", 131),
	GOODs_2P("GOODs", 132),
	BADs_2P("BADs", 133),
	POORs_2P("POORs", 134),
	IncrementalRate_2P("Incremental Rate", 135),
	IncrementalRateDecimal_2P("Incremental Rate decimal part (2 digits)", 136),

	// Result skin (MUST USE #DISABLEFLIP)
	HighScore("High score", 150),
	TargetScore("Target score", 151),
	DifferenceHighScoreCurrentScore("Difference between high score and current score", 152),
	DifferenceTargetScoreCurrentScore("Difference between target score and current score", 153),
	DifferenceFromNextRank("Difference from next rank", 154),
	HighScoreRate("High score RATE", 155),
	HighScoreRateDecimal("High score RATE decimal part (2 digits)", 156),
	TargetScoreRate("Target score RATE", 157),
	TargetScoreRateDecimal("Target score RATE decimal part (2 digits)", 158),

	// Play status
	CurrentBPM("Current BPM", 160),
	MinutesCurrent("Minutes", 161),
	SecondsCurrent("Seconds", 162),
	RemainingMinutes("Remaining minutes", 163),
	RemainingSeconds("Remaining seconds", 164),
	ChartLoadPercent("Chart load %", 165),

	// Update screen
	EXScoreBeforeUpdate("EX Score before Update", 170),
	EXScoreAfterUpdate("EX Score after Update", 171),
	EXScoreDifference("EX Score difference", 172),

	MaxComboBeforeUpdate("Max Combo before Update", 173),
	MaxComboAfterUpdate("Max Combo after Update", 174),
	MaxComboDifference("Max Combo difference", 175),

	BadPoorBeforeUpdate("Bad+Poor before Update", 176),
	BadPoorAfterUpdate("Bad+Poor after Update", 177),
	BadPoorDifference("Bad+Poor difference", 178),

	IRRankUpdate("IR rank", 179),
	IRTotalPlayersUpdate("IR total players", 180),
	IRClearRateUpdate("IR clear rate", 181),
	IRRankBeforeUpdate("IR rank before update", 182),

	RateBeforeUpdate("Rate before update", 183),
	RateDecimalBeforeUpdate("Rate decimal before update (2 digits)", 184),

	// FAST/SLOW and stuff
	HitOffset_1P("1P hit offset (ms)", 201),
	FastSlow_1P("1P FAST/SLOW", 210),
	FastSlow_2P("2P FAST/SLOW", 211),
	// Combine with DST op 35 for SLOW, and -35 for FAST.
	FastCount("Fast count", 212),
	HitOffset_2P("2P hit offset (ms)", 213),
	SlowCount("Slow count", 214),
	ComboBreaks("Combo Breaks", 216),
	RemainingNotes("Remaining notes", 217),
	RunningNotes("Running notes", 218),

	// LR2IR
	IRPlayersInfo("IR players", 200),
	IRTotalPlays("IR total plays", 201),

	FailedPlayers("Failed players", 210),
	FailedPlayersPercentage("Failed players percentage", 211),
	EasyClearedPlayers("Easy cleared players", 212),
	EasyClearedPlayersPercentage("Easy cleared players percentage", 213),
	ClearedPlayers("Cleared players", 214),
	ClearedPlayersPercentage("Cleared players percentage", 215),
	HardClearedPlayers("Hard Cleared players", 216),
	HardClearedPlayersPercentage("Hard Cleared players percentage", 217),
	FullComboClearedPlayers("Full Combo Cleared players", 218),
	FullComboClearedPlayersPercentage("Full Combo Cleared players percentage", 219),

	TimeRemainingUntilIRAutoUpdate("Time remaining until IR auto update", 220),

	// Course stage levels
	CourseStage1Level("Course Stage 1 level", 250),
	CourseStage2Level("Course Stage 2 level", 251),
	CourseStage3Level("Course Stage 3 level", 252),
	CourseStage4Level("Course Stage 4 level", 253),
	CourseStage5Level("Course Stage 5 level", 254),

	// In Rival folder
	MoneyScoreRival("Money score", 270),
	EXScoreRival("EX score", 271),
	TargetEXScoreRival("Target EX score", 272),
	RatePercentRival("Rate %", 273),
	TotalNotesRival("Total notes", 274),
	MaxComboRival("Max Combo", 275),
	BadPoorRival("Bad + Poor", 276),
	PlaysRival("Plays", 277),
	ClearsRival("Clears", 278),
	FailsRival("Fails", 279),

	PGREATCountRival("PGREAT count", 280),
	GREATCountRival("GREAT count", 281),
	GOODCountRival("GOOD count", 282),
	BADCountRival("BAD count", 283),
	POORCountRival("POOR count", 284),
	PGREATPercentRival("PGREAT %", 285),
	GREATPercentRival("GREAT %", 286),
	GOODPercentRival("GOOD %", 287),
	BADPercentRival("BAD %", 288),
	POORPercentRival("POOR %", 289),

	MaxBPMRival("Max BPM", 290),
	MinBPMRival("Min BPM", 291),

	IRRankRival("IR rank", 292),
	IRPlayersRival("IR players", 293),
	IRClearRateRival("IR clear rate", 294),

	RandomPattern_1P("1P random pattern (7 digit long from 1-7)", 295),
	HitMean("Hit mean", 296),
	HitMeanDecimal("Hit mean decimal part (2 digits)", 297),
	StandardDeviation("Standard deviation", 298),
	StandardDeviationDecimal("Standard deviation decimal part (2 digits)", 299),
	Total("TOTAL", 301),
	GreenNumber("Green number", 302),
	WhiteNumber("White number", 303),
	GreenNumberMin("Green number min", 304),
	GreenNumberMax("Green number max", 305),

	PARatio("PA ratio", 400),
	PARatioDecimal("PA ratio decimal part", 401),
	GARatio("GA ratio", 402),
	GARatioDecimal("GA ratio decimal part", 403),

	PGREATPercentage("PGREAT percentage", 404),
	PGREATPercentageDecimal("PGREAT percentage decimal part", 405),
	GREATPercentage("GREAT percentage", 406),
	GREATPercentageDecimal("GREAT percentage decimal part", 407),
	GOODPercentage("GOOD percentage", 408),
	GOODPercentageDecimal("GOOD percentage decimal part", 409),
	BADPercentage("BAD percentage", 410),
	BADPercentageDecimal("BAD percentage decimal part", 411),
	POORPercentage("POOR percentage", 412),
	POORPercentageDecimal("POOR percentage decimal part", 413),

	GreenNumberDecimal("Green number decimal part", 414),
	WhiteNumberDecimal("White number decimal part", 415),

	LIFTNumber("LIFT number", 416),
	LIFTNumberDecimal("LIFT number decimal part", 417),

	RandomPattern_2P("2P random pattern", 418),

	CustomGauge("Custom gauge", 419),
	CustomGaugeDecimal1("Custom gauge decimal part (1 digit)", 420),
	CustomGaugeDecimal2("Custom gauge decimal part (2 digit)", 421),

	TotalJudgements("Total judgements", 422),
	WastedHumanCyclesSessionSeconds("Wasted human cycles this session (seconds)", 423),
	WastedHumanCyclesSessionMinutes("Wasted human cycles this session (minutes)", 424),
	WastedHumanCyclesSessionHours("Wasted human cycles this session (hours)", 425),
	TotalWastedHumanCyclesSeconds("Total wasted human cycles (seconds)", 426),
	TotalWastedHumanCyclesMinutes("Total wasted human cycles (minutes)", 427),
	TotalWastedHumanCyclesHours("Total wasted human cycles (hours)", 428),

	// Arena
	ArenaEXScoreDifferenceTo1st("ARENA EX Score difference to 1st place", 450),

	ArenaMoneyScore_1P("ARENA 1P money score", 500),
	ArenaEXScore_1P("ARENA 1P EX Score", 501),
	ArenaCurrentCombo_1P("ARENA 1P current combo", 502),
	ArenaMaxCombo_1P("ARENA 1P max combo", 503),
	ArenaRate_1P("ARENA 1P rate", 504),
	ArenaRateDecimal_1P("ARENA 1P rate decimal part", 505),
	ArenaTotalNotes_1P("ARENA 1P total notes", 506),
	ArenaIncrementalRate_1P("ARENA 1P incremental rate", 507),
	ArenaIncrementalRateDecimal_1P("ARENA 1P incremental rate decimal part", 508),
	ArenaPGREATCount_1P("ARENA 1P PGREAT count", 509),
	ArenaGREATCount_1P("ARENA 1P GREAT count", 510),
	ArenaGOODCount_1P("ARENA 1P GOOD count", 511),
	ArenaBADCount_1P("ARENA 1P BAD count", 512),
	ArenaPOORCount_1P("ARENA 1P POOR count", 513),
	ArenaEmptyPOORCount_1P("ARENA 1P Empty POOR count", 514),
	ArenaMissCount_1P("ARENA 1P Miss count", 515),
	ArenaBadPoorCount_1P("ARENA 1P Bad+Poor count", 516),
	ArenaComboBreakCount_1P("ARENA 1P Combo Break count", 517),
	ArenaGauge_1P("ARENA 1P gauge", 518),
	ArenaEXScoreDifferenceToNextRank_1P("ARENA 1P EX Score difference to Next rank", 519),
	ArenaEXScoreDifferenceTo1P_1P("ARENA 1P Ex Score difference to 1P (?)", 520);

// Note: 530-559 ARENA 2P, 560-589 ARENA 3P, 590-619 ARENA 4P,
// 620-649 ARENA 5P, 650-679 ARENA 6P, 680-709 ARENA 7P, 710-739 ARENA 8P
// These ranges follow the same pattern as ARENA 1P (500-520) but for other players.
// For brevity, we'll list the ranges as comments.

	private final String name;
	private final int value;

	LR2NumberDef(String name, int value) {
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
	public static LR2NumberDef valueOf(int v) {
		return Arrays.stream(LR2NumberDef.values()).filter(def -> def.value == v).findAny().orElse(null);
	}

	/**
	 * Some of the lr2 definitions cannot be mapped directly because their place has been taken or it's implemented
	 *  elsewhere. This function helps converting them. The original value would be returned if it's no need to be
	 *  converted or don't know how.
	 */
	public static int convert(int v) {
		if (v == LR2NumberDef.IRRank.value) {
			return NUMBER_IR_RANK;
		} else if (v == LR2NumberDef.IRPlayers.value) {
			return NUMBER_IR_TOTALPLAYER;
		} else if (v == IRClearRate.value) {
			return NUMBER_IR_PLAYER_TOTAL_CLEAR_RATE;
		} else if (v == RatePercent.value) {
			return NUMBER_SCORE_RATE;
		} else if (v == StandardDeviation.value) {
			return NUMBER_STDDEV_TIMING;
		} else if (v == StandardDeviationDecimal.value) {
			return NUMBER_STDDEV_TIMING_AFTERDOT;
		} else if (v == HitMean.value) {
			return NUMBER_AVERAGE_TIMING;
		} else if (v == HitMeanDecimal.value) {
			return NUMBER_AVERAGE_TIMING_AFTERDOT;
		} else if (v == Total.value) {
			return NUMBER_SONGGAUGE_TOTAL;
		} else if (v == GreenNumber.value) {
			return NUMBER_DURATION_GREEN;
		} else if (v == WhiteNumber.value) {
			return NUMBER_DURATION;
		}
		return v;
	}
}
