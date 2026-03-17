package bms.player.beatoraja.skin.property;

import bms.player.beatoraja.MainState;

public interface BooleanProperty {
	
	public boolean isStatic(MainState state);
	
	public boolean get(MainState state);

	default BooleanProperty reverse() {
		return new BooleanProperty() {
			@Override
			public boolean isStatic(MainState state) {
				return BooleanProperty.this.isStatic(state);
			}

			@Override
			public boolean get(MainState state) {
				return !BooleanProperty.this.get(state);
			}
		};
	}
}