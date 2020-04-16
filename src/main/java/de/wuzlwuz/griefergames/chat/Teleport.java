package de.wuzlwuz.griefergames.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class Teleport extends Chat {
	private static Pattern tpaMesssageRegexp = Pattern
			.compile("^([A-Za-z\\-]+\\+?) \\| ((\\u007E)?\\w{1,16}) fragt, ob er sich zu dir teleportieren darf.$");
	private static Pattern tpahereMesssageRegexp = Pattern
			.compile("^([A-Za-z\\-]+\\+?) \\| ((\\u007E)?\\w{1,16}) fragt, ob du dich zu ihm teleportierst.$");

	@Override
	public String getName() {
		return "teleport";
	}

	@Override
	public boolean doAction(String unformatted, String formatted) {
		Matcher tpaMesssage = tpaMesssageRegexp.matcher(unformatted);
		Matcher tpahereMesssage = tpahereMesssageRegexp.matcher(unformatted);

		if (getSettings().isMarkTPAMsg() && unformatted.trim().length() > 0
				&& (tpaMesssage.find() || tpahereMesssage.find()))
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
		String unformatted = msg.getUnformattedText();

		if (doActionModifyChatMessage(msg)) {
			Matcher tpaMesssage = tpaMesssageRegexp.matcher(unformatted);
			Matcher tpahereMesssage = tpahereMesssageRegexp.matcher(unformatted);

			if (tpaMesssage.find()) {
				ITextComponent beforeTpaMsg = new TextComponentString("[TPA] ")
						.setStyle(new Style().setColor(TextFormatting.DARK_GREEN).setBold(true));
				ITextComponent newMsg = new TextComponentString("").appendSibling(beforeTpaMsg).appendSibling(msg);
				return newMsg;
			}

			if (tpahereMesssage.find()) {
				ITextComponent beforeTpaMsg = new TextComponentString("[TPAHERE] ")
						.setStyle(new Style().setColor(TextFormatting.RED).setBold(true));
				ITextComponent newMsg = new TextComponentString("").appendSibling(beforeTpaMsg).appendSibling(msg);
				return newMsg;
			}
		}

		return super.modifyChatMessage(msg);
	}
}