package superapp.logic.concreteServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import superapp.boundaries.command.MiniAppCommandBoundary;
import superapp.boundaries.grab.GrabPollBoundary;
import superapp.boundaries.object.SuperAppObjectBoundary;
import superapp.boundaries.user.UserIdBoundary;
import superapp.converters.SuperAppObjectConverter;
import superapp.converters.UserConverter;
import superapp.dal.SuperAppObjectEntityRepository;
import superapp.data.*;
import superapp.logic.GrabsService;
import superapp.logic.MiniAppServices;
import superapp.util.exceptions.CannotProcessException;
import superapp.util.exceptions.ForbbidenOperationException;
import superapp.util.exceptions.InvalidInputException;
import superapp.util.exceptions.NotFoundException;
import superapp.util.geoLocationAPI.MapBoxConverter;
import superapp.util.geoLocationAPI.RestaurantGeoLocationHandler;

import java.util.*;

import static superapp.data.ObjectTypes.*;
import static superapp.util.Constants.*;

@Service("Grab")
public class GrabService implements GrabsService, MiniAppServices {
	private SuperAppObjectEntityRepository objectRepository;
	private SuperAppObjectConverter objectConverter;
	private UserConverter userConverter;
	private RestaurantGeoLocationHandler restaurantHandler;

	private final Random RANDOM = new Random();
	private final List<GrabCuisines> CUISINES = List.of(GrabCuisines.values());
	private final int CUISINES_SIZE = CUISINES.size();
	private final int DEFAULT_RADIUS = 5;
	private final int DEFAULT_LIMIT = 5;

	@Autowired
	public GrabService(SuperAppObjectEntityRepository objectRepository,
					   SuperAppObjectConverter objectConverter,
					   UserConverter userConverter) {
		this.objectRepository = objectRepository;
		this.objectConverter = objectConverter;
		this.userConverter = userConverter;
		this.restaurantHandler = new RestaurantGeoLocationHandler(new MapBoxConverter());
	}

	@Override
	public void handleObjectByType(SuperAppObjectBoundary object) {
		String objectType = object.getType();
		if (!isValidObjectType(objectType) || ObjectTypes.valueOf(objectType) != GrabPoll)
			throw new InvalidInputException(UNKNOWN_OBJECT_EXCEPTION);

		this.checkPollData(object);
	}

	@Override
	public Object runCommand(MiniAppCommandBoundary command) {
		SuperappObjectPK targetObjectKey = this.objectConverter.idToEntity(command.getTargetObject().getObjectId());
		UserIdBoundary invokedBy = command.getInvokedBy().getUserId();
		SuperAppObjectEntity poll = this.objectRepository.findById(targetObjectKey)
				.orElseThrow(() ->  new NotFoundException(VALUE_NOT_FOUND_EXCEPTION.formatted("Grab poll")));
		SuperAppObjectEntity group =
				poll.getParents()
				.stream()
				.findFirst() // grab poll can only be bound to one parent
				.orElseThrow(() ->  new NotFoundException(OBJECT_NOT_BOUND_EXCEPTION.formatted("Grab poll")));

		if (!isUserInGroup(group, invokedBy))
			throw new InvalidInputException(USER_NOT_IN_GROUP_EXCEPTION);
		if (!(group.getActive() && poll.getActive()))
			throw new InvalidInputException(EXECUTE_ON_INACTIVE_EXCEPTION.formatted("group or poll"));

		String commandCase = command.getCommand();
		switch (commandCase) {
			case "addVote" -> {
				checkIsValidVote(command);
				List<GrabCuisines> cuisines =
						((List<String>) command.getCommandAttributes().get("cuisines"))
								.stream()
								.map(GrabCuisines::valueOf)
								.toList();
				this.addVote(poll, cuisines);
				return null;
			}
			case "selectRandomly" -> { return this.selectRandomly(poll); }
			case "selectByMajority" -> { return this.selectByMajority(poll); }
			default -> throw new NotFoundException(UNKNOWN_COMMAND_EXCEPTION);
		}
	}

	@Override
	public void checkValidBinding(SuperAppObjectEntity parent, SuperAppObjectEntity child, UserPK userId) {
		if (!parent.getType().equals(ObjectTypes.Group.name()))
			throw new InvalidInputException("Cannot bind poll to non-group objects");
		if (child.getParents().size() > 0)
			throw new ForbbidenOperationException("Grab poll can only be bound to one group");
	}

	@Override
	public void addVote(SuperAppObjectEntity poll, List<GrabCuisines> votes) {
		Map<String, Integer> existingVotes =
				(Map<String, Integer>)this.objectConverter
						.detailsToMap(poll.getObjectDetails())
						.get("votes");
		if (existingVotes == null)
			existingVotes = new HashMap<>();

		for (GrabCuisines vote: votes) {
			String name = vote.name();
			if (existingVotes.containsKey(name))
				existingVotes.replace(name , existingVotes.get(name) + 1 );
			else
				existingVotes.put(name , 1);
		}

		Map<String, Object> newVotes = new HashMap<>();
		newVotes.put("votes", existingVotes);
		poll.setObjectDetails(this.objectConverter.detailsToString(newVotes));
		this.objectRepository.save(poll);
	}

	@Override
	public Object selectRandomly(SuperAppObjectEntity poll) {
		GrabCuisines chosenCuisine = CUISINES.get(RANDOM.nextInt(CUISINES_SIZE));
		List<Map<String, Object>> suggestedRestaurants =
				getRestaurantSuggestions(chosenCuisine, DEFAULT_LIMIT, DEFAULT_RADIUS);

		GrabPollBoundary selection =  new GrabPollBoundary(chosenCuisine, suggestedRestaurants);
		addSelectionToPoll(poll, selection);
		return disableAndSave(poll);
	}

	@Override
	public SuperAppObjectBoundary selectByMajority(SuperAppObjectEntity poll) {
		Map<String, Object> pollDetails =
				(Map<String, Object>)this.objectConverter
						.detailsToMap(poll.getObjectDetails())
						.get("votes");
		if (pollDetails == null)
			throw new CannotProcessException("No votes registered");

		GrabCuisines chosenCuisine = null;
		int totalVotes = 0;
		for (Map.Entry<String, Object> entry : pollDetails.entrySet()) {
			Integer votes = (Integer)entry.getValue();
			if (votes > totalVotes) {
				chosenCuisine = GrabCuisines.valueOf(entry.getKey());
				totalVotes = votes;
			}
		}
		List<Map<String, Object>> suggestedRestaurants =
				getRestaurantSuggestions(chosenCuisine, DEFAULT_LIMIT, DEFAULT_RADIUS);

		GrabPollBoundary selection =  new GrabPollBoundary(chosenCuisine, suggestedRestaurants);
		addSelectionToPoll(poll,selection);
		return disableAndSave(poll);
	}

	private void addSelectionToPoll(SuperAppObjectEntity poll, GrabPollBoundary selection) {
		Map<String, Object> pollDetails = this.objectConverter.detailsToMap(poll.getObjectDetails());
		if (pollDetails == null)
			pollDetails = new HashMap<>();
		pollDetails.put("selection", selection);
		poll.setObjectDetails(this.objectConverter.detailsToString(pollDetails));
	}

	private SuperAppObjectBoundary disableAndSave(SuperAppObjectEntity poll) {
		poll.setActive(false);
		this.objectRepository.save(poll);
		return this.objectConverter.toBoundary(poll);
	}

	private void checkPollData(SuperAppObjectBoundary poll) {
		if (!poll.getActive())
			throw new ForbbidenOperationException("Cannot operate on inactive poll");

		GrabPollBoundary selection = (GrabPollBoundary)poll.getObjectDetails().get("selection");
		if (selection != null) {
			disableAndSave(this.objectConverter.toEntity(poll));
			throw new ForbbidenOperationException("Cannot operate on a poll that have ended");
		}
	}

	private void checkIsValidVote(MiniAppCommandBoundary command) {
		try {
			List<GrabCuisines> cuisines =
					((List<String>)command.getCommandAttributes()
							.get("cuisines"))
							.stream()
							.map(GrabCuisines::valueOf)
							.toList();
		} catch (IllegalArgumentException e) {
			throw new InvalidInputException("Unknown cuisine");
		} catch (NullPointerException e) {
			throw new InvalidInputException("Missing cuisines list");
		}
	}

	private List<Map<String, Object>> getRestaurantSuggestions(GrabCuisines cuisine, int limit, int radius) {
		if (cuisine == null)
			return null;

		return this.restaurantHandler.getRestaurantByCuisine(cuisine.name(),limit,radius);
	}

	private boolean isUserInGroup(SuperAppObjectEntity group, UserIdBoundary userId) {
		List<UserIdBoundary> members = this.userConverter.mapListToBoundaryList(
				(List<Map<String, String>>)this.objectConverter
						.detailsToMap(group.getObjectDetails())
						.get("members"));

		return (members != null && members.contains(userId));
	}
}
