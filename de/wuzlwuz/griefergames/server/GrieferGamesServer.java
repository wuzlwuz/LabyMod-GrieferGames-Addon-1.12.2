package de.wuzlwuz.griefergames.server;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import de.wuzlwuz.griefergames.GrieferGames;
import de.wuzlwuz.griefergames.booster.Booster;
import de.wuzlwuz.griefergames.helper.MessageHelper;
import de.wuzlwuz.griefergames.listener.SubServerListener;
import de.wuzlwuz.griefergames.modules.BoosterModule;
import de.wuzlwuz.griefergames.modules.FlyModule;
import de.wuzlwuz.griefergames.modules.GodmodeModule;
import de.wuzlwuz.griefergames.modules.VanishModule;
import net.labymod.api.LabyModAPI;
import net.labymod.api.events.MessageModifyChatEvent;
import net.labymod.api.events.TabListEvent;
import net.labymod.core.LabyModCore;
import net.labymod.ingamegui.ModuleCategoryRegistry;
import net.labymod.main.LabyMod;
import net.labymod.main.lang.LanguageManager;
import net.labymod.servermanager.ChatDisplayAction;
import net.labymod.servermanager.Server;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.UUIDFetcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.PacketBuffer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class GrieferGamesServer extends Server {
	private List<SubServerListener> subServerListener = new ArrayList<SubServerListener>();
	private Minecraft mc;
	private LabyModAPI api;
	private MessageHelper msgHelper;
	private String subServer = "";
	private long nextLastMessageRequest = System.currentTimeMillis()
			+ (-GrieferGames.getSettings().getFilterDuplicateMessagesTime() * 1000L);
	private long nextScoreboardRequest = System.currentTimeMillis() + (-1 * 1000L);
	private long nextPayAchievement = System.currentTimeMillis() + (-1 * 1000L);
	private long nextCheckFly = System.currentTimeMillis() + (-1 * 1000L);
	private String lastMessage = "";
	private boolean doClearChat = false;
	private boolean changedSubserver = false;
	private String playerRank = "Spieler";
	private boolean isInTeam = false;
	private boolean modulesLoaded = false;
	private String lastMsg = "";
	ITextComponent resetMsg = new TextComponentString(" ").setStyle(new Style().setColor(TextFormatting.RESET));

	private boolean listenerLoaded = false;

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
		if (!subServerListener.contains(ssl)) {
			subServerListener.add(ssl);
		}
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

	public String getPlayerRank() {
		return playerRank;
	}

	public void setPlayerRank(String playerRank) {
		this.playerRank = playerRank;
	}

	private void setIsInTeam(boolean isInTeam) {
		this.isInTeam = isInTeam;
	}

	public boolean getIsInTeam() {
		return this.isInTeam;
	}

	private void setModulesLoaded(boolean modulesLoaded) {
		this.modulesLoaded = modulesLoaded;
	}

	public boolean getModulesLoaded() {
		return this.modulesLoaded;
	}

	private void setListenerLoaded(boolean listenerLoaded) {
		this.listenerLoaded = listenerLoaded;
	}

	public boolean getListenerLoaded() {
		return this.listenerLoaded;
	}

	public GrieferGamesServer(Minecraft minecraft) {
		super("GrieferGames", GrieferGames.getGriefergames().getServerIp());
		setMc(minecraft);
		setApi(GrieferGames.getGriefergames().getApi());
		setMsgHelper(new MessageHelper());
		addSubServerListener(new SubServerListener() {
			@Override
			public void onSubServerChanged(String subServerNameOld, String subServerName) {
				if (subServerName.equalsIgnoreCase("lobby")) {
					GrieferGames.getGriefergames().setShowBoosterDummy(true);
					String accountName = LabyModCore.getMinecraft().getPlayer().getName().trim();

					try {
						NetHandlerPlayClient nethandlerplayclient = LabyModCore.getMinecraft().getPlayer().connection;
						Collection<NetworkPlayerInfo> playerMap = nethandlerplayclient.getPlayerInfoMap();

						for (NetworkPlayerInfo player : playerMap) {
							ITextComponent tabListName = player.getDisplayName();
							if (accountName.length() > 0 && accountName.equalsIgnoreCase(
									getMsgHelper().getPayerName(tabListName.getUnformattedText()).trim())) {

								setPlayerRank(getMsgHelper().getPayerRank(tabListName.getUnformattedText().trim()));
								setIsInTeam(getMsgHelper().isInTeam(getPlayerRank()));
							}
						}
						if (!getModulesLoaded()) {
							setModulesLoaded(true);

							ModuleCategoryRegistry.loadCategory(GrieferGames.getGriefergames().getModuleCategory());

							new BoosterModule();
							new FlyModule();
							if (getMsgHelper().showGodModule(getPlayerRank())) {
								new GodmodeModule();
							}
							if (getMsgHelper().showVanishModule(getPlayerRank())) {
								new VanishModule();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					GrieferGames.getGriefergames().setShowBoosterDummy(false);
					GrieferGames.getGriefergames().setBoosters(new ArrayList<Booster>());
				}

				GrieferGames.getGriefergames().setGodActive(false);
				GrieferGames.getGriefergames().setVanishActive(getMsgHelper().vanishDefaultState(getPlayerRank()));
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

	public String getLastMsg() {
		return lastMsg;
	}

	public void setLastMsg(String lastMsg) {
		this.lastMsg = lastMsg;
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

				getMsgHelper().isValidBoosterMessage(unformatted, formatted);
				getMsgHelper().isValidBoosterDoneMessage(unformatted, formatted);
				getMsgHelper().checkCurrentBoosters(unformatted, formatted);

				if (GrieferGames.getSettings().isUpdateBoosterState()
						&& getMsgHelper().isSwitcherDoneMsg(unformatted, formatted) > 0) {
					getMc().player.sendChatMessage("/booster");
				}

				if (GrieferGames.getSettings().isFilterDuplicateMessages() && getLastMessage().equals(formatted)) {
					return ChatDisplayAction.HIDE;
				}

				int status = getMsgHelper().isVanishMessage(unformatted, formatted);
				if (status >= 0) {
					GrieferGames.getGriefergames().setVanishActive(status > 0);
					return ChatDisplayAction.NORMAL;
				}

				status = getMsgHelper().isGodmodeMessage(unformatted, formatted);
				if (status >= 0) {
					GrieferGames.getGriefergames().setGodActive(status > 0);
					return ChatDisplayAction.NORMAL;
				}

				setLastMessage(formatted);

				if (getMsgHelper().isSupremeBlank(unformatted, formatted) > 0) {
					return GrieferGames.getSettings().isCleanSupremeBlanks() ? ChatDisplayAction.HIDE
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().isVoteMsg(unformatted, formatted) > 0) {
					return GrieferGames.getSettings().isCleanVoteMsg() ? ChatDisplayAction.HIDE
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().isValidPayMessage(unformatted, formatted) > 0) {
					if (GrieferGames.getSettings().isPayAchievement()) {
						String payerName = getMsgHelper().getPayerName(unformatted);
						String displayName = getMsgHelper().getDisplayName(unformatted);
						UUID playerUUID = UUIDFetcher.getUUID(payerName);
						double money = getMsgHelper().getMoneyPay(unformatted);
						if (money > 0) {
							DecimalFormat moneyFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.ENGLISH);

							String gotMoney = LanguageManager.translateOrReturnKey("message_gg_gotMoney",
									new Object[0]);
							String gotMoneyFrom = LanguageManager.translateOrReturnKey("message_gg_gotMoneyFrom",
									new Object[0]);

							gotMoney = gotMoney.replace("{money}", moneyFormat.format(money));
							gotMoneyFrom = gotMoneyFrom.replace("{money}", moneyFormat.format(money));
							gotMoneyFrom = gotMoneyFrom.replace("{name}", displayName);

							if (playerUUID == null) {
								LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(gotMoney,
										gotMoneyFrom);
							} else {
								LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(
										new GameProfile(playerUUID, payerName), gotMoney, gotMoneyFrom);
							}
						}
					}

					return GrieferGames.getSettings().isPayChatRight() ? ChatDisplayAction.SWAP
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().hasPayedMessage(unformatted, formatted) > 0) {
					if (GrieferGames.getSettings().isPayAchievement()
							&& System.currentTimeMillis() > nextPayAchievement) {
						nextPayAchievement = System.currentTimeMillis() + 1000L;
						String payerName = getMsgHelper().getPayerName(unformatted);
						String displayName = getMsgHelper().getDisplayName(unformatted);
						UUID playerUUID = UUIDFetcher.getUUID(payerName);
						double money = getMsgHelper().getMoneyPay(unformatted);
						if (money > 0) {
							DecimalFormat moneyFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.ENGLISH);

							String paidMoney = LanguageManager.translateOrReturnKey("message_gg_paidMoney",
									new Object[0]);
							String paidMoneyTo = LanguageManager.translateOrReturnKey("message_gg_paidMoneyTo",
									new Object[0]);

							paidMoney = paidMoney.replace("{money}", moneyFormat.format(money));
							paidMoneyTo = paidMoneyTo.replace("{money}", moneyFormat.format(money));
							paidMoneyTo = paidMoneyTo.replace("{name}", displayName);

							if (playerUUID == null) {
								LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(paidMoney,
										paidMoneyTo);
							} else {
								LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(
										new GameProfile(playerUUID, payerName), paidMoney, paidMoneyTo);
							}
						}
					}
					return GrieferGames.getSettings().isPayChatRight() ? ChatDisplayAction.SWAP
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().hasEarnedMoneyMessage(unformatted, formatted) > 0) {
					return GrieferGames.getSettings().isPayChatRight() ? ChatDisplayAction.SWAP
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().bankPayInMessage(unformatted, formatted) > 0) {
					if (GrieferGames.getSettings().isBankAchievement()) {
						int money = getMsgHelper().getMoneyBank(unformatted);
						if (money > 0) {
							DecimalFormat moneyFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.ENGLISH);

							String moneyDeposited = LanguageManager.translateOrReturnKey("message_gg_moneyDeposited",
									new Object[0]);
							String moneyDepositedBank = LanguageManager
									.translateOrReturnKey("message_gg_moneyDepositedBank", new Object[0]);

							moneyDeposited = moneyDeposited.replace("{money}", moneyFormat.format(money));
							moneyDepositedBank = moneyDepositedBank.replace("{money}", moneyFormat.format(money));

							LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(moneyDeposited,
									moneyDepositedBank);
						}
					}
					return GrieferGames.getSettings().isBankChatRight() ? ChatDisplayAction.SWAP
							: ChatDisplayAction.NORMAL;
				} else if (getMsgHelper().bankPayOutMessage(unformatted, formatted) > 0) {
					if (GrieferGames.getSettings().isBankAchievement()) {
						int money = getMsgHelper().getMoneyBank(unformatted);
						if (money > 0) {
							DecimalFormat moneyFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.ENGLISH);

							String moneyWithdrawn = LanguageManager.translateOrReturnKey("message_gg_moneyWithdrawn",
									new Object[0]);
							String moneyWithdrawnBank = LanguageManager
									.translateOrReturnKey("message_gg_moneyWithdrawnBank", new Object[0]);

							moneyWithdrawn = moneyWithdrawn.replace("{money}", moneyFormat.format(money));
							moneyWithdrawnBank = moneyWithdrawnBank.replace("{money}", moneyFormat.format(money));

							LabyMod.getInstance().getGuiCustomAchievement().displayAchievement(moneyWithdrawn,
									moneyWithdrawnBank);
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
				} else if (getMsgHelper().isClearChatMessage(unformatted, formatted) > 0 && !getIsInTeam()) {
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
		if (!getListenerLoaded()) {
			this.getApi().getEventManager().register(new MessageModifyChatEvent() {

				@Override
				@SubscribeEvent(priority = EventPriority.HIGH)
				public Object onModifyChatMessage(Object o) {
					return modifyChatMessage(o);
				}

			});
			this.getApi().registerForgeListener(this);
			setListenerLoaded(true);
		}
	}

	public Object modifyChatMessage(Object o) {
		if (!GrieferGames.getSettings().isModEnabled())
			return o;

		try {
			ITextComponent msg = (ITextComponent) o;

			MessageHelper msgHelper = getMsgHelper();
			String unformatted = msg.getUnformattedText();
			String formatted = msg.getFormattedText();

			if (msgHelper.isBlankMessage(unformatted) || msgHelper.isSupremeBlank(unformatted, formatted) > 0)
				return msg;

			String oldMessage = getMsgHelper().getProperTextFormat(formatted);

			if (GrieferGames.getSettings().isPayHover() || GrieferGames.getSettings().isPayMarker()) {
				if (msgHelper.isValidPayMessage(unformatted, formatted) > 0) {
					if (GrieferGames.getSettings().isPayMarker()) {
						ITextComponent checkmarkText = new TextComponentString(" \u2714")
								.setStyle(new Style().setColor(TextFormatting.GREEN));
						msg.appendSibling(checkmarkText);
					}
					if (GrieferGames.getSettings().isPayHover()) {

						String ValidPayment = LanguageManager.translateOrReturnKey("message_gg_validPayment",
								new Object[0]);

						ITextComponent hoverText = new TextComponentString(ValidPayment);
						msg.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
					}
				}
			}

			if (msgHelper.isClearChatMessage(unformatted, formatted) > 0 && !getIsInTeam()) {
				setDoClearChat(true);
				ITextComponent newMsg = new TextComponentString("\n");
				for (int i = 0; i < 100; i++) {
					newMsg.appendSibling(new TextComponentString("\n"));
				}
				newMsg.appendSibling(msg);
			}

			if (GrieferGames.getSettings().isBetterIgnoreList()
					&& msgHelper.isIngnoreListChatMessage(unformatted, formatted) > 0) {
				List<ITextComponent> ignoreList = msg.getSiblings();
				if (ignoreList.size() == 2) {
					Style ignoStyle = ignoreList.get(0).getStyle().createDeepCopy();
					ITextComponent newMsg = new TextComponentString("Ignoriert: ").setStyle(ignoStyle);

					String ignoredNames = ignoreList.get(1).getUnformattedText().trim();
					String[] ignoredNamesArr = ignoredNames.split(" ");
					for (String ignoName : ignoredNamesArr) {
						newMsg.appendSibling(new TextComponentString("\n"));
						newMsg.appendSibling(new TextComponentString(" - " + ignoName)
								.setStyle(new Style().setColor(TextFormatting.WHITE)));
					}
				}

			}

			if (GrieferGames.getSettings().isMobRemoverLastTimeHover()
					&& msgHelper.mobRemoverDoneMessage(unformatted, formatted) > 0) {

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
				String dateNowStr = LocalDateTime.now().format(formatter);

				ITextComponent hoverText = new TextComponentString(dateNowStr);
				msg.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
			}

			if (GrieferGames.getSettings().isMsgDisplayNameClick()
					&& msgHelper.isGlobalUserChatMessage(unformatted, formatted) > 0) {

				if (msg.getSiblings().size() > 3) {
					String username = "/msg " + getMsgHelper().getUserFromGlobalMessage(unformatted) + " ";
					msg.getSiblings().get(0).getStyle()
							.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, username));
					if (msg.getSiblings().size() > 4 && getMsgHelper()
							.getProperTextFormat(msg.getSiblings().get(3).getFormattedText()).equals("§8: §r")) {
						msg.getSiblings().get(1).getStyle()
								.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, username));
					}
				}
			}

			if (GrieferGames.getSettings().isMsgDisplayNameClick()
					&& msgHelper.isValidPrivateMessage(unformatted, formatted) > 0) {

				if (msg.getSiblings().size() > 5) {
					String username = "/msg " + getMsgHelper().getPrivateMessageName(unformatted) + " ";
					msg.getSiblings().get(1).getStyle()
							.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, username));
					if (getMsgHelper().getProperTextFormat(msg.getSiblings().get(5).getFormattedText())
							.equals("§6] §r")) {
						msg.getSiblings().get(2).getStyle()
								.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, username));
					}
				}
			}

			if (GrieferGames.getSettings().isMsgDisplayNameClick()
					&& msgHelper.isValidSendPrivateMessage(unformatted, formatted) > 0) {

				if (msg.getSiblings().size() > 5) {
					String username = "/msg " + getMsgHelper().getSentPrivateMessageName(unformatted) + " ";
					msg.getSiblings().get(3).getStyle()
							.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, username));
					if (getMsgHelper().getProperTextFormat(msg.getSiblings().get(5).getFormattedText())
							.equals("§6] §r")) {
						msg.getSiblings().get(4).getStyle()
								.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, username));
					}
				}
			}

			if (oldMessage.indexOf("§k") != -1 && GrieferGames.getSettings().isAMPEnabled()) {
				ITextComponent newMsg = new TextComponentString("");
				for (ITextComponent component : msg.getSiblings()) {
					if (component.getStyle().getObfuscated()
							&& component.getUnformattedText().matches("(([A-z\\-]+\\+?) \\| (\\w{1,16}))")) {
						Style msgStyling = component.getStyle().createDeepCopy().setObfuscated(false);
						String chatRepText = GrieferGames.getSettings().getAMPChatReplacement();

						if (chatRepText.indexOf("%CLEAN%") == -1) {
							chatRepText = GrieferGames.getSettings().getDefaultAMPChatReplacement();
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
				msg = newMsg;
			}

			if (GrieferGames.getSettings().isMarkTPAMsg() && msgHelper.isTPAHERE(unformatted, formatted) > 0) {
				ITextComponent beforeTpaMsg = new TextComponentString("[TPAHERE] ")
						.setStyle(new Style().setColor(TextFormatting.DARK_RED).setBold(true));
				ITextComponent newMsg = new TextComponentString("").appendSibling(beforeTpaMsg).appendSibling(msg);
				msg = newMsg;
			}

			if (GrieferGames.getSettings().isMarkTPAMsg() && msgHelper.isTPA(unformatted, formatted) > 0) {
				ITextComponent beforeTpaMsg = new TextComponentString("[TPA] ")
						.setStyle(new Style().setColor(TextFormatting.DARK_GREEN).setBold(true));
				ITextComponent newMsg = new TextComponentString("").appendSibling(beforeTpaMsg).appendSibling(msg);
				msg = newMsg;
			}

			if (GrieferGames.getSettings().isShowChatTime()) {
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

				msg = newMsg;
			}

			setLastMessage(msg.getFormattedText());

			return msg;
		} catch (

		Exception e) {
			e.printStackTrace();
		}
		return o;
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onTick(TickEvent.ClientTickEvent event) {
		if (LabyModCore.getMinecraft().getWorld() != null && event.phase == TickEvent.Phase.START) {
			if (System.currentTimeMillis() > this.nextLastMessageRequest) {
				this.nextLastMessageRequest = System.currentTimeMillis()
						+ GrieferGames.getSettings().getFilterDuplicateMessagesTime() * 1000L;
				setLastMessage("");
			}
			if (System.currentTimeMillis() > this.nextScoreboardRequest) {
				this.nextScoreboardRequest = System.currentTimeMillis() + 500L;
				Scoreboard scoreboard = LabyModCore.getMinecraft().getWorld().getScoreboard();
				ScoreObjective scoreobjective = scoreboard.getObjectiveInDisplaySlot(1);
				if (scoreobjective != null) {
					List<Score> scoreList = (List<Score>) scoreboard.getSortedScores(scoreobjective);
					Collections.reverse(scoreList);
					for (int i = 0; i < scoreList.size(); i++) {
						ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(scoreList.get(i).getPlayerName());
						String scoreName = ScorePlayerTeam.formatPlayerName(scorePlayerTeam,
								scoreList.get(i).getPlayerName());
						if (scoreName.indexOf("Server:") > 0) {
							scorePlayerTeam = scoreboard.getPlayersTeam(scoreList.get(i + 1).getPlayerName());
							scoreName = ScorePlayerTeam
									.formatPlayerName(scorePlayerTeam, scoreList.get(i + 1).getPlayerName())
									.replaceAll("\u00A7[0-9a-z]", "").trim();
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

			if (System.currentTimeMillis() > this.nextCheckFly) {
				this.nextCheckFly = System.currentTimeMillis() + 500L;
				if (getSubServer().equalsIgnoreCase("lobby")) {
					GrieferGames.getGriefergames().setFlyActive(false);
				} else {
					GrieferGames.getGriefergames()
							.setFlyActive(LabyModCore.getMinecraft().getPlayer().capabilities.allowFlying);
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onPreRender(RenderGameOverlayEvent event) {
		if (getMc().gameSettings.keyBindPlayerList.isKeyDown() && !getMc().isIntegratedServerRunning()
				&& GrieferGames.getSettings().isAMPEnabled()) {
			ScoreObjective scoreobjective = LabyModCore.getMinecraft().getWorld().getScoreboard()
					.getObjectiveInDisplaySlot(0);
			NetHandlerPlayClient handler = LabyModCore.getMinecraft().getPlayer().connection;
			if (handler.getPlayerInfoMap().size() > 1 || scoreobjective != null) {
				Collection<NetworkPlayerInfo> players = handler.getPlayerInfoMap();
				for (NetworkPlayerInfo player : players) {
					if (player.getDisplayName() != null) {
						ITextComponent playerDisplayName = (ITextComponent) player.getDisplayName();
						if (playerDisplayName.getUnformattedText().length() > 0) {
							String oldMessage = playerDisplayName.getFormattedText().replaceAll("\u00A7", "§");
							if (oldMessage.indexOf("§k") != -1) {
								ITextComponent newPlayerDisplayName = new TextComponentString("");
								for (ITextComponent component : playerDisplayName.getSiblings()) {
									if (component.getStyle().getObfuscated() && component.getUnformattedText()
											.matches("(([A-z\\-]+\\+?) \\| (\\w{1,16}))")) {
										Style playerDisplayNameStyling = component.getStyle().createDeepCopy()
												.setObfuscated(false);
										String chatRepText = GrieferGames.getSettings().getAMPTablistReplacement();

										if (chatRepText.indexOf("%CLEAN%") == -1) {
											chatRepText = GrieferGames.getSettings().getDefaultAMPTablistReplacement();
										}

										chatRepText = chatRepText.replaceAll("%CLEAN%", component.getUnformattedText());
										chatRepText = "${REPSTART}" + chatRepText + "${REPEND}";

										newPlayerDisplayName.appendSibling(new TextComponentString(
												chatRepText.replace("${REPSTART}", "").replace("${REPEND}", ""))
														.setStyle(playerDisplayNameStyling));
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
}