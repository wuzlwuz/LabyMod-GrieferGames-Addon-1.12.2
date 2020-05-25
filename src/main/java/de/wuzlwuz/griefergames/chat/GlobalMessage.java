package de.wuzlwuz.griefergames.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.labymod.core.LabyModCore;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;

public class GlobalMessage extends Chat {
	// private static Pattern msgUserGlobalChatRegex =
	// Pattern.compile("^([A-Za-z\\-]+\\+?) \\u2503 (\\w{1,16})\\s\\u00BB");
	private static Pattern msgUserGlobalChatRegex = Pattern
			.compile("^([A-Za-z\\-]+\\+?) \\u2503 ((\\u007E)?\\w{1,16})");
	private static Pattern msgUserGlobalChatClanRegex = Pattern
			.compile("^(\\[[^\\]]+\\])\\s([A-Za-z\\-]+\\+?) \\u2503 ((\\u007E)?\\w{1,16})\\s\\u00BB");

	@Override
	public String getName() {
		return "globalMessage";
	}

	@Override
	public boolean doAction(String unformatted, String formatted) {
		Matcher matcher = msgUserGlobalChatRegex.matcher(unformatted);
		Matcher matcher2 = msgUserGlobalChatClanRegex.matcher(unformatted);

		if (matcher.find() && !getUserFromGlobalMessage(unformatted)
				.equalsIgnoreCase(LabyModCore.getMinecraft().getPlayer().getName().trim())) {
			return true;
		} else if (matcher2.find() && !getUserFromGlobalMessage(unformatted)
				.equalsIgnoreCase(LabyModCore.getMinecraft().getPlayer().getName().trim())) {
			return true;
		}
		return false;
	}

	@Override
	public boolean doActionModifyChatMessage(ITextComponent msg) {
		String unformatted = msg.getUnformattedText();
		String formatted = msg.getFormattedText();

		return (getSettings().isMsgDisplayNameClick() && doAction(unformatted, formatted));
	}

	@Override
	public ITextComponent modifyChatMessage(ITextComponent msg) {
		String unformatted = msg.getUnformattedText();

		String username = "/msg " + getUserFromGlobalMessage(unformatted) + " ";

		int siblingCnt = 0;
		int nameStart = 0;
		int nameEnd = 0;
		for (ITextComponent msgs : msg.getSiblings()) {
			if (msgs.getUnformattedText().equals("] ") && nameStart == 0) {
				nameStart = siblingCnt + 1;
			}

			if (msgs.getUnformattedText().trim().equalsIgnoreCase("\u00BB")) {
				nameEnd = siblingCnt - 1;
			}
			siblingCnt++;
		}

		// String suggestMsgHoverTxt =
		// LanguageManager.translateOrReturnKey("message_gg_suggestMsgHoverMsg", new
		// Object[0]);
		// ITextComponent hoverText = new TextComponentString(ModColor.cl("a") +
		// suggestMsgHoverTxt);

		for (int i = nameStart; i <= nameEnd; i++) {
			msg.getSiblings().get(i).getStyle()
					.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, username));
			// .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
		}

		return msg;
	}

	private String getUserFromGlobalMessage(String unformatted) {
		String displayName = "";
		Matcher msgUserGlobalChat = msgUserGlobalChatRegex.matcher(unformatted);
		Matcher msgUserGlobalChatClan = msgUserGlobalChatClanRegex.matcher(unformatted);
		if (msgUserGlobalChat.find()) {
			displayName = msgUserGlobalChat.group(2);
		} else if (msgUserGlobalChatClan.find()) {
			displayName = msgUserGlobalChatClan.group(3);
		}
		return displayName;
	}
}
