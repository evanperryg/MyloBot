package commands.music;

import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;

/**
 * @author PatrickUbelhor
 * @version 05/28/2017
 *
 * TODO: Make bot enter the voice channel of the requester
 * TODO: Make bot leave voice channel after some period of inactivity
 */
public final class Play extends Music {
	
	private boolean active;
	
	public Play() {
		super();
		active = false;
	}
	
	@Override
	public void run(MessageReceivedEvent event, String[] args) {
		
		if (!active) {
			AudioManager am = event.getGuild().getAudioManager();
			VoiceChannel vc = event.getGuild().getVoiceChannels().get(0);
			
			am.setSendingHandler(new AudioPlayerSendHandler(player));
			am.openAudioConnection(vc);
		}
		
		playerManager.loadItem(args[1], new MyAudioLoadResultHandler(trackScheduler)); // queues the track
		
	}
	
	
	@Override
	public String getUsage() {
		return "play <url>";
	}
	
	
	@Override
	public String getDescription() {
		return "Plays the audio from a YouTube video, given as 'url'";
	}
	
}
