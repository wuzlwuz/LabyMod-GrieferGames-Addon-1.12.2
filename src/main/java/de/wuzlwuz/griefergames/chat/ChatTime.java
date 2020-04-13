package de.wuzlwuz.griefergames.chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class ChatTime extends Chat {
	ITextComponent resetMsg = new TextComponentString(" ").setStyle(new Style().setColor(TextFormatting.RESET));

	@Override
	public String getName() {
		return "chatTime";
	}

	@Override
	public boolean doAction(String unformatted, String formatted) {
		return getSettings().isShowChatTime();
	}

	@Override
	public boolean doActionModifyChatMessage(ITextComponent msg) {
		String unformatted = msg.getUnformattedText();
		String formatted = msg.getFormattedText();

		return (doAction(unformatted, formatted) && true);
	}

	@Override
	public ITextComponent modifyChatMessage(ITextComponent msg) {
		String unformatted = msg.getUnformattedText();
		String formatted = msg.getFormattedText();

		if (doAction(unformatted, formatted)) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
			String dateNowStr = LocalDateTime.now().format(formatter);

			ITextComponent befTimeMsg = new TextComponentString("[")
					.setStyle(new Style().setColor(TextFormatting.GOLD));
			ITextComponent timeMsg = new TextComponentString(dateNowStr)
					.setStyle(new Style().setColor(TextFormatting.WHITE));
			ITextComponent aftTimeMsg = new TextComponentString("]")
					.setStyle(new Style().setColor(TextFormatting.GOLD));

			ITextComponent newMsg = new TextComponentString("").appendSibling(befTimeMsg).appendSibling(timeMsg)
					.appendSibling(aftTimeMsg).appendSibling(resetMsg).appendSibling(msg);

			return newMsg;
		}

		return super.modifyChatMessage(msg);
	}
}
