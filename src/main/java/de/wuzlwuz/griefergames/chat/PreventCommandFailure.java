package de.wuzlwuz.griefergames.chat;

import net.labymod.utils.ModColor;
import net.minecraft.util.text.ITextComponent;

public class PreventCommandFailure extends Chat {
	private String lastCommand = "";

	private String getLastCommand() {
		return lastCommand;
	}

	private void setLastCommand(String lastCommand) {
		this.lastCommand = lastCommand;
	}

	@Override
	public String getName() {
		return "preventCommandFailure";
	}

	@Override
	public boolean doActionCommandMessage(String unformatted) {
		if (getSettings().isPreventCommandFailure()) {
			if (unformatted.length() > 1 && unformatted.startsWith("7")
					&& (!unformatted.equalsIgnoreCase(getLastCommand()))) {

				getApi().displayMessageInChat(ModColor.RED + "Hast du dich vertippt? Wenn nicht, sende es nochmal!");

				setLastCommand(unformatted);
				return true;
			} else {
				setLastCommand("");
			}
		}
		return false;
	}

	@Override
	public boolean doActionModifyChatMessage(ITextComponent msg) {
		return super.doActionModifyChatMessage(msg);
	}

	@Override
	public boolean commandMessage(String unformatted) {
		return true;
	}
}