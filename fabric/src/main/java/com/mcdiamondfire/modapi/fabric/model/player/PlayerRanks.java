package com.mcdiamondfire.modapi.fabric.model.player;

import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;

/**
 * Represents the ranks of a player.
 */
public final class PlayerRanks {
	
	private final EnumSet<Rank> ranks = EnumSet.noneOf(Rank.class);
	
	@ApiStatus.Internal
	public PlayerRanks(Rank... ranks) {
		Arrays.stream(ranks).forEach(this::addRank);
	}
	
	/**
	 * Returns whether the player has a rank.
	 * <p>
	 *     When a player has a higher level rank on the same branch
	 *     as the given rank, the player is considered to have it.
	 * </p>
	 *
	 * @param rank the rank to check
	 * @return whether the player has that rank
	 */
	public boolean has(Rank rank) {
		return ranks.contains(rank);
	}
	
	/**
	 * Returns the player's regular rank.
	 *
	 * @return the rank, or empty when unranked
	 */
	public Optional<Rank> rank() {
		return highest(Rank.OVERLORD, Rank.MYTHIC, Rank.EMPEROR, Rank.NOBLE);
	}
	
	/**
	 * Returns the player's support rank.
	 *
	 * @return the support rank, or empty when lacking a support rank
	 */
	public Optional<Rank> supportRank() {
		return highest(Rank.SR_HELPER, Rank.HELPER, Rank.JR_HELPER);
	}
	
	/**
	 * Returns the player's moderation rank.
	 *
	 * @return the moderation rank, or empty when lacking a moderation rank
	 */
	public Optional<Rank> moderationRank() {
		return highest(Rank.SR_MOD, Rank.MOD, Rank.JR_MOD);
	}
	
	/**
	 * Returns the player's administration rank.
	 *
	 * @return the administration rank, or empty when lacking an administration rank
	 */
	public Optional<Rank> administrationRank() {
		return highest(Rank.OWNER, Rank.ADMIN, Rank.DEV);
	}
	
	@Override
	public boolean equals(Object object) {
		return object == this || object instanceof PlayerRanks other && ranks.equals(other.ranks);
	}
	
	@Override
	public int hashCode() {
		return ranks.hashCode();
	}
	
	@Override
	public String toString() {
		return "PlayerRanks" + ranks;
	}
	
	private void addRank(Rank rank) {
		switch (rank) {
			case OVERLORD -> addRanks(Rank.NOBLE, Rank.EMPEROR, Rank.MYTHIC, Rank.OVERLORD);
			case MYTHIC -> addRanks(Rank.NOBLE, Rank.EMPEROR, Rank.MYTHIC);
			case EMPEROR -> addRanks(Rank.NOBLE, Rank.EMPEROR);
			case SR_HELPER -> addRanks(Rank.JR_HELPER, Rank.HELPER, Rank.SR_HELPER);
			case HELPER -> addRanks(Rank.JR_HELPER, Rank.HELPER);
			case SR_MOD -> addRanks(Rank.JR_MOD, Rank.MOD, Rank.SR_MOD);
			case MOD -> addRanks(Rank.JR_MOD, Rank.MOD);
			case OWNER -> addRanks(Rank.DEV, Rank.ADMIN, Rank.OWNER);
			case ADMIN -> addRanks(Rank.DEV, Rank.ADMIN);
			default -> ranks.add(rank);
		}
	}
	
	private void addRanks(Rank... ranks) {
		this.ranks.addAll(Arrays.asList(ranks));
	}
	
	private Optional<Rank> highest(Rank... ranks) {
		return Arrays.stream(ranks).filter(this.ranks::contains).findFirst();
	}
	
	/**
	 * Ranks a player can have.
	 */
	public enum Rank {
		/// Noble.
		NOBLE,
		/// Emperor.
		EMPEROR,
		/// Mythic.
		MYTHIC,
		/// Overlord.
		OVERLORD,
		/// VIP.
		VIP,
		/// Feedback team tester.
		TESTER,
		/// YouTube.
		YOUTUBER,
		/// Junior Helper.
		JR_HELPER,
		/// Helper.
		HELPER,
		/// Senior Helper.
		SR_HELPER,
		/// Junior Moderator.
		JR_MOD,
		/// Moderator.
		MOD,
		/// Senior Moderator.
		SR_MOD,
		/// Developer.
		DEV,
		/// Administrator.
		ADMIN,
		/// Owner.
		OWNER
	}
	
}
