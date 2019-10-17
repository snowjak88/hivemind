/**
 * 
 */
package org.snowjak.hivemind.display;

import org.snowjak.hivemind.events.EventBus;
import org.snowjak.hivemind.events.ExitAppEvent;
import org.snowjak.hivemind.ui.Skin;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import squidpony.squidgrid.gui.gdx.SColor;

/**
 * In this {@link State}, the {@link Display} presents the main menu.
 * 
 * @author snowjak88
 *
 */
public class MainMenuDisplayState implements DisplayState {
	
	private final VerticalGroup rootWidget;
	
	private boolean startGame = false;
	private boolean showConfig = false;
	
	public MainMenuDisplayState() {
		
		rootWidget = new VerticalGroup();
		rootWidget.setFillParent(true);
		rootWidget.align(Align.center);
		
		final Label titleLabel = new Label("Hivemind",
				new LabelStyle(Fonts.get().get(Fonts.FONT_HEADING), SColor.AURORA_CLOUD));
		titleLabel.setAlignment(Align.center);
		
		final BitmapFont buttonFont = Fonts.get().get(Fonts.FONT_NORMAL);
		buttonFont.setColor(SColor.AURORA_CLOUD);
		final TextButtonStyle buttonStyle = new TextButtonStyle(Skin.get().getDrawable(Skin.BUTTON_UP),
				Skin.get().getDrawable(Skin.BUTTON_DOWN), Skin.get().getDrawable(Skin.BUTTON_CHECKED), buttonFont);
		
		final TextButton startButton = new TextButton("Start", buttonStyle);
		startButton.pad(16, 32, 16, 32);
		startButton.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				
				startGame = true;
			}
			
		});
		
		final TextButton configButton = new TextButton("Settings", buttonStyle);
		configButton.pad(16, 32, 16, 32);
		configButton.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				
				showConfig = true;
			}
		});
		
		final TextButton exitButton = new TextButton("Exit", buttonStyle);
		exitButton.pad(16, 32, 16, 32);
		exitButton.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				
				EventBus.get().post(ExitAppEvent.class);
			}
			
		});
		
		rootWidget.addActor(titleLabel);
		rootWidget.addActor(startButton);
		rootWidget.addActor(configButton);
		rootWidget.addActor(exitButton);
	}
	
	@Override
	public void enter(Display entity) {
		
		entity.setRoot(rootWidget);
		
		startGame = false;
		showConfig = false;
	}
	
	@Override
	public void update(Display entity) {
		
		if (startGame)
			entity.getDisplayStateMachine().changeState(new GameScreenDisplayState());
		
		else if (showConfig)
			entity.getDisplayStateMachine().changeState(new ConfigScreenDisplayState());
	}
	
	@Override
	public void exit(Display entity) {
		
		entity.setRoot(null);
	}
	
	@Override
	public boolean onMessage(Display entity, Telegram telegram) {
		
		// TODO Auto-generated method stub
		return false;
	}
	
}
