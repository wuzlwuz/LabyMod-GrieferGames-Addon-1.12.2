package de.wuzlwuz.griefergames.chat;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.authlib.GameProfile;

import net.labymod.main.LabyMod;
import net.labymod.main.lang.LanguageManager;
import net.labymod.servermanager.ChatDisplayAction;
import net.labymod.utils.UUIDFetcher;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;

public class Payment extends Chat {
	private static Pattern getMoneyValidRegex = Pattern.compile(
			"^([A-Za-z\\-]+\\+?) \\| (\\w{1,16}) hat dir \\$((?:[1-9]\\d{0,2}(?:,\\d{1,3})*|0)(?:\\.\\d+)?) gegeben\\.$");
	private static Pattern payedMoneyRegex = Pattern.compile(
			"^Du hast ([A-Za-z\\-]+\\+?) \\| (\\w{1,16}) \\$((?:[1-9]\\d{0,2}(?:,\\d{1,3})*|0)(?:\\.\\d+)?) gegeben\\.$");
	private static Pattern earnedMoneyRegex = Pattern
			.compile("\\$((?:[1-9]\\d{0,2}(?:,\\d{1,3})*|0)(?:\\.\\d+)?) wurde zu deinem Konto hinzugef\\u00FCgt");
	private static Pattern getMoneyRegex = Pattern.compile("\\$((?:[1-9]\\d{0,2}(?:,\\d{1,3})*|0)(?:\\.\\d+)?)");

	@Override
	public String getName() {
		return "payment";
	}

	@Override
	public boolean doAction(String unformatted, String formatted) {
		if (getHelper().getProperTextFormat(formatted).indexOf("§r §r§ahat dir $") >= 0) {
			Matcher matcher = getMoneyValidRegex.matcher(unformatted);
			if (matcher.find()) {
				return true;
			}
		}

		Matcher payedMoney = payedMoneyRegex.matcher(unformatted);
		Matcher earnedMoney = earnedMoneyRegex.matcher(unformatted);
		if (payedMoney.find() || earnedMoney.find()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean doActionHandleChatMessage(String unformatted, String formatted) {
		return (doAction(unformatted, formatted) && getSettings().isPayChatRight());
	}

	@Override
	public boolean doActionModifyChatMessage(ITextComponent msg) {
		String unformatted = msg.getUnformattedText();
		String formatted = msg.getFormattedText();

		if (getHelper().getProperTextFormat(formatted).indexOf("§r §r§ahat dir $") >= 0) {
			Matcher matcher = getMoneyValidRegex.matcher(unformatted);
			if (matcher.find()) {
				return (getSettings().isPayHover() || getSettings().isPayMarker());
			}
		}

		return false;
	}

	@Override
	public ChatDisplayAction handleChatMessage(String unformatted, String formatted) {
		if (getSettings().isPayAchievement()) {
			String payerName = getHelper().getPlayerName(unformatted);
			String displayName = getHelper().getDisplayName(unformatted);
			UUID playerUUID = UUIDFetcher.getUUID(payerName);
			double money = getMoneyPay(unformatted);
			if (money > 0) {
				DecimalFormat moneyFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.ENGLISH);

				String title = LanguageManager.translateOrReturnKey("message_gg_gotMoney", new Object[0]);
				String desc = LanguageManager.translateOrReturnKey("message_gg_gotMoneyFrom", new Object[0]);

				Matcher payedMoney = payedMoneyRegex.matcher(unformatted);
				if (payedMoney.find()) {
					title = LanguageManager.translateOrReturnKey("message_gg_paidMoney", new Object[0]);
					desc = LanguageManager.translateOrReturnKey("message_gg_paidMoneyTo", new Object[0]);
				}

				title = title.replace("{money}", moneyFormat.format(money));
				desc = desc.replace("{money}", moneyFormat.format(money));
				desc = desc.replace("{name}", displayName);

				if (playerUUID == null) {
					LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(title, desc);
				} else {
					LabyMod.getInstance().getGuiCustomAchievement()
							.displayAchievement(new GameProfile(playerUUID, payerName), title, desc);
				}
			}
		}

		return ChatDisplayAction.SWAP;
	}

	@Override
	public ITextComponent modifyChatMessage(ITextComponent msg) {
		if (getSettings().isPayMarker()) {
			ITextComponent checkmarkText = new TextComponentString(" \u2714")
					.setStyle(new Style().setColor(TextFormatting.GREEN));
			msg.appendSibling(checkmarkText);
		}

		if (getSettings().isPayHover()) {
			String ValidPayment = LanguageManager.translateOrReturnKey("message_gg_validPayment", new Object[0]);

			ITextComponent hoverText = new TextComponentString(ValidPayment);
			msg.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
		}

		return msg;
	}

	public double getMoneyPay(String unformatted) {
		double money = 0.0;
		Matcher matcher = getMoneyRegex.matcher(unformatted);
		if (matcher.find()) {
			String moneyStr = matcher.group(1).trim();
			if (moneyStr.length() > 0) {
				moneyStr = moneyStr.replace("$", "");
				moneyStr = moneyStr.replaceAll(",", "");
				try {
					money = Double.parseDouble(moneyStr);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
		return money;
	}
}