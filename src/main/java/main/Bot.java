package main;

import commands.AddPicture;
import commands.ClearText;
import commands.Command;
import commands.Help;
import commands.Reverse;
import commands.Shutdown;
import commands.music.Pause;
import commands.music.Play;
import commands.music.Skip;
import commands.music.Unpause;
import commands.subscription.Subscribe;
import commands.subscription.Unsubscribe;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.ResumedEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Random;

import static main.Globals.DISCORD_TOKEN;
import static main.Globals.logger;

/**
 * @author Patrick Ubelhor
 * @version 4/10/2018
 * TODO: On Twitch startup, verify token is valid
 */
public class Bot extends ListenerAdapter {
	
	private static final char KEY = '!';
	private static final LinkedHashMap<String, Command> commands = Command.getCommandMap();
	
	// Even though we don't use these variables, this still adds them to the HashMap
	private static final Help help = new Help();
	private static final ClearText clearText = new ClearText();
	private static final Play play = new Play();
	private static final Skip skip = new Skip();
	private static final Pause pause = new Pause();
	private static final Unpause unpause = new Unpause();
	private static final AddPicture picture = new AddPicture();
	private static final Reverse reverse = new Reverse();
	private static final Shutdown shutdown = new Shutdown();
	private static final Subscribe sub = new Subscribe();
	private static final Unsubscribe unsub = new Unsubscribe();
	
	static {
		File pics = new File("AtEveryone");
		if (!pics.exists() && !pics.mkdir()) {
			logger.error("Could not create 'AtEveryone' directory!");
		}
		
		File music = new File("Music");
		if (!music.exists() && !music.mkdir()) {
			logger.error("Could not create 'Music' directory!");
		}
	}
	
	
	private static JDA jda;

	public static void main(String[] args) {
		
		try {
			
			jda = new JDABuilder(AccountType.BOT)
				      .setToken(DISCORD_TOKEN)
				      .buildBlocking();
			
			logger.info("Initializing commands...");
			for (Command c : Command.getCommandMap().values().toArray(new Command[] {})) {
				c.init();
			}
			logger.info("Initialization finished.");
			
			jda.addEventListener(new Bot());
			
		} catch (Exception e) {
			logger.fatal("Couldn't initialize bot", e);
		}
		
	}
	
	
	public static JDA getJDA() {
		return jda;
	}

	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		
		User author = event.getAuthor();
		Message message = event.getMessage();
		MessageChannel channel = event.getChannel();
		TextChannel ch = event.getTextChannel();
		String msg = message.getContentDisplay().trim();
		
		// TODO: could possibly make this a subscription service?
		if (message.mentionsEveryone()) {
			// Post atEveryone meme
			File[] pics = new File("AtEveryone").listFiles();
			
			if (pics == null || pics.length == 0) {
				channel.sendMessage("reeeeEEEEEEEEEEEE E E E E E E E E E E E E E").queue();
				return;
			}
			
			channel.sendFile(pics[new Random().nextInt(pics.length)]).queue();
			
			return;
		}
		
		
		/*
		FIXME: Checking for '!' here makes David's autodelete code useless. Check afterwards to fix, but maybe not until
		we complete the 'TODO' below
		 */
		if (msg.length() < 1 || msg.charAt(0) != KEY || author.isBot()) return; // Checking isBot() prevents user from spamming a !reverse

		
		switch (event.getChannelType()) {
			case TEXT:
				// TODO: delete messages after a qualified period of time
				// TODO: make autodelete a subscription service
				if (Objects.equals(ch.getName(), "patricks_taxes") ||
				    Objects.equals(ch.getName(), "twitch_streams")) {
					
					message.delete().queue();
				}
				
				break;
			case PRIVATE:
				// If from a DM, do special stuff here
				return;
			case GROUP:
				// If from a group message, do special stuff here
				return;
		}
		
		String[] args = msg.substring(1).split(" ");
		args[0] = args[0].toLowerCase();
		
		// Runs the command, if it exists and the user has valid permission levels. Otherwise prints an error message
		if (commands.containsKey(args[0])) {
			Command command = commands.get(args[0]);
			
			// TODO: Check for proper permission level
			switch (command.getPerm()) {
				case DISABLED:
					channel.sendMessage("That command has been disabled by the bot admin, sorry!").queue();
					break;
				case USER:
					// Check for USER role
					break;
				case MOD:
					// Check for MOD role
					break;
			}
			
			command.run(event, args);
		} else {
			channel.sendMessage("Unknown or unavailable command").queue();
		}
		
	}
	
	
	@Override
	public void onReconnect(ReconnectedEvent event) {
		logger.info("Reconnected");
	}
	
	
	@Override
	public void onReady(ReadyEvent event) {
		logger.info("Ready");
	}
	
	
	@Override
	public void onResume(ResumedEvent event) {
		logger.info("Resumed");
	}
	
	
	@Override
	public void onDisconnect(DisconnectEvent event) {
		logger.info("Disconnected");
	}
	
}
