package de.wuzlwuz.griefergames.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.labymod.servermanager.ChatDisplayAction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class Realname extends Chat {
	private static Pattern realnameRegex = Pattern
			.compile("^([A-Za-z\\-]+\\+?) \\u2503 ((\\u007E)?\\w{1,16}) ist (\\w{1,16})$");
	private static Pattern realnameRegex2 = Pattern.compile("§r ist ((\\u007E)?\\w{1,16})§r$");

	private static Pattern realnameDupRegex = Pattern
			.compile("^\\$\\{\\{dup\\}\\}([A-Za-z\\-]+\\+?) \\u2503 ((\\u007E)?\\w{1,16}) ist (\\w{1,16})$");

	// private static Pattern isNickedPlayer = Pattern.compile("([A-Za-z\\-]+\\+?)
	// \\u2503 (\\u007E\\w{1,16})");

	@Override
	public String getName() {
		return "realname";
	}

	@Override
	public boolean doAction(String unformatted, String formatted) {
		Matcher realname = realnameRegex.matcher(unformatted);
		Matcher realname2 = realnameRegex2.matcher(getHelper().getProperTextFormat(formatted));
		if (getSettings().isRealnameRight() && unformatted.trim().length() > 0 && realname.find() && realname2.find()) {
			return true;
		}

		return false;
	}

	@Override
	public boolean doActionHandleChatMessage(String unformatted, String formatted) {
		return (doAction(unformatted, formatted));
	}

	@Override
	public ChatDisplayAction handleChatMessage(String unformatted, String formatted) {
		if (doAction(unformatted, formatted)) {
			if (getSettings().isRealnameBoth()) {
				getApi().displayMessageInChat("${{dup}}" + formatted);
			}

			return ChatDisplayAction.SWAP;
		}
		return super.handleChatMessage(unformatted, formatted);
	}

	@Override
	public boolean doActionModifyChatMessage(ITextComponent msg) {
		String unformatted = msg.getUnformattedText();

		Matcher realname = realnameDupRegex.matcher(unformatted);
		return (realname.find());
	}

	@Override
	public ITextComponent modifyChatMessage(ITextComponent msg) {
		ITextComponent newMsg = new TextComponentString(msg.getFormattedText().replace("${{dup}}", ""));

		return newMsg;
	}
}
