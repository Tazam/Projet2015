package fr.univavignon.courbes.agents.voisin;

import fr.univavignon.courbes.agents.Agent;
import fr.univavignon.courbes.common.Direction;
import fr.univavignon.courbes.common.Position;
import fr.univavignon.courbes.common.Snake;

/**
 * @author uapv1504059
 *
 */
public class AgentImpl extends Agent {

	/**
	 * @param playerId Id of the player
	 */
	Snake agentSnake;
	long startTime;
	public AgentImpl(Integer playerId) {
		super(playerId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Direction processDirection() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @param d direction of the Snake
	 * @param p position of the Snake
	 * @param angle angle of the Snake
	 * @return calculated position and angle of the Snake
	 */
	public Pair<Position, Double> calculatePosition(Direction d, Position p, double angle){
		
		double finalAngle = angle*Math.PI/180;
		float distance = startTime*agentSnake.movingSpeed;
		
		if(d == Direction.LEFT )
			finalAngle = finalAngle + (agentSnake.turningSpeed*startTime);
		else if(d == Direction.RIGHT)
			finalAngle = finalAngle - (agentSnake.turningSpeed*startTime);
		
		double x = distance*Math.cos(finalAngle);
		double y = distance*Math.sin(finalAngle);
		
		Position finalPosition = new Position((int)Math.round(p.x+x), (int)Math.round(p.y+y));
		
		Pair<Position, Double> pair = new Pair<Position, Double>(finalPosition, angle);
		
		return pair;
	}

}
