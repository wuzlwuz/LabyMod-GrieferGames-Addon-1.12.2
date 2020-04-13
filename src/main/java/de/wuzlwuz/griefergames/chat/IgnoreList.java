package de.wuzlwuz.griefergames.chat;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;

public class IgnoreList extends Chat {
	private static Pattern ignoreListRegex = Pattern.compile("^Ignoriert:((\\s\\w{1,16})+)$");

	@Override
	public String getName() {
		return "ignoreList";
	}

	@Override
	public boolean doAction(String unformatted, String formatted) {
		Matcher ignoreList = ignoreListRegex.matcher(unformatted);
		if (getSettings().isBetterIgnoreList() && ignoreList.find()) {
			return true;
		}
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
		List<ITextComponent> ignoreList = msg.getSiblings();
		if (ignoreList.size() == 2) {
			Style ignoStyle = ignoreList.get(0).getStyle().createDeepCopy();
			ITextComponent newMsg = new TextComponentString("Ignoriert:").setStyle(ignoStyle);

			String ignoredNames = ignoreList.get(1).getUnformattedText().trim();
			String[] ignoredNamesArr = ignoredNames.split(" ");
			for (String ignoName : ignoredNamesArr) {
				// newMsg.appendSibling(new TextComponentString("\n"));
				// newMsg.appendSibling(new TextComponentString(ignoName).setStyle(new
				// Style().setColor(TextFormatting.WHITE)));
				getApi().displayMessageInChat(ignoName);
			}
			msg = newMsg;
		}
		return msg;
	}
}