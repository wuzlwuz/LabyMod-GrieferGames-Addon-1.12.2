package de.wuzlwuz.griefergames.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Nickname extends Chat {
	// private static Pattern userInChatRegexp = Pattern.compile("([A-Za-z\\-]+\\+?
	// \\| \\w{1,16})(\\]| -> mir\\]|\\s:)");
	private static Pattern nicknameMsgRegex = Pattern
			.compile("^Dein Spitzname ist nun ([A-Za-z\\-]+\\+?) \\| (\\u007E)?(\\w{1,16}).$");

	@Override
	public String getName() {
		return "nickname";
	}

	@Override
	public boolean doAction(String unformatted, String formatted) {
		if (unformatted.trim().length() > 0) {
			Matcher nicknameMsg = nicknameMsgRegex.matcher(unformatted);
			if (nicknameMsg.find()) {
				getGG().setNickname(nicknameMsg.group(3));
			} else if (unformatted.trim().equalsIgnoreCase("Du hast keinen Spitznamen mehr.")) {
				getGG().setNickname("");
			}
		}

		return false;
	}

	@Override
	public boolean doActionHandleChatMessage(String unformatted, String formatted) {
		return doAction(unformatted, formatted);
	}

	/*
	 * @Override public boolean doActionModifyChatMessage(ITextComponent msg) {
	 * return (getSettings().isNickamesFilterChat() &&
	 * getGG().getNickedPlayers().size() > 0); }
	 *
	 * @Override public ITextComponent modifyChatMessage(ITextComponent msg) {
	 * String unformatted = msg.getUnformattedText();
	 *
	 * Matcher userInChat = userInChatRegexp.matcher(unformatted); if
	 * (userInChat.find()) { String userString = userInChat.group(1); String
	 * nickname = getHelper().getPlayerName(userString);
	 *
	 * if (getGG().getNickedPlayers().containsKey(nickname)) { boolean doReplacement
	 * = true; for (ITextComponent msgPart : msg.getSiblings()) { String AMPText =
	 * getSettings().getAMPTablistReplacement();
	 *
	 * if (AMPText.indexOf("%CLEAN%") == -1) { AMPText =
	 * getSettings().getDefaultAMPTablistReplacement(); }
	 *
	 * AMPText = AMPText.replaceAll("%CLEAN%", userString); AMPText = "${REPSTART}"
	 * + AMPText + "${REPEND}";
	 *
	 * if (doReplacement &&
	 * ((msgPart.getUnformattedText().trim().equalsIgnoreCase(userString)) ||
	 * (getSettings().isAMPEnabled() && doReplacement &&
	 * msgPart.getUnformattedText().trim().equalsIgnoreCase(AMPText)) ||
	 * (msgPart.getUnformattedText().trim().equalsIgnoreCase(nickname)))) { Style
	 * appendMsgStyle = new Style();
	 *
	 * ITextComponent hoverText = new TextComponentString( ModColor.cl("a") +
	 * "Realname: " + getGG().getNickedPlayers().get(nickname));
	 * appendMsgStyle.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
	 * hoverText)); appendMsgStyle.setClickEvent( new
	 * ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/realname " + nickname));
	 *
	 * ITextComponent newMsg = msg.createCopy();
	 *
	 * ITextComponent appendMsg = new TextComponentString(" " + ModColor.cl("r") +
	 * ModColor.cl("6") + "[" + ModColor.cl("f") + "RN" + ModColor.cl("6") +
	 * "]").setStyle(appendMsgStyle);
	 * newMsg.getSiblings().get(newMsg.getSiblings().indexOf(msgPart)).appendSibling
	 * (appendMsg);
	 *
	 * doReplacement = false; msg = newMsg; } } } } return msg; }
	 */
}
