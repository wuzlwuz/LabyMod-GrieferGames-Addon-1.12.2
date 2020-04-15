package de.wuzlwuz.griefergames.chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.labymod.servermanager.ChatDisplayAction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;

public class ItemRemover extends Chat {
	private static Pattern itemRemoverMessageRegex = Pattern.compile(
			"^\\[GrieferGames\\] Warnung! Die auf dem Boden liegenden Items werden in ([0-9]+) Sekunden entfernt!$");
	private static Pattern itemRemoverDoneMessageRegex = Pattern
			.compile("^\\[GrieferGames\\] Es wurden ([0-9]+) auf dem Boden liegende Items entfernt!$");

	@Override
	public String getName() {
		return "itemRemover";
	}

	@Override
	public boolean doAction(String unformatted, String formatted) {
		Matcher itemRemoverMessage = itemRemoverMessageRegex.matcher(unformatted);
		Matcher itemRemoverDoneMessage = itemRemoverDoneMessageRegex.matcher(unformatted);
		if (unformatted.trim().length() > 0 && (itemRemoverMessage.find() || itemRemoverDoneMessage.find()))
			return true;

		return false;
	}

	@Override
	public boolean doActionHandleChatMessage(String unformatted, String formatted) {
		return (doAction(unformatted, formatted) && getSettings().isItemRemoverChatRight());
	}

	@Override
	public boolean doActionModifyChatMessage(ITextComponent msg) {
		String unformatted = msg.getUnformattedText();

		Matcher itemRemoverDoneMessage = itemRemoverDoneMessageRegex.matcher(unformatted);

		return (itemRemoverDoneMessage.find());
	}

	@Override
	public ChatDisplayAction handleChatMessage(String unformatted, String formatted) {
		if (doAction(unformatted, formatted)) {
			return ChatDisplayAction.SWAP;
		}
		return super.handleChatMessage(unformatted, formatted);
	}

	@Override
	public ITextComponent modifyChatMessage(ITextComponent msg) {
		if (getSettings().isItemRemoverLastTimeHover() && doActionModifyChatMessage(msg)) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
			String dateNowStr = LocalDateTime.now().format(formatter);

			ITextComponent hoverText = new TextComponentString(dateNowStr);
			msg.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
		}
		return super.modifyChatMessage(msg);
	}
}
