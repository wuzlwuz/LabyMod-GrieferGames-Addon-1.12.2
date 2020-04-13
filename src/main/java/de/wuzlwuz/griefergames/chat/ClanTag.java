package de.wuzlwuz.griefergames.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.labymod.main.lang.LanguageManager;
import net.labymod.utils.ModColor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class ClanTag extends Chat {
	private static Pattern clanTagRegex = Pattern.compile("^\\[([^\\]]+)\\] [A-Za-z\\-]+\\+? \\| (\\u007E)?\\w{1,16}");

	@Override
	public String getName() {
		return "clanTag";
	}

	@Override
	public boolean doAction(String unformatted, String formatted) {
		Matcher clanTag = clanTagRegex.matcher(unformatted);

		if (getSettings().isClanTagClick() && unformatted.trim().length() > 0 && clanTag.find())
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
			String clanTag = "/clan info " + getClanTagFromMessage(msg.getUnformattedText());
			boolean clickClanTag = true;
			ITextComponent newMsg = new TextComponentString("");
			for (ITextComponent component : msg.getSiblings()) {
				if (clickClanTag) {
					Style msgStyling = component.getStyle().createDeepCopy()
							.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clanTag));

					String clanTagClickHoverTxt = LanguageManager
							.translateOrReturnKey("message_gg_clanTagClickHoverTxt", new Object[0]);
					ITextComponent hoverText = new TextComponentString(ModColor.cl("a") + clanTagClickHoverTxt);

					msgStyling.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
					component.setStyle(msgStyling);

					clickClanTag = (component.getUnformattedText().indexOf("]") == -1);
				}
				newMsg.appendSibling(component);
			}
			return newMsg;
		}

		return msg;
	}

	private String getClanTagFromMessage(String unformatted) {
		String clanTag = "";
		Matcher clanTagMatch = clanTagRegex.matcher(unformatted);
		if (clanTagMatch.find()) {
			clanTag = clanTagMatch.group(1);
		}
		return clanTag;
	}
}