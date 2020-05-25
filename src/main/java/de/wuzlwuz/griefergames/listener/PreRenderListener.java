package de.wuzlwuz.griefergames.listener;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.wuzlwuz.griefergames.GrieferGames;
import net.labymod.core.LabyModCore;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PreRenderListener {
	private static Pattern antiMagixPrefixRegex = Pattern.compile("([A-Za-z\\-]+\\+?) \\u2503 ((\\u007E)?\\w{1,16})");

	@SubscribeEvent
	public void onPreRender(RenderGameOverlayEvent event) {
		if (GrieferGames.getGriefergames().getGGServer().getMC().gameSettings.keyBindPlayerList.isKeyDown()
				&& !GrieferGames.getGriefergames().getGGServer().getMC().isIntegratedServerRunning()
				&& GrieferGames.getSettings().isAMPEnabled()) {

			ScoreObjective scoreobjective = LabyModCore.getMinecraft().getWorld().getScoreboard()
					.getObjectiveInDisplaySlot(0);
			NetHandlerPlayClient handler = LabyModCore.getMinecraft().getPlayer().connection;
			if (handler.getPlayerInfoMap().size() > 1 || scoreobjective != null) {
				Collection<NetworkPlayerInfo> players = handler.getPlayerInfoMap();
				for (NetworkPlayerInfo player : players) {
					if (player.getDisplayName() != null) {
						ITextComponent playerDisplayName = (ITextComponent) player.getDisplayName();

						if (playerDisplayName.getUnformattedText().length() > 0
								&& GrieferGames.getSettings().isAMPEnabled()) {
							String oldPlayerDisplayName = GrieferGames.getGriefergames().getHelper()
									.getProperTextFormat(playerDisplayName.getFormattedText());
							if (oldPlayerDisplayName.indexOf("§k") != -1) {
								Matcher antiMagixPrefix = antiMagixPrefixRegex
										.matcher(playerDisplayName.getUnformattedText());
								ITextComponent newPlayerDisplayName = new TextComponentString("");
								if (antiMagixPrefix.find()) {
									for (ITextComponent displayName : playerDisplayName.getSiblings()) {
										// if (displayName.getStyle().getObfuscated() &&
										// displayName.getUnformattedText().matches("(([A-Za-z\\-]+\\+?) \\u2503
										// ((\\u007E)?\\w{1,16}))")) {
										if (displayName.getStyle().getObfuscated() && displayName.getUnformattedText()
												.equalsIgnoreCase(antiMagixPrefix.group(1))) {

											Style playerDisplayNameStyling = displayName.getStyle().createDeepCopy()
													.setObfuscated(false);
											String chatRepText = GrieferGames.getSettings().getAMPTablistReplacement();

											if (chatRepText.indexOf("%CLEAN%") == -1) {
												chatRepText = GrieferGames.getSettings()
														.getDefaultAMPTablistReplacement();
											}

											chatRepText = chatRepText.replaceAll("%CLEAN%",
													displayName.getUnformattedText());
											chatRepText = "${REPSTART}" + chatRepText + "${REPEND}";

											newPlayerDisplayName.appendSibling(new TextComponentString(
													chatRepText.replace("${REPSTART}", "").replace("${REPEND}", ""))
															.setStyle(playerDisplayNameStyling));
										} else if (displayName.getStyle().getObfuscated() && displayName
												.getUnformattedText().equalsIgnoreCase(antiMagixPrefix.group(2))) {
											Style playerDisplayNameStyling = displayName.getStyle().createDeepCopy()
													.setObfuscated(false);
											newPlayerDisplayName
													.appendSibling(displayName.setStyle(playerDisplayNameStyling));
										} else {
											newPlayerDisplayName.appendSibling(displayName);
										}
									}

									player.setDisplayName(newPlayerDisplayName);
								}
							}
						}
					}
				}
			}
		}
	}
}