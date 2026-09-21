package com.mcdiamondfire.modapi.fabric.internal.mapping;

import com.mcdiamondfire.modapi.Semver;
import com.mcdiamondfire.modapi.fabric.client.HandshakeException;
import com.mcdiamondfire.modapi.fabric.model.Location;
import com.mcdiamondfire.modapi.fabric.model.plot.ActionReference;
import com.mcdiamondfire.modapi.fabric.model.player.Mode;
import com.mcdiamondfire.modapi.fabric.model.player.PlayerInfo;
import com.mcdiamondfire.modapi.fabric.model.player.Ranks;
import com.mcdiamondfire.modapi.fabric.model.player.Ranks.Rank;
import com.mcdiamondfire.modapi.fabric.model.plot.*;
import com.mcdiamondfire.modapi.fabric.model.server.ServerBooster;
import com.mcdiamondfire.modapi.fabric.model.server.ServerInfo;
import com.mcdiamondfire.modapi.messages.clientbound.plot.S2CPlotInfo;
import com.mcdiamondfire.modapi.messages.clientbound.plot.S2CPlotLineStarterUpdate;
import com.mcdiamondfire.modapi.messages.clientbound.plot.S2CPlotLineStarters;
import com.mcdiamondfire.modapi.messages.clientbound.server.S2CHandshakeResponse;
import com.mcdiamondfire.modapi.messages.clientbound.server.S2CPlayerInfo;
import com.mcdiamondfire.modapi.messages.clientbound.server.S2CServerBooster;
import com.mcdiamondfire.modapi.messages.common.*;
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
					case EVENT -> ServerInfo.ServerType.EVENT;
					case LOCAL -> ServerInfo.ServerType.LOCAL;
					case PRIVATE -> ServerInfo.ServerType.PRIVATE;
					case UNRECOGNIZED -> ServerInfo.ServerType.UNKNOWN;
				}
		);
	}
	
	public static Mode playerMode(ApiMode mode) {
		return switch (mode) {
			case PLAY -> Mode.PLAY;
			case BUILD -> Mode.BUILD;
			case DEV -> Mode.DEV;
			case CODE_SPECTATE -> Mode.CODE_SPECTATE;
			case VERIFY -> Mode.VERIFY;
			case VANISH -> Mode.VANISH;
			case SPAWN -> Mode.SPAWN;
			case UNKNOWN, UNRECOGNIZED -> Mode.UNKNOWN;
		};
	}
	
	public static PlayerInfo playerInfo(S2CPlayerInfo info) {
		ApiCurrencies currency = info.getCurrency();
		ApiRanks ranks = info.getRanks();
		return new PlayerInfo(
				new PlayerInfo.Currencies(
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
				playerRanks(ranks)
		);
	}
	
	private static Ranks playerRanks(ApiRanks apiRanks) {
		List<Rank> ranks = new ArrayList<>();
		for (ApiRanks.Rank rank : apiRanks.getRanksList()) {
			if (rank == ApiRanks.Rank.UNRECOGNIZED) {
				continue;
			}
			ranks.add(Rank.valueOf(rank.name()));
		}
		return new Ranks(ranks.toArray(Rank[]::new));
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
	
	public static ActionReference actionReference(ApiActionReference reference) {
		return new ActionReference(
				Identifier.parse(reference.getMaterial()),
				ComponentMapper.component(reference.getName()),
				reference.getSignName(),
				ComponentMapper.components(reference.getDescriptionList()),
				reference.getAdditionalInfoList().stream().map(ModelMapper::note).toList(),
				reference.hasTags() ? OptionalInt.of(reference.getTags()) : OptionalInt.empty(),
				reference.getArgumentsList().stream().map(argument -> new ActionReference.Argument(
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
	
	public static List<LineStarter> lineStarters(S2CPlotLineStarters lineStarters) {
		return lineStarters.getLineStarterList().stream().map(ModelMapper::lineStarter).toList();
	}
	
	public static LineStarter lineStarter(ApiLineStarter lineStarter) {
		return new LineStarter(
				blockPosition(lineStarter.getLocation()),
				lineStarter.hasChest() ? Optional.of(actionReference(lineStarter.getChest())) : Optional.empty()
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
	
	private static Region region(ApiRegion region) {
		return new Region(blockPosition(region.getMin()), blockPosition(region.getMax()));
	}
	
	private static PlotTag plotTag(ApiPlotTag tag) {
		if (tag == ApiPlotTag.UNRECOGNIZED) {
			return PlotTag.UNKNOWN;
		}
		return PlotTag.valueOf(tag.name());
	}
	
	private static ActionReference.Note note(ApiActionReference.Note note) {
		return new ActionReference.Note(ComponentMapper.components(note.getNoteList()));
	}
	
	private static ActionReference.ValueType valueType(ApiActionReference.Value value) {
		if (value == ApiActionReference.Value.UNRECOGNIZED) {
			return ActionReference.ValueType.UNKNOWN;
		}
		return ActionReference.ValueType.valueOf(value.name());
	}
	
	private static ActionReference.ReturnValue returnValue(ApiActionReference.ReturnValue value) {
		return switch (value.getReturnValueTypeCase()) {
			case STANDARD_VALUE -> new ActionReference.StandardReturnValue(
					valueType(value.getStandardValue().getValueType()),
					ComponentMapper.components(value.getStandardValue().getDescriptionsList())
			);
			case SIMPLE_VALUE -> new ActionReference.SimpleReturnValue(
					ComponentMapper.component(value.getSimpleValue().getText())
			);
			case RETURNVALUETYPE_NOT_SET -> new ActionReference.SimpleReturnValue(
					net.minecraft.network.chat.Component.empty()
			);
		};
	}
	
}
