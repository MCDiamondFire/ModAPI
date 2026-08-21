package com.mcdiamondfire.modapi.fabric.internal.mapping;

import com.mcdiamondfire.modapi.Semver;
import com.mcdiamondfire.modapi.fabric.client.HandshakeException;
import com.mcdiamondfire.modapi.fabric.model.Location;
import com.mcdiamondfire.modapi.fabric.model.player.ChestReference;
import com.mcdiamondfire.modapi.fabric.model.player.PlayerInfo;
import com.mcdiamondfire.modapi.fabric.model.player.PlayerMode;
import com.mcdiamondfire.modapi.fabric.model.player.PlayerRanks;
import com.mcdiamondfire.modapi.fabric.model.player.PlayerRanks.Rank;
import com.mcdiamondfire.modapi.fabric.model.plot.*;
import com.mcdiamondfire.modapi.fabric.model.server.ServerBooster;
import com.mcdiamondfire.modapi.fabric.model.server.ServerInfo;
import com.mcdiamondfire.modapi.messages.clientbound.plot.S2CPlotInfo;
import com.mcdiamondfire.modapi.messages.clientbound.plot.S2CPlotLineStarterUpdate;
import com.mcdiamondfire.modapi.messages.clientbound.server.S2CHandshakeResponse;
import com.mcdiamondfire.modapi.messages.clientbound.server.S2CPlayerInfo;
import com.mcdiamondfire.modapi.messages.clientbound.server.S2CServerBooster;
import com.mcdiamondfire.modapi.messages.common.APILocation;
import com.mcdiamondfire.modapi.messages.common.PlayerCurrency;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.util.*;

@ApiStatus.Internal
public final class ModelMapper {
	
	private ModelMapper() {
		throw new UnsupportedOperationException();
	}
	
	public static APILocation location(Location location) {
		return APILocation.newBuilder()
				.setX(location.x())
				.setY(location.y())
				.setZ(location.z())
				.setPitch(location.pitch())
				.setYaw(location.yaw())
				.build();
	}
	
	public static APILocation blockPosition(BlockPos position) {
		return APILocation.newBuilder()
				.setX(position.getX())
				.setY(position.getY())
				.setZ(position.getZ())
				.build();
	}
	
	public static Location location(APILocation location) {
		return new Location(location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
	}
	
	private static BlockPos blockPosition(APILocation location) {
		return BlockPos.containing(location.getX(), location.getY(), location.getZ());
	}
	
	public static ServerInfo serverInfo(
			com.mcdiamondfire.modapi.messages.common.ServerInfo serverInfo
	) {
		return new ServerInfo(
				Semver.parse(serverInfo.getProtocolVersion()),
				serverInfo.getBungeeName(),
				serverInfo.getPatchVersion(),
				switch (serverInfo.getServerType()) {
					case MAIN -> ServerInfo.ServerType.MAIN;
					case BETA -> ServerInfo.ServerType.BETA;
					case DEV -> ServerInfo.ServerType.DEV;
					case PUBLIC_TEST -> ServerInfo.ServerType.PUBLIC_TEST;
					case PUBLIC_EVENT -> ServerInfo.ServerType.EVENT;
					case LOCAL_DEV -> ServerInfo.ServerType.LOCAL;
					case PRIVATE -> ServerInfo.ServerType.PRIVATE;
					case UNRECOGNIZED -> ServerInfo.ServerType.UNKNOWN;
				}
		);
	}
	
	public static PlayerMode playerMode(com.mcdiamondfire.modapi.messages.common.PlayerMode mode) {
		return switch (mode) {
			case PLAY -> PlayerMode.PLAY;
			case BUILD -> PlayerMode.BUILD;
			case DEV -> PlayerMode.DEV;
			case CODE_STALK -> PlayerMode.CODE_SPECTATE;
			case VERIFY -> PlayerMode.VERIFY;
			case VANISH -> PlayerMode.VANISH;
			case IDLE -> PlayerMode.SPAWN;
			case UNRECOGNIZED -> PlayerMode.UNKNOWN;
		};
	}
	
	public static PlayerInfo playerInfo(S2CPlayerInfo info) {
		PlayerCurrency currency = info.getCurrency();
		com.mcdiamondfire.modapi.messages.common.PlayerPermissions permissions = info.getPermissions();
		return new PlayerInfo(
				new PlayerInfo.PlayerCurrency(
						currency.getTokens(),
						currency.getTickets(),
						currency.getTicketBundlesList().stream()
								.map(bundle -> new PlayerInfo.TicketBundle(
										bundle.getEventName(),
										bundle.getPrizeName(),
										bundle.getTicketAmount()
								))
								.toList(),
						currency.getSparks()
				),
				permissions(permissions)
		);
	}
	
	private static PlayerRanks permissions(
			com.mcdiamondfire.modapi.messages.common.PlayerPermissions permissions
	) {
		List<Rank> ranks = new ArrayList<>();
		addRank(ranks, permissions.getDonor(),
				Rank.NOBLE, Rank.EMPEROR, Rank.MYTHIC, Rank.OVERLORD
		);
		addRank(ranks, permissions.getVip(),
				Rank.VIP
		);
		addRank(ranks, permissions.getQa(),
				Rank.TESTER
		);
		addRank(ranks, permissions.getYoutuber(),
				Rank.YOUTUBER
		);
		addRank(ranks, permissions.getSupport(),
				Rank.JR_HELPER, Rank.HELPER, Rank.SR_HELPER
		);
		addRank(ranks, permissions.getModeration(),
				Rank.JR_MOD, Rank.MOD, Rank.SR_MOD
		);
		addRank(ranks, permissions.getAdmin(),
				Rank.DEV, Rank.ADMIN, Rank.OWNER
		);
		return new PlayerRanks(ranks.toArray(Rank[]::new));
	}
	
	private static void addRank(List<Rank> ranks, int level, Rank... levels) {
		if (level > 0) {
			ranks.add(levels[Math.min(level, levels.length) - 1]);
		}
	}
	
	public static ServerBooster serverBooster(S2CServerBooster booster) {
		if (!booster.getIsActive() || !booster.hasActiveBooster()) {
			return new ServerBooster(Optional.empty());
		}
		
		S2CServerBooster.ActiveBooster active = booster.getActiveBooster();
		return new ServerBooster(Optional.of(new ServerBooster.ActiveBooster(
				active.getTipped(),
				active.getMultiplier(),
				Duration.ofMillis(active.getTimeRemaining()),
				active.getUserName(),
				UUID.fromString(active.getUserUuid())
		)));
	}
	
	public static PlotInfo plotInfo(S2CPlotInfo plot) {
		return new PlotInfo(
				plot.getId(),
				ComponentMapper.component(plot.getName()),
				plot.getOwnerName(),
				UUID.fromString(plot.getOwnerUuid()),
				switch (plot.getPlotSize()) {
					case 1 -> PlotInfo.PlotSize.BASIC;
					case 2 -> PlotInfo.PlotSize.LARGE;
					case 3 -> PlotInfo.PlotSize.MASSIVE;
					case 4 -> PlotInfo.PlotSize.MEGA;
					case 5 -> PlotInfo.PlotSize.WORLD;
					default -> PlotInfo.PlotSize.UNKNOWN;
				},
				region(plot.getBuildRegion()),
				region(plot.getCodeRegion()),
				location(plot.getSpawnPos()),
				plot.getIsOwner(),
				plot.getIsDeveloper(),
				plot.getIsBuilder(),
				plot.getTagsList().stream().map(ModelMapper::plotTag).toList(),
				plot.getHandle().isEmpty() ? Optional.empty() : Optional.of(plot.getHandle()),
				plot.getPlayersList().stream().map(player -> new PlotInfo.PlotPlayer(
						player.getUserName(),
						UUID.fromString(player.getUserUuid()),
						player.getIsOwner(),
						player.getIsDeveloper(),
						player.getIsBuilder(),
						playerMode(player.getMode())
				)).toList()
		);
	}
	
	public static ChestReference chestReference(com.mcdiamondfire.modapi.messages.common.ChestReference reference) {
		return new ChestReference(
				Identifier.parse(reference.getMaterial()),
				ComponentMapper.component(reference.getName()),
				ComponentMapper.components(reference.getDescriptionList()),
				reference.getAdditionalInfoList().stream().map(ModelMapper::note).toList(),
				reference.hasTags() ? OptionalInt.of(reference.getTags()) : OptionalInt.empty(),
				reference.getArgumentsList().stream().map(argument -> new ChestReference.Argument(
						valueType(argument.getType()),
						argument.getPlural(),
						argument.getOptional(),
						ComponentMapper.components(argument.getDescriptionList()),
						argument.getNotesList().stream().map(ModelMapper::note).toList()
				)).toList(),
				reference.getReturnValuesList().stream().map(ModelMapper::returnValue).toList(),
				reference.hasCancellable() ? Optional.of(reference.getCancellable()) : Optional.empty(),
				reference.hasCancelledAutomatically()
						? Optional.of(reference.getCancelledAutomatically())
						: Optional.empty()
		);
	}
	
	public static CodeLineStarter lineStarter(com.mcdiamondfire.modapi.messages.common.CodeLineStarter lineStarter) {
		return new CodeLineStarter(
				blockPosition(lineStarter.getLocation()),
				lineStarter.hasChest() ? Optional.of(chestReference(lineStarter.getChest())) : Optional.empty()
		);
	}
	
	public static LineStarterUpdate lineStarterUpdate(S2CPlotLineStarterUpdate update) {
		return new LineStarterUpdate(
				lineStarter(update.getLineStarter()),
				switch (update.getAction()) {
					case ADD -> LineStarterUpdate.Action.ADD;
					case CHANGE -> LineStarterUpdate.Action.CHANGE;
					case REMOVE -> LineStarterUpdate.Action.REMOVE;
					case UNRECOGNIZED -> LineStarterUpdate.Action.UNKNOWN;
				}
		);
	}
	
	public static HandshakeException.Error handshakeError(S2CHandshakeResponse.Error error) {
		if (error == S2CHandshakeResponse.Error.UNRECOGNIZED) {
			return HandshakeException.Error.UNKNOWN;
		}
		return HandshakeException.Error.valueOf(error.name());
	}
	
	private static Region region(com.mcdiamondfire.modapi.messages.common.Region region) {
		return new Region(blockPosition(region.getMin()), blockPosition(region.getMax()));
	}
	
	private static PlotTag plotTag(com.mcdiamondfire.modapi.messages.common.PlotTag tag) {
		if (tag == com.mcdiamondfire.modapi.messages.common.PlotTag.UNRECOGNIZED) {
			return PlotTag.UNKNOWN;
		}
		return PlotTag.valueOf(tag.name());
	}
	
	private static ChestReference.Note note(com.mcdiamondfire.modapi.messages.common.ChestReference.Note note) {
		return new ChestReference.Note(ComponentMapper.components(note.getNoteList()));
	}
	
	private static ChestReference.ValueType valueType(
			com.mcdiamondfire.modapi.messages.common.ChestReference.Value value
	) {
		if (value == com.mcdiamondfire.modapi.messages.common.ChestReference.Value.UNRECOGNIZED) {
			return ChestReference.ValueType.UNKNOWN;
		}
		return ChestReference.ValueType.valueOf(value.name());
	}
	
	private static ChestReference.ReturnValue returnValue(
			com.mcdiamondfire.modapi.messages.common.ChestReference.ReturnValue value
	) {
		return switch (value.getReturnValueTypeCase()) {
			case STANDARD_VALUE -> new ChestReference.StandardReturnValue(
					valueType(value.getStandardValue().getValueType()),
					ComponentMapper.components(value.getStandardValue().getDescriptionsList())
			);
			case SIMPLE_VALUE -> new ChestReference.SimpleReturnValue(
					ComponentMapper.component(value.getSimpleValue().getText())
			);
			case RETURNVALUETYPE_NOT_SET -> new ChestReference.SimpleReturnValue(
					net.minecraft.network.chat.Component.empty()
			);
		};
	}
	
}
