package de.wuzlwuz.griefergames.server;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import de.wuzlwuz.griefergames.GrieferGames;
import de.wuzlwuz.griefergames.helper.MessageHelper;
import de.wuzlwuz.griefergames.listener.SubServerListener;
import net.labymod.api.LabyModAPI;
import net.labymod.api.events.MessageModifyChatEvent;
import net.labymod.api.events.TabListEvent;
import net.labymod.core.LabyModCore;
import net.labymod.main.LabyMod;
import net.labymod.servermanager.ChatDisplayAction;
import net.labymod.servermanager.Server;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.UUIDFetcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.PacketBuffer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class GrieferGamesServer extends Server {
	private List<SubServerListener> subServerListener = new ArrayList<SubServerListener>();
	private Minecraft mc;
	private LabyModAPI api;
	private MessageHelper msgHelper;
	private String subServer = "Lobby";
	private long nextLastMessageRequest = System.currentTimeMillis()
			+ (-GrieferGames.getSettings().getFilterDuplicateMessagesTime() * 1000L);
	private long nextScoreboardRequest = System.currentTimeMillis() + (-1 * 1000L);
	private String lastMessage = "";
	private boolean doClearChat = false;
	private boolean changedSubserver = false;

	public Minecraft getMc() {
		return mc;
	}

	public void setMc(Minecraft mc) {
		this.mc = mc;
	}

	public LabyModAPI getApi() {
		return api;
	}

	public void addSubServerListener(SubServerListener ssl) {
		subServerListener.add(ssl);
	}

	private void setApi(LabyModAPI api) {
		this.api = api;
	}

	public MessageHelper getMsgHelper() {
		return msgHelper;
	}

	private void setMsgHelper(MessageHelper msgHelper) {
		this.msgHelper = msgHelper;
	}

	public GrieferGamesServer(Minecraft minecraft) {
		super("GrieferGames", "griefergames.net");
		setMc(minecraft);
		setApi(GrieferGames.getGriefergames().getApi());
		setMsgHelper(new MessageHelper());
		addSubServerListener(new SubServerListener() {
			@Override
			public void onSubServerChanged(String subServerNameOld, String subServerName) {
				if (GrieferGames.getSettings().isModEnabled() && GrieferGames.getSettings().isServerSwitchMsg()) {
					TextComponentString switchServerMSG = new TextComponentString("");

					TextComponentString switchServerMSGBefore = new TextComponentString(
							"\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500[");
					switchServerMSGBefore.func_150255_a(new Style().func_150238_a(TextFormatting.WHITE));
					switchServerMSG.func_150257_a(switchServerMSGBefore);

					TextComponentString switchServerMSGBetween = new TextComponentString("SubServer: " + subServerName);
					switchServerMSGBetween.func_150255_a(new Style().func_150238_a(TextFormatting.AQUA).func_150227_a(true));
					switchServerMSG.func_150257_a(switchServerMSGBetween);

					TextComponentString switchServerMSGAfter = new TextComponentString(
							"]\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
					switchServerMSGAfter.func_150255_a(new Style().func_150238_a(TextFormatting.WHITE));
					switchServerMSG.func_150257_a(switchServerMSGAfter);

					getMc().field_71439_g.func_145747_a(switchServerMSG);
				}
			}
		});
	}

	public String getSubServer() {
		return subServer;
	}

	private void setSubServer(String subServer) {
		this.subServer = subServer.trim();
	}

	public String getLastMessage() {
		return lastMessage;
	}

	private void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	private boolean DoClearChat() {
		return this.doClearChat;
	}

	private void setDoClearChat(boolean doClearChat) {
		this.doClearChat = doClearChat;
	}

	private boolean hasChangedSubserver() {
		return this.changedSubserver;
	}

	private void setChangedSubserver(boolean changedSubserver) {
		this.changedSubserver = changedSubserver;
	}

	@Override
	public ChatDisplayAction handleChatMessage(String unformatted, String formatted) throws Exception {
		if (GrieferGames.getSettings().isModEnabled()) {
			try {
				if (getMsgHelper().isBlankMessage(unformatted)) {
					return GrieferGames.getSettings().isCleanBlanks() && !DoClearChat() && !hasChangedSubserver()
							? ChatDisplayAction.HIDE
							: ChatDisplayAction.NORMAL;
				}

				if (GrieferGames.getSettings().isFilterDuplicateMessages() && getLastMessage().equals(formatted)) {
					return ChatDisplayAction.HIDE;
				}

				setLastMessage(formatted);

				if (getMsgHelper().isSupremeBlank(unformatted, formatted) > 0) {
					return GrieferGames.getSettings().isCleanSupremeBlanks() ? ChatDisplayAction.HIDE
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().isValidPayMessage(unformatted, formatted) > 0) {
					if (GrieferGames.getSettings().isPayAchievement()) {
						String payerName = getMsgHelper().getPayerName(unformatted);
						String displayName = getMsgHelper().getDisplayName(unformatted);
						UUID playerUUID = UUIDFetcher.getUUID(payerName);
						double money = getMsgHelper().getMoneyPay(unformatted);
						if (money > 0) {
							DecimalFormat moneyFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.ENGLISH);
							if (playerUUID == null) {
								LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(
										"$" + moneyFormat.format(money) + " erhalten.",
										"Du hast $" + moneyFormat.format(money) + " von " + displayName + " erhalten.");
							} else {
								LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(
										new GameProfile(playerUUID, payerName),
										"$" + moneyFormat.format(money) + " erhalten.",
										"Du hast $" + moneyFormat.format(money) + " von " + displayName + " erhalten.");
							}
						}
					}

					return GrieferGames.getSettings().isPayChatRight() ? ChatDisplayAction.SWAP
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().hasPayedMessage(unformatted, formatted) > 0) {
					if (GrieferGames.getSettings().isPayAchievement()) {
						String payerName = getMsgHelper().getPayerName(unformatted);
						String displayName = getMsgHelper().getDisplayName(unformatted);
						UUID playerUUID = UUIDFetcher.getUUID(payerName);
						double money = getMsgHelper().getMoneyPay(unformatted);
						if (money > 0) {
							DecimalFormat moneyFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.ENGLISH);
							if (playerUUID == null) {
								LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(
										"$" + moneyFormat.format(money) + " bezahlt.",
										"Du hast $" + moneyFormat.format(money) + " an " + displayName + " bezahlt.");
							} else {
								LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(
										new GameProfile(playerUUID, payerName),
										"$" + moneyFormat.format(money) + " bezahlt.",
										"Du hast $" + moneyFormat.format(money) + " an " + displayName + " bezahlt.");
							}
						}
					}
					return GrieferGames.getSettings().isPayChatRight() ? ChatDisplayAction.SWAP
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().bankPayInMessage(unformatted, formatted) > 0) {
					if (GrieferGames.getSettings().isBankAchievement()) {
						int money = getMsgHelper().getMoneyBank(unformatted);
						if (money > 0) {
							DecimalFormat moneyFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.ENGLISH);
							LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(
									"$" + moneyFormat.format(money) + " eingezahlt.",
									"Du hast $" + moneyFormat.format(money) + " in die Bank eingezahlt.");
						}
					}
					return GrieferGames.getSettings().isBankChatRight() ? ChatDisplayAction.SWAP
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().bankPayOutMessage(unformatted, formatted) > 0) {
					if (GrieferGames.getSettings().isBankAchievement()) {
						int money = getMsgHelper().getMoneyBank(unformatted);
						if (money > 0) {
							DecimalFormat moneyFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.ENGLISH);
							LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(
									"$" + moneyFormat.format(money) + " abgehoben.",
									"Du hast $" + moneyFormat.format(money) + " von der Bank abgehoben.");
						}
					}
					return GrieferGames.getSettings().isBankChatRight() ? ChatDisplayAction.SWAP
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().bankBalanceMessage(unformatted, formatted) > 0) {
					return GrieferGames.getSettings().isBankChatRight() ? ChatDisplayAction.SWAP
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().bankMessageOther(unformatted, formatted) > 0) {
					return GrieferGames.getSettings().isBankChatRight() ? ChatDisplayAction.SWAP
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().isValidPrivateMessage(unformatted, formatted) > 0) {
					return GrieferGames.getSettings().isPrivateChatRight() ? ChatDisplayAction.SWAP
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().isValidSendPrivateMessage(unformatted, formatted) > 0) {
					return GrieferGames.getSettings().isPrivateChatRight() ? ChatDisplayAction.SWAP
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().clearLagMessage(unformatted, formatted) > 0) {
					return GrieferGames.getSettings().isClearlagChatRight() ? ChatDisplayAction.SWAP
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().mobRemoverMessage(unformatted, formatted) > 0) {
					return GrieferGames.getSettings().isMobRemoverChatRight() ? ChatDisplayAction.SWAP
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().mobRemoverDoneMessage(unformatted, formatted) > 0) {
					return GrieferGames.getSettings().isMobRemoverChatRight() ? ChatDisplayAction.SWAP
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().isClearChatMessage(unformatted, formatted) > 0) {
					setDoClearChat(false);
					return ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().isPlotChatMessage(unformatted, formatted) > 0) {
					return GrieferGames.getSettings().isPlotChatRight() ? ChatDisplayAction.SWAP
							: ChatDisplayAction.NORMAL;
				} else if (hasChangedSubserver()) {
					setChangedSubserver(false);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ChatDisplayAction.NORMAL;
	}

	@Override
	public void fillSubSettings(List<SettingsElement> settings) {

	}

	@Override
	public void handlePluginMessage(String channelName, PacketBuffer packetBuffer) throws Exception {

	}

	@Override
	public void handleTabInfoMessage(TabListEvent.Type tabInfoType, String formatted, String clean) throws Exception {

	}

	@Override
	public void onJoin(ServerData serverData) {
		this.getApi().getEventManager().register(new MessageModifyChatEvent() {
			@Override
			public Object onModifyChatMessage(Object o) {
				return modifyChatMessage(o);
			}
		});
		this.getApi().registerForgeListener(this);
	}

	public Object modifyChatMessage(Object o) {
		if (!GrieferGames.getSettings().isModEnabled())
			return o;

		try {
			ITextComponent msg = (ITextComponent) o;

			if (msg.func_150260_c().length() == 0)
				return msg;

			MessageHelper msgHelper = getMsgHelper();
			String unformatted = msg.func_150260_c();
			String formatted = msg.func_150254_d();

			System.out.println(formatted);

			if (GrieferGames.getSettings().isPayHover() || GrieferGames.getSettings().isPayMarker()) {
				if (msgHelper.isValidPayMessage(unformatted, formatted) > 0) {
					if (GrieferGames.getSettings().isPayMarker()) {
						ITextComponent checkmarkText = new TextComponentString(" \u2714")
								.func_150255_a(new Style().func_150238_a(TextFormatting.GREEN));
						msg.func_150257_a(checkmarkText);
					}
					if (GrieferGames.getSettings().isPayHover()) {
						ITextComponent hoverText = new TextComponentString("Es ist eine valide Zahlung!");
						msg.func_150256_b().func_150209_a(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
					}
				}
			}
			if (msgHelper.isClearChatMessage(unformatted, formatted) > 0) {
				setDoClearChat(true);
				ITextComponent newMsg = new TextComponentString("\n");
				for (int i = 0; i < 100; i++) {
					newMsg.func_150257_a(new TextComponentString("\n"));
				}
				newMsg.func_150257_a(msg);
				return newMsg;
			}
			if (GrieferGames.getSettings().isBetterIgnoreList()
					&& msgHelper.isIngnoreListChatMessage(unformatted, formatted) > 0) {
				List<ITextComponent> ignoreList = msg.func_150253_a();
				if (ignoreList.size() == 2) {
					Style ignoStyle = ignoreList.get(0).func_150256_b().func_150206_m();
					ITextComponent newMsg = new TextComponentString("Ignoriert: ").func_150255_a(ignoStyle);

					String ignoredNames = ignoreList.get(1).func_150260_c().trim();
					String[] ignoredNamesArr = ignoredNames.split(" ");
					for (String ignoName : ignoredNamesArr) {
						newMsg.func_150257_a(new TextComponentString("\n"));
						newMsg.func_150257_a(new TextComponentString(" - " + ignoName)
								.func_150255_a(new Style().func_150238_a(TextFormatting.WHITE)));
					}

					return newMsg;
				}
			}
			if (GrieferGames.getSettings().isMobRemoverLastTimeHover()
					&& msgHelper.mobRemoverDoneMessage(unformatted, formatted) > 0) {

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
				String dateNowStr = LocalDateTime.now().format(formatter);

				ITextComponent hoverText = new TextComponentString(dateNowStr);
				msg.func_150256_b().func_150209_a(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (LabyModCore.getMinecraft().getWorld() != null && event.phase == TickEvent.Phase.START) {
			if (System.currentTimeMillis() > this.nextLastMessageRequest) {
				this.nextLastMessageRequest = System.currentTimeMillis()
						+ GrieferGames.getSettings().getFilterDuplicateMessagesTime() * 1000L;
				setLastMessage("");
			}
			if (System.currentTimeMillis() > this.nextScoreboardRequest) {
				this.nextScoreboardRequest = System.currentTimeMillis() + 500L;
				Scoreboard scoreboard = LabyModCore.getMinecraft().getWorld().func_96441_U();
				ScoreObjective scoreobjective = scoreboard.func_96539_a(1);
				if (scoreobjective != null) {
					List<Score> scoreList = (List<Score>) scoreboard.func_96534_i(scoreobjective);
					Collections.reverse(scoreList);
					for (int i = 0; i < scoreList.size(); i++) {
						ScorePlayerTeam scorePlayerTeam = scoreboard.func_96509_i(scoreList.get(i).func_96653_e());
						String scoreName = ScorePlayerTeam.func_96667_a(scorePlayerTeam,
								scoreList.get(i).func_96653_e());
						if (scoreName.indexOf("Server:") > 0) {
							scorePlayerTeam = scoreboard.func_96509_i(scoreList.get(i + 1).func_96653_e());
							scoreName = ScorePlayerTeam
									.func_96667_a(scorePlayerTeam, scoreList.get(i + 1).func_96653_e())
									.replaceAll("§[0-9a-z]", "").trim();
							if (!getSubServer().matches(scoreName)) {
								for (SubServerListener ssl : subServerListener)
									ssl.onSubServerChanged(getSubServer(), scoreName);
								setSubServer(scoreName);
							}
							i = scoreList.size();
						}
					}
				}
			}
		}
	}
}
