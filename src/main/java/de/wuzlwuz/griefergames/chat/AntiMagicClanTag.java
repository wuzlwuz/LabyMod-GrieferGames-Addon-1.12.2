package de.wuzlwuz.griefergames.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;

public class AntiMagicClanTag extends Chat {
	private static Pattern antiMagicClanTagRegex = Pattern
			.compile("^(\\[[^\\]]+\\] ([A-Za-z\\-]+\\+?) \\| (\\w{1,16}))");

	@Override
	public String getName() {
		return "antiMagicClanTag";
	}

	@Override
	public boolean doAction(String unformatted, String formatted) {
		String oldMessage = getHelper().getProperTextFormat(formatted);

		Matcher antiMagicClanTag = antiMagicClanTagRegex.matcher(unformatted);

		if (getSettings().isAMPClanEnabled() && unformatted.trim().length() > 0
				&& (oldMessage.indexOf("§k") != -1 || oldMessage.indexOf("§m") != -1) && antiMagicClanTag.find())
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
			boolean delClanTagMagic = true;
			ITextComponent newMsg = new TextComponentString("");
			for (ITextComponent component : msg.getSiblings()) {
				delClanTagMagic = (component.getUnformattedText().indexOf("]") == -1);

				if ((component.getStyle().getObfuscated() || component.getStyle().getStrikethrough())
						&& delClanTagMagic) {
					Style msgStyling = component.getStyle().createDeepCopy().setObfuscated(false)
							.setStrikethrough(false);
					component.setStyle(msgStyling);
					newMsg.appendSibling(component);
				} else {
					newMsg.appendSibling(component);
				}
			}
			return newMsg;
		}

		return msg;
	}
}