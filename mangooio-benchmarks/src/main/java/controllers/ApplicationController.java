package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import interfaces.Constants;
import io.mangoo.routing.Response;
import models.Fortune;
import models.Message;
import models.World;
import services.DataService;
import utils.RandomUtils;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class ApplicationController {
	private DataService dataService;
	private final Message message = new Message(Constants.HELLO_WORLD);
	private final Fortune fortune = new Fortune(0, Constants.FORTUNE_MESSAGE);

	@Inject
	public ApplicationController(DataService dataService) {
		this.dataService = dataService;
	}

	public Response index() {
		return Response.withOk()
				.andEmptyBody();
	}

	public Response json() {
    	return Response.withOk()
    			.andJsonBody(message);
    }
	
	public Response db() {
		World world = dataService.findById(RandomUtils.getRandomId()); 
    	return Response.withOk()
    			.andJsonBody(world);
    }
	
	public Response queries(String queries) {
		List<World> worlds = dataService.getWorlds(queries);
		return Response.withOk()
				.andJsonBody(worlds);
    }
	
	public Response plaintext() {
    	return Response.withOk()
    			.andTextBody(Constants.HELLO_WORLD);
    }
	
	public Response fortunes() {
		List<Fortune> fortunes = dataService.findAllFortunes();
		fortunes.add(fortune);
		Collections.sort(fortunes);
		
    	return Response.withOk()
    			.andContent("fortunes", fortunes);
    }
	
	public Response updates(String queries) {
		List<World> worlds = new ArrayList<>();
		dataService.getWorlds(queries).forEach(world -> {
			world.setRandomnumber(RandomUtils.getRandomId());
			dataService.save(world);
			worlds.add(world);
		});
		
		return Response.withOk()
				.andJsonBody(worlds);
    }
}