package net.stormdev.mario.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import net.stormdev.mario.mariokart.main;

import org.bukkit.entity.Player;

public class RaceQueue {
	private RaceTrack track = null;
	private Boolean starting = false;
	private int playerLimit = 2;
	private RaceType type = RaceType.RACE;
	private List<Player> players = new ArrayList<Player>();
	private UUID queueId = null;

	public RaceQueue(RaceTrack track, RaceType type, Player creator) {
		this.track = track;
		this.type = type;
		this.queueId = UUID.randomUUID();
		this.playerLimit = track.getMaxPlayers();
		this.players.add(creator);
		LinkedHashMap<UUID, RaceQueue> trackQueues = new LinkedHashMap<UUID, RaceQueue>();
		if (main.plugin.queues.containsKey(getTrackName())) {
			trackQueues = main.plugin.queues.get(getTrackName());
		}
		trackQueues.put(queueId, this);
		main.plugin.queues.put(getTrackName(), trackQueues); // Queue is now
																// registered
																// with the
																// system
	}

	public String getTrackName() {
		return track.getTrackName();
	}

	public void setTrack(RaceTrack track) {
		this.track = track;
		this.playerLimit = track.getMaxPlayers();
		main.plugin.raceQueues.updateQueue(this);
	}

	public RaceTrack getTrack() {
		return track;
	}

	public Boolean isStarting() {
		return starting;
	}

	public void setStarting(Boolean starting) {
		this.starting = starting;
		main.plugin.raceQueues.updateQueue(this);
		main.plugin.raceScheduler.recalculateQueues();
		return;
	}

	public int getMaxPlayers() {
		return playerLimit;
	}

	public RaceType getRaceMode() {
		return type;
	}

	public void setRaceMode(RaceType type) {
		this.type = type;
		main.plugin.raceQueues.updateQueue(this);
	}

	public UUID getQueueId() {
		return queueId;
	}

	public void regenQueueId() {
		this.queueId = UUID.randomUUID();
		main.plugin.raceQueues.updateQueue(this);
	}

	public Boolean validatePlayers() {
		Boolean valid = true;
		ArrayList<String> leftPlayers = new ArrayList<String>();
		try {
			for (Player p : getPlayers()) {
				if (p == null || !p.isOnline()) {
					players.remove(p);
					leftPlayers.add(p.getName());
				}
			}
		} catch (Exception e) {
			// Error checking if players valid
		}
		if (players.size() < 1) {
			valid = false;
		}
		if (!valid) { // If there's not enough players in the queue
			clear();
			LinkedHashMap<UUID, RaceQueue> trackQueues = new LinkedHashMap<UUID, RaceQueue>();
			if (main.plugin.queues.containsKey(getTrackName())) {
				trackQueues = main.plugin.queues.get(getTrackName());
			}
			trackQueues.remove(queueId);
			main.plugin.queues.put(getTrackName(), trackQueues); // Queue is now
																	// un-registered
																	// with the
																	// system
			return false;
		}
		for (String s : leftPlayers) {
			broadcast(main.colors.getTitle() + "[MarioKart:] "
					+ main.colors.getInfo() + s
					+ main.msgs.get("race.que.left"));
		}
		return true;
	}

	public int playerCount() {
		try {
			validatePlayers();
		} catch (Exception e) {
			//Game voided
		}
		return players.size();
	}
	
	public int currentPlayerCount() {
		return players.size();
	}

	public int playerLimit() {
		return playerLimit;
	}

	public List<Player> getPlayers() {
		return new ArrayList<Player>(players);
	}

	public Boolean addPlayer(Player player) {
		if (player != null && player.isOnline()
				&& (playerCount() + 1 < playerLimit)) {
			players.add(player);
			main.plugin.raceQueues.updateQueue(this);
			main.plugin.raceScheduler.recalculateQueues();
			main.plugin.signManager.updateSigns(getTrack());
			return true;
		}
		return false;
	}

	public void removePlayer(Player player) {
		players.remove(player);
		validatePlayers();
		broadcast(main.colors.getTitle() + "[MarioKart:] "
				+ main.colors.getInfo() + player.getName()
				+ main.msgs.get("race.que.left"));
		main.plugin.raceQueues.updateQueue(this);
		main.plugin.signManager.updateSigns(getTrack());
		main.plugin.raceScheduler.recalculateQueues();
	}

	public void removePlayer(String player) {
		for (Player p : getPlayers()) {
			if (p.getName().equals(player)) {
				main.plugin.signManager.updateSigns(getTrack());
				removePlayer(p);
				return;
			}
		}
	}
	
	public void quietRemovePlayer(String player) {
		for (Player p : getPlayers()) {
			if (p.getName().equals(player)) {
				removePlayer(p);
				return;
			}
		}
	}

	public void clear() {
		this.players.clear();
		this.type = RaceType.RACE;
		starting = false;
		main.plugin.raceQueues.updateQueue(this);
	}

	public Boolean containsPlayer(Player player) {
		return players.contains(player);
	}

	public void broadcast(String message) {
		validatePlayers();
		for (Player p : getPlayers()) {
			p.sendMessage(main.colors.getInfo() + message);
		}
		return;
	}

}
