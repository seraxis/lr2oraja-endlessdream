package bms.player.beatoraja.skin.lr2.commands;

public class Options {
	public @CSVField(value = 0, option = true) int op1;
	public @CSVField(value = 1, option = true) int op2;
	public @CSVField(value = 2, option = true) int op3;
	public @CSVField(value = 3, option = true, optional = true) int op4;
}
