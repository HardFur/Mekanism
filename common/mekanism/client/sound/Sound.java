package mekanism.client.sound;

import java.net.URL;

import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

public abstract class Sound
{
	/** The bundled path where the sound is */
	public String soundPath;
	
	/** A unique identifier for this sound */
	public String identifier;
	
	/** Whether or not this sound is playing */
	public boolean isPlaying = false;
	
	private Object objRef;
	
	/**
	 * A sound that runs off of the PaulsCode sound system.
	 * @param id - unique identifier
	 * @param sound - bundled path to the sound
	 * @param tileentity - the tile this sound is playing from.
	 */
	public Sound(String id, String sound, Object obj, Vector3 loc)
	{
		if(MekanismClient.audioHandler.getFrom(obj) != null)
		{
			return;
		}
		
		synchronized(MekanismClient.audioHandler.sounds)
		{
			soundPath = sound;
			identifier = id;
			objRef = obj;
			
			URL url = getClass().getClassLoader().getResource("assets/mekanism/sound/" + sound);
			
			if(url == null)
			{
				System.out.println("[Mekanism] Invalid sound file: " + sound);
			}
			
			if(SoundHandler.getSoundSystem() != null)
			{
				SoundHandler.getSoundSystem().newSource(false, id, url, sound, true, (float)loc.x, (float)loc.y, (float)loc.z, 0, 16F);
				updateVolume(Minecraft.getMinecraft().thePlayer);
				SoundHandler.getSoundSystem().activate(id);
			}
			
			MekanismClient.audioHandler.sounds.put(obj, this);
		}
	}
	
	/** 
	 * Start looping the sound effect 
	 */
	public void play()
	{
		synchronized(MekanismClient.audioHandler.sounds)
		{
			if(isPlaying)
			{
				return;
			}
			
			if(SoundHandler.getSoundSystem() != null)
			{
				updateVolume(Minecraft.getMinecraft().thePlayer);
				SoundHandler.getSoundSystem().play(identifier);
			}
			
			isPlaying = true;
		}
	}
	
	/** 
	 * Stop looping the sound effect 
	 */
	public void stopLoop()
	{
		synchronized(MekanismClient.audioHandler.sounds)
		{
			if(!isPlaying)
			{
				return;
			}
			
			if(SoundHandler.getSoundSystem() != null)
			{
				updateVolume(Minecraft.getMinecraft().thePlayer);
				SoundHandler.getSoundSystem().stop(identifier);
			}
			
			isPlaying = false;
		}
	}
	
	/** 
	 * Remove the sound effect from the PaulsCode SoundSystem 
	 */
	public void remove()
	{
		synchronized(MekanismClient.audioHandler.sounds)
		{
			if(isPlaying)
			{
				stopLoop();
			}
			
			MekanismClient.audioHandler.sounds.remove(objRef);
			
			if(SoundHandler.getSoundSystem() != null)
			{
				updateVolume(Minecraft.getMinecraft().thePlayer);
				SoundHandler.getSoundSystem().removeSource(identifier);
			}
		}
	}
	
	public abstract boolean update(World world);
	
	public abstract Vector3 getLocation();
	
	public abstract float getMultiplier();
	
	/**
	 * Updates the volume based on how far away the player is from the machine.
	 * @param entityplayer - player who is near the machine, always Minecraft.thePlayer
	 */
    public void updateVolume(EntityPlayer entityplayer)
    {
		synchronized(MekanismClient.audioHandler.sounds)
		{
			try {
				float multiplier = getMultiplier();
		    	float volume = 0;
		    	float masterVolume = MekanismClient.audioHandler.masterVolume;
		    	
		        double distance = entityplayer.getDistance(getLocation().x, getLocation().y, getLocation().z);
		        volume = (float)Math.min(Math.max(masterVolume-((distance*.08F)*masterVolume), 0)*multiplier, 1);
		        volume *= Math.max(0, Math.min(1, MekanismClient.baseSoundVolume));
		
		        if(SoundHandler.getSoundSystem() != null)
		        {
		        	SoundHandler.getSoundSystem().setVolume(identifier, volume);
		        }
			} catch(Exception e) {}
		}
    }
}
