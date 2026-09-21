package com.mcdiamondfire.modapi.fabric.internal.mapping;

import com.mcdiamondfire.modapi.fabric.code.CodeOperation;
import com.mcdiamondfire.modapi.fabric.code.CodeOperationResult;
import com.mcdiamondfire.modapi.fabric.code.CodeTemplate;
import com.mcdiamondfire.modapi.fabric.code.LineStarterType;
import com.mcdiamondfire.modapi.messages.clientbound.plot.S2CCodeOperationResult;
import com.mcdiamondfire.modapi.messages.common.ApiCodeTemplate;
import com.mcdiamondfire.modapi.messages.common.ApiLineStarterType;
import com.mcdiamondfire.modapi.messages.serverbound.plot.*;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

@ApiStatus.Internal
public final class CodeMapper {
	
	private CodeMapper() {
		throw new UnsupportedOperationException();
	}
	
	public static C2SCodeOperation operation(CodeOperation operation) {
		C2SCodeOperation.Builder builder = C2SCodeOperation.newBuilder();
		return switch (operation) {
			case CodeOperation.GetByLocation get -> builder.setGetByLocation(
					GetByLocationOperation.newBuilder().setLocation(ModelMapper.blockPosition(get.location()))
			).build();
			case CodeOperation.GetByBlock get -> builder.setGetByBlock(
					GetByBlockOperation.newBuilder().setType(lineStarterType(get.type())).setAction(get.name())
			).build();
			case CodeOperation.Place place -> builder.setPlace(
					PlaceOperation.newBuilder()
							.setLocation(ModelMapper.blockPosition(place.location()))
							.setTemplate(template(place.template()))
			).build();
			case CodeOperation.DeleteByLocation delete -> builder.setDeleteByLocation(
					DeleteByLocationOperation.newBuilder().setLocation(ModelMapper.blockPosition(delete.location()))
			).build();
			case CodeOperation.DeleteByBlock delete -> builder.setDeleteByBlock(
					DeleteByBlockOperation.newBuilder().setType(lineStarterType(delete.type())).setAction(delete.name())
			).build();
			case CodeOperation.ReplaceByLocation replace -> builder.setReplaceByLocation(
					ReplaceByLocationOperation.newBuilder()
							.setLocation(ModelMapper.blockPosition(replace.location()))
							.setTemplate(template(replace.template()))
			).build();
			case CodeOperation.ReplaceByBlock replace -> builder.setReplaceByBlock(
					ReplaceByBlockOperation.newBuilder()
							.setType(lineStarterType(replace.type()))
							.setAction(replace.name())
							.setTemplate(template(replace.template()))
			).build();
		};
	}
	
	public static CodeOperationResult result(S2CCodeOperationResult result) {
		return switch (result.getResultCase()) {
			case TEMPLATE -> new CodeOperationResult.Success(Optional.of(result.getTemplate()));
			case ERROR -> result.getError() == S2CCodeOperationResult.Error.NONE
					? new CodeOperationResult.Success(Optional.empty())
					: new CodeOperationResult.Failure(error(result.getError()));
			case RESULT_NOT_SET -> new CodeOperationResult.Failure(CodeOperationResult.Error.NO_OPERATION);
		};
	}
	
	private static ApiLineStarterType lineStarterType(LineStarterType type) {
		return ApiLineStarterType.valueOf(type.name());
	}
	
	private static ApiCodeTemplate template(CodeTemplate template) {
		ApiCodeTemplate.Builder builder = ApiCodeTemplate.newBuilder();
		return builder.setData(template.value()).build();
	}
	
	private static CodeOperationResult.Error error(S2CCodeOperationResult.Error error) {
		if (error == S2CCodeOperationResult.Error.UNRECOGNIZED) {
			return CodeOperationResult.Error.UNKNOWN;
		}
		return CodeOperationResult.Error.valueOf(error.name());
	}
	
}
