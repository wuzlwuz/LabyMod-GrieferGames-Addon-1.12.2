package de.wuzlwuz.griefergames.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;

public class AntiMagicPrefix extends Chat {
	private static Pattern antiMagixPrefixRegex = Pattern.compile("(([A-Za-z\\-]+\\+?) \\| ((\\u007E)?\\w{1,16}))");

	@Override
	public String getName() {
		return "antiMagicPrefix";
	}

	@Override
	public boolean doAction(String unformatted, String formatted) {
		String oldMessage = getHelper().getProperTextFormat(formatted);

		Matcher antiMagixPrefix = antiMagixPrefixRegex.matcher(unformatted);

		if (getSettings().isAMPEnabled() && unformatted.trim().length() > 0 && oldMessage.indexOf("§k") != -1
				&& antiMagixPrefix.find())
			return true;

		return false;
	}

	@Override
	public boolean doActionModifyChatMessage(ITextComponent msg) {
		String unformatted = msg.getUnformattedText();
		String formatted = msg.getFormattedText();

		return doAction(unformatted, formatted);
	}

	@Override
	public ITextComponent modifyChatMessage(ITextComponent msg) {
		if (doActionModifyChatMessage(msg)) {
			ITextComponent newMsg = new TextComponentString("");
			for (ITextComponent component : msg.getSiblings()) {
				Matcher antiMagixPrefix = antiMagixPrefixRegex.matcher(component.getUnformattedText());
				if (component.getStyle().getObfuscated() && antiMagixPrefix.find()) {
					Style msgStyling = component.getStyle().createDeepCopy().setObfuscated(false);
					String chatRepText = getSettings().getAMPChatReplacement();

					if (chatRepText.indexOf("%CLEAN%") == -1) {
						chatRepText = getSettings().getDefaultAMPChatReplacement();
					}

					chatRepText = chatRepText.replaceAll("%CLEAN%", component.getUnformattedText());
					chatRepText = "${REPSTART}" + chatRepText + "${REPEND}";

					newMsg.appendSibling(
							new TextComponentString(chatRepText.replace("${REPSTART}", "").replace("${REPEND}", ""))
									.setStyle(msgStyling));
				} else {
					newMsg.appendSibling(component);
				}
			}
			return newMsg;
		}

		return msg;
	}
}