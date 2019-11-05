/**
 * 
 */
package org.snowjak.hivemind.display;

import java.util.Collection;
import java.util.LinkedList;

import org.snowjak.hivemind.config.Config;
import org.snowjak.hivemind.gamescreen.UpdateableInputAdapter;
import org.snowjak.hivemind.ui.Skin;
import org.snowjak.hivemind.util.TypedStore.TypedStoreItem;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import squidpony.squidgrid.gui.gdx.SColor;

/**
 * In this {@link State}, the {@link Display} displays the configuration-screen.
 * 
 * @author snowjak88
 *
 */
public class ConfigScreenDisplayState implements DisplayState {
	
	private final LabelStyle headingLabelStyle, bodyLabelStyle;
	private final TextFieldStyle textFieldStyle;
	private final CheckBoxStyle checkboxStyle;
	
	private Table root;
	private boolean backToMainMenu = false;
	
	private final Collection<TypedStoreItem<?>> configurations = new LinkedList<>();
	private final Collection<TypedStoreItem<?>> modified = new LinkedList<>();
	
	public ConfigScreenDisplayState() {
		
		this.root = new Table();
		this.root.setFillParent(true);
		
		headingLabelStyle = new LabelStyle(Fonts.get().get(Fonts.FONT_HEADING), SColor.AURORA_APRICOT);
		bodyLabelStyle = new LabelStyle(Fonts.get().get(Fonts.FONT_NORMAL), SColor.APRICOT);
		textFieldStyle = new TextFieldStyle(Fonts.get().get(Fonts.FONT_NORMAL), SColor.APRICOT, null, null,
				Skin.get().getDrawable(Skin.BUTTON_DOWN));
		checkboxStyle = new CheckBoxStyle();
		
		addRow(new Label("Key", headingLabelStyle), new Label("Description", headingLabelStyle),
				new Label("Type", headingLabelStyle), new Label("Default", headingLabelStyle),
				new Label("Value", headingLabelStyle));
		
		for (TypedStoreItem<?> ci : Config.get().getItems())
			if (ci.isConfigurable())
				this.configurations.add(new TypedStoreItem<>(ci));
			
		for (TypedStoreItem<?> ci : this.configurations)
			addRow(ci);
		
		final BitmapFont buttonFont = Fonts.get().get(Fonts.FONT_NORMAL);
		buttonFont.setColor(SColor.AURORA_CLOUD);
		final TextButtonStyle buttonStyle = new TextButtonStyle(Skin.get().getDrawable(Skin.BUTTON_UP),
				Skin.get().getDrawable(Skin.BUTTON_DOWN), Skin.get().getDrawable(Skin.BUTTON_CHECKED), buttonFont);
		
		final TextButton applyButton = new TextButton("Apply", buttonStyle);
		applyButton.pad(16, 32, 16, 32);
		applyButton.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				
				boolean requiresRestart = false;
				for (TypedStoreItem<?> ci : modified) {
					Config.get().set(ci.getKey(), ci.getValue());
					if (ci.isRequiresRestart())
						requiresRestart = true;
				}
				
				if (requiresRestart) {
					final Window alertWindow = new Window("Restart to Apply Changes",
							new WindowStyle(Fonts.get().get(Fonts.FONT_HEADING), SColor.WHITE,
									Skin.get().getDrawable(Skin.BUTTON_DOWN)));
					alertWindow.setModal(true);
					
					final Label alertLabel = new Label("You must restart Hivemind for your changes to take effect.",
							bodyLabelStyle);
					alertLabel.setWrap(true);
					alertLabel.setFillParent(true);
					alertWindow.row().left().expandX();
					alertWindow.add(alertLabel);
					
					final TextButton dismissAlertWindowButton = new TextButton("OK", buttonStyle);
					dismissAlertWindowButton.pad(16, 32, 16, 32);
					dismissAlertWindowButton.addListener(new ClickListener() {
						
						@Override
						public void clicked(InputEvent event, float x, float y) {
							
							alertWindow.remove();
							backToMainMenu = true;
						}
					});
					alertWindow.row().center();
					alertWindow.add(dismissAlertWindowButton);
					
					root.add(alertWindow);
				}
			}
		});
		
		root.row();
		root.add(applyButton);
		
		backToMainMenu = false;
	}
	
	private void addRow(TypedStoreItem<?> item) {
		
		final TextField keyLabel = new TextField(item.getKey(), textFieldStyle);
		keyLabel.setDisabled(true);
		
		final Label descriptionLabel = new Label(item.getDescription(), bodyLabelStyle);
		descriptionLabel.setWrap(true);
		
		final TextField typeLabel = new TextField(item.getType().getSimpleName(), textFieldStyle);
		typeLabel.setDisabled(true);
		
		final Label defaultValueField = new Label(item.getDefaultValue().toString(), bodyLabelStyle);
		
		final Actor valueField;
		
		if (String.class.isAssignableFrom(item.getType())) {
			valueField = new TextField(item.getStringValue(), textFieldStyle);
			valueField.addListener(new ChangeListener() {
				
				@SuppressWarnings("unchecked")
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					
					final TextField field = (TextField) actor;
					((TypedStoreItem<String>) item).setValue(field.getText());
					modified.add(item);
				}
			});
		} else if (Number.class.isAssignableFrom(item.getType())) {
			valueField = new TextField(item.getStringValue(), textFieldStyle);
			valueField.addListener(new ChangeListener() {
				
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					
					final TextField field = (TextField) actor;
					item.setStringValue(field.getText());
					modified.add(item);
				}
			});
		} else if (Boolean.class.isAssignableFrom(item.getType())) {
			valueField = new CheckBox("", checkboxStyle);
			((CheckBox) valueField).setChecked((Boolean) item.getValue());
			valueField.addListener(new ChangeListener() {
				
				@SuppressWarnings("unchecked")
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					
					final CheckBox field = (CheckBox) actor;
					((TypedStoreItem<Boolean>) item).setValue(field.isChecked());
				}
			});
		} else {
			valueField = new Label("(unknown type [" + item.getType().getSimpleName() + "])", bodyLabelStyle);
			((Label) valueField).setWrap(true);
		}
		
		addRow(keyLabel, descriptionLabel, typeLabel, defaultValueField, valueField);
	}
	
	private void addRow(Actor keyActor, Actor descriptionActor, Actor typeActor, Actor defaultValueActor,
			Actor valueActor) {
		
		this.root.row();
		this.root.add(keyActor);
		this.root.add(descriptionActor).left().width(240);
		this.root.add(typeActor).uniform();
		this.root.add(defaultValueActor).uniform();
		this.root.add(valueActor).expandX().uniform();
	}
	
	@Override
	public void enter(Display entity) {
		
		entity.setRoot(root);
		entity.setInput(new UpdateableInputAdapter() {
			
			@Override
			public boolean keyDown(int keycode) {
				
				switch (keycode) {
				case (Keys.ESCAPE):
					backToMainMenu = true;
					return true;
				default:
					return false;
				}
			}
		});
	}
	
	@Override
	public void update(Display entity) {
		
		if (backToMainMenu)
			entity.getDisplayStateMachine().changeState(new MainMenuDisplayState());
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
