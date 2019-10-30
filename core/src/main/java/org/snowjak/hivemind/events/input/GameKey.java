/**
 * 
 */
package org.snowjak.hivemind.events.input;

import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Bits;

/**
 * Adaption of {@link Input.Keys}.
 * 
 * @author snowjak88
 *
 */
public enum GameKey {
	NUM_0(7, Keys.NUM_0),
	NUM_1(8, Keys.NUM_1),
	NUM_2(9, Keys.NUM_2),
	NUM_3(10, Keys.NUM_3),
	NUM_4(11, Keys.NUM_4),
	NUM_5(12, Keys.NUM_5),
	NUM_6(13, Keys.NUM_6),
	NUM_7(14, Keys.NUM_7),
	NUM_8(15, Keys.NUM_8),
	NUM_9(16, Keys.NUM_9),
	A(29, Keys.A),
	ALT_LEFT(57, Keys.ALT_LEFT),
	ALT_RIGHT(58, Keys.ALT_RIGHT),
	APOSTROPHE(75, Keys.APOSTROPHE),
	AT(77, Keys.AT),
	B(30, Keys.B),
	BACK(4, Keys.BACK),
	BACKSLASH(73, Keys.BACKSLASH),
	C(31, Keys.C),
	CALL(5, Keys.CALL),
	CAMERA(27, Keys.CAMERA),
	CLEAR(28, Keys.CLEAR),
	COMMA(55, Keys.COMMA),
	D(32, Keys.D),
	DEL(67, Keys.DEL),
	BACKSPACE(67, Keys.BACKSPACE),
	FORWARD_DEL(112, Keys.FORWARD_DEL),
	DPAD_CENTER(23, Keys.DPAD_CENTER),
	DPAD_DOWN(20, Keys.DPAD_DOWN),
	DPAD_LEFT(21, Keys.DPAD_LEFT),
	DPAD_RIGHT(22, Keys.DPAD_RIGHT),
	DPAD_UP(19, Keys.DPAD_UP),
	CENTER(23, Keys.CENTER),
	DOWN(20, Keys.DOWN),
	LEFT(21, Keys.LEFT),
	RIGHT(22, Keys.RIGHT),
	UP(19, Keys.UP),
	E(33, Keys.E),
	ENDCALL(6, Keys.ENDCALL),
	ENTER(66, Keys.ENTER),
	ENVELOPE(65, Keys.ENVELOPE),
	EQUALS(70, Keys.EQUALS),
	EXPLORER(64, Keys.EXPLORER),
	F(34, Keys.F),
	FOCUS(80, Keys.FOCUS),
	G(35, Keys.G),
	GRAVE(68, Keys.GRAVE),
	H(36, Keys.H),
	HEADSETHOOK(79, Keys.HEADSETHOOK),
	HOME(3, Keys.HOME),
	I(37, Keys.I),
	J(38, Keys.J),
	K(39, Keys.K),
	L(40, Keys.L),
	LEFT_BRACKET(71, Keys.LEFT_BRACKET),
	M(41, Keys.M),
	MEDIA_FAST_FORWARD(90, Keys.MEDIA_FAST_FORWARD),
	MEDIA_NEXT(87, Keys.MEDIA_NEXT),
	MEDIA_PLAY_PAUSE(85, Keys.MEDIA_PLAY_PAUSE),
	MEDIA_PREVIOUS(88, Keys.MEDIA_PREVIOUS),
	MEDIA_REWIND(89, Keys.MEDIA_REWIND),
	MEDIA_STOP(86, Keys.MEDIA_STOP),
	MENU(82, Keys.MENU),
	MINUS(69, Keys.MINUS),
	MUTE(91, Keys.MUTE),
	N(42, Keys.N),
	NOTIFICATION(83, Keys.NOTIFICATION),
	NUM(78, Keys.NUM),
	O(43, Keys.O),
	P(44, Keys.P),
	PERIOD(56, Keys.PERIOD),
	PLUS(81, Keys.PLUS),
	POUND(18, Keys.POUND),
	POWER(26, Keys.POWER),
	Q(45, Keys.Q),
	R(46, Keys.R),
	RIGHT_BRACKET(72, Keys.RIGHT_BRACKET),
	S(47, Keys.S),
	SEARCH(84, Keys.SEARCH),
	SEMICOLON(74, Keys.SEMICOLON),
	SHIFT_LEFT(59, Keys.SHIFT_LEFT),
	SHIFT_RIGHT(60, Keys.SHIFT_RIGHT),
	SLASH(76, Keys.SLASH),
	SOFT_LEFT(1, Keys.SOFT_LEFT),
	SOFT_RIGHT(2, Keys.SOFT_RIGHT),
	SPACE(62, Keys.SPACE),
	STAR(17, Keys.STAR),
	SYM(63, Keys.SYM),
	T(48, Keys.T),
	TAB(61, Keys.TAB),
	U(49, Keys.U),
	UNKNOWN(0, Keys.UNKNOWN),
	V(50, Keys.V),
	VOLUME_DOWN(25, Keys.VOLUME_DOWN),
	VOLUME_UP(24, Keys.VOLUME_UP),
	W(51, Keys.W),
	X(52, Keys.X),
	Y(53, Keys.Y),
	Z(54, Keys.Z),
	META_ALT_LEFT_ON(16, Keys.META_ALT_LEFT_ON),
	META_ALT_ON(2, Keys.META_ALT_ON),
	META_ALT_RIGHT_ON(32, Keys.META_ALT_RIGHT_ON),
	META_SHIFT_LEFT_ON(64, Keys.META_SHIFT_LEFT_ON),
	META_SHIFT_ON(1, Keys.META_SHIFT_ON),
	META_SHIFT_RIGHT_ON(128, Keys.META_SHIFT_RIGHT_ON),
	META_SYM_ON(4, Keys.META_SYM_ON),
	CONTROL_LEFT(129, Keys.CONTROL_LEFT),
	CONTROL_RIGHT(130, Keys.CONTROL_RIGHT),
	ESCAPE(131, Keys.ESCAPE),
	END(132, Keys.END),
	INSERT(133, Keys.INSERT),
	PAGE_UP(92, Keys.PAGE_UP),
	PAGE_DOWN(93, Keys.PAGE_DOWN),
	PICTSYMBOLS(94, Keys.PICTSYMBOLS),
	SWITCH_CHARSET(95, Keys.SWITCH_CHARSET),
	BUTTON_CIRCLE(255, Keys.BUTTON_CIRCLE),
	BUTTON_A(96, Keys.BUTTON_A),
	BUTTON_B(97, Keys.BUTTON_B),
	BUTTON_C(98, Keys.BUTTON_C),
	BUTTON_X(99, Keys.BUTTON_X),
	BUTTON_Y(100, Keys.BUTTON_Y),
	BUTTON_Z(101, Keys.BUTTON_Z),
	BUTTON_L1(102, Keys.BUTTON_L1),
	BUTTON_R1(103, Keys.BUTTON_R1),
	BUTTON_L2(104, Keys.BUTTON_L2),
	BUTTON_R2(105, Keys.BUTTON_R2),
	BUTTON_THUMBL(106, Keys.BUTTON_THUMBL),
	BUTTON_THUMBR(107, Keys.BUTTON_THUMBR),
	BUTTON_START(108, Keys.BUTTON_START),
	BUTTON_SELECT(109, Keys.BUTTON_SELECT),
	BUTTON_MODE(110, Keys.BUTTON_MODE),
	
	NUMPAD_0(144, Keys.NUMPAD_0),
	NUMPAD_1(145, Keys.NUMPAD_1),
	NUMPAD_2(146, Keys.NUMPAD_2),
	NUMPAD_3(147, Keys.NUMPAD_3),
	NUMPAD_4(148, Keys.NUMPAD_4),
	NUMPAD_5(149, Keys.NUMPAD_5),
	NUMPAD_6(150, Keys.NUMPAD_6),
	NUMPAD_7(151, Keys.NUMPAD_7),
	NUMPAD_8(152, Keys.NUMPAD_8),
	NUMPAD_9(153, Keys.NUMPAD_9),
	
	COLON(243, Keys.COLON),
	F1(244, Keys.F1),
	F2(245, Keys.F2),
	F3(246, Keys.F3),
	F4(247, Keys.F4),
	F5(248, Keys.F5),
	F6(249, Keys.F6),
	F7(250, Keys.F7),
	F8(251, Keys.F8),
	F9(252, Keys.F9),
	F10(253, Keys.F10),
	F11(254, Keys.F11),
	F12(255, Keys.F12);
	
	public static final int MAX_BIT_INDEX = 255;
	private static final MutableIntObjectMap<GameKey> keycodeToKey = new IntObjectHashMap<>();
	private final int bitIndex, keycode;
	
	GameKey(int bitIndex, int keycode) {
		
		this.bitIndex = bitIndex;
		this.keycode = keycode;
	}
	
	public int getBitIndex() {
		
		return bitIndex;
	}
	
	public int getKeycode() {
		
		return keycode;
	}
	
	public static Bits getBits(GameKey... keys) {
		
		final Bits result = new Bits(MAX_BIT_INDEX);
		for (int i = 0; i < keys.length; i++)
			if (keys[i] != null)
				result.set(keys[i].bitIndex);
		return result;
	}
	
	/**
	 * Get the {@link GameKey} instance mapped to the given GDX keycode, or
	 * {@code null} if no such GameKey has been mapped.
	 * 
	 * @param keycode
	 * @return
	 */
	public static GameKey getForKey(int keycode) {
		
		synchronized (GameKey.class) {
			if (keycodeToKey.isEmpty())
				for (int i = 0; i < GameKey.values().length; i++) {
					final GameKey gk = GameKey.values()[i];
					keycodeToKey.put(gk.keycode, gk);
				}
			return keycodeToKey.get(keycode);
		}
	}
}
