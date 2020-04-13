package de.wuzlwuz.griefergames.chat;

import de.wuzlwuz.griefergames.GrieferGames;
import de.wuzlwuz.griefergames.helper.Helper;
import de.wuzlwuz.griefergames.settings.ModSettings;
import net.labymod.api.LabyModAPI;
import net.labymod.servermanager.ChatDisplayAction;
import net.minecraft.util.text.ITextComponent;

public class Chat {
	protected GrieferGames getGG() {
		return GrieferGames.getGriefergames();
	}

	protected LabyModAPI getApi() {
		return getGG().getApi();
	}

	protected Helper getHelper() {
		return GrieferGames.getGriefergames().getHelper();
	}

	protected ModSettings getSettings() {
		return GrieferGames.getSettings();
	}

	public String getName() {
		return "chat";
	}

	public boolean doAction(String unformatted, String formatted) {
		return false;
	}

	public boolean doActionHandleChatMessage(String unformatted, String formatted) {
		return false;
	}

	public boolean doActionModifyChatMessage(ITextComponent msg) {
		return false;
	}

	public boolean doActionCommandMessage(String unformatted) {
		return false;
	}

	public ChatDisplayAction handleChatMessage(String unformatted, String formatted) {
		return ChatDisplayAction.NORMAL;
	}

	public ITextComponent modifyChatMessage(ITextComponent msg) {
		return msg;
	}

	public boolean commandMessage(String unformatted) {
		return false;
	}

	public boolean doActionReceiveMessage(String formatted, String unformatted) {
		return false;
	}

	public boolean receiveMessage(String formatted, String unformatted) {
		return false;
	}
}
