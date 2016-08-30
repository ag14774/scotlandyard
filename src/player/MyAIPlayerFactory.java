package player;

import gui.Gui;
import net.PlayerFactory;
import scotlandyard.Colour;
import scotlandyard.Player;
import scotlandyard.ScotlandYardView;
import scotlandyard.Spectator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class MyAIPlayerFactory implements PlayerFactory {
	protected Map<Colour, PlayerType> typeMap;

	public enum PlayerType {
		AI, GUI
	}

	String imageFilename;
	String positionsFilename;

	protected List<Spectator> spectators;
	Gui gui;

	public MyAIPlayerFactory() {
		typeMap = new HashMap<Colour, PlayerType>();
		typeMap.put(Colour.Black, MyAIPlayerFactory.PlayerType.AI);
		typeMap.put(Colour.Blue, MyAIPlayerFactory.PlayerType.AI);
		typeMap.put(Colour.Green, MyAIPlayerFactory.PlayerType.GUI);
		typeMap.put(Colour.Red, MyAIPlayerFactory.PlayerType.GUI);
		typeMap.put(Colour.White, MyAIPlayerFactory.PlayerType.GUI);
		typeMap.put(Colour.Yellow, MyAIPlayerFactory.PlayerType.GUI);

		positionsFilename = "resources/pos.txt";
		imageFilename = "resources/map.jpg";

		spectators = new ArrayList<Spectator>();
	}

	public MyAIPlayerFactory(Map<Colour, PlayerType> typeMap, String imageFilename, String positionsFilename) {
		this.typeMap = typeMap;
		this.imageFilename = imageFilename;
		this.positionsFilename = positionsFilename;
		spectators = new ArrayList<Spectator>();
	}

	@Override
	public Player player(Colour colour, ScotlandYardView view, String mapFilename) {
		switch (typeMap.get(colour)) {
		case AI:
			if (colour == Colour.Black) {
				MyAIPlayer ai = new MyAIPlayer(view, mapFilename);
				spectators.add(ai);
				return ai;
			} else{
				DetectiveAIPlayer ai = new DetectiveAIPlayer(view, mapFilename);
				spectators.add(ai);
				return ai;
			}
		case GUI:
			return gui(view);
		default:
			return new RandomPlayer(view, mapFilename);
		}
	}

	@Override
	public void ready() {
		if (gui != null)
			gui.run();
	}

	@Override
	public List<Spectator> getSpectators(ScotlandYardView view) {
		List<Spectator> specs = new ArrayList<Spectator>();
		for(Spectator spec : spectators){
			if(!(spec instanceof Gui))
				specs.add(spec);
		}
		specs.add(gui(view));
		return specs;
	}

	@Override
	public void finish() {
		if (gui != null)
			gui.update();
	}

	private Gui gui(ScotlandYardView view) {
		System.out.println("GUI");
		if (gui == null)
			try {
				gui = new Gui(view, imageFilename, positionsFilename);
				spectators.add(gui);
			} catch (IOException e) {
				e.printStackTrace();
			}
		return gui;
	}
}
