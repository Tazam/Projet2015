package fr.univavignon.courbes.agents.Pierre;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import fr.univavignon.courbes.agents.Agent;
import fr.univavignon.courbes.common.Board;
import fr.univavignon.courbes.common.Direction;
import fr.univavignon.courbes.common.Position;
import fr.univavignon.courbes.common.Snake;


public class AgentImpl extends Agent
{
	private Snake agentSnake;
	private Set<Position> border = new TreeSet<Position>();
	private static double ANGLE_WIDTH = Math.PI/2;
	/** Direction courante du serpent de l'agent */
	private double currentAngle;
	private static int CORNER_THRESHOLD = 100;
	/**
	 * @param playerId
	 */
	
	private Direction previousDirection = Direction.NONE;

	private long startTime = 0;
	
	
	
	public AgentImpl(Integer playerId) {
		super(playerId);
	}

	@Override
	public Direction processDirection() {
		
		checkInterruption();
		Board board = getBoard();
		if(board == null)
			return null;
		else
		{
			if(border.size()==0)
				ObstacleBorder(board);
			if(startTime>System.currentTimeMillis())
				return previousDirection;
			agentSnake = board.snakes[getPlayerId()];
			updateAngles();
			Set<Position> trail = new TreeSet<Position>(border);
			getObstacle(board, trail);
			Direction direction = bestChoice(board, trail);
			previousDirection = direction;
			startTime=(System.currentTimeMillis()+startTime)/2;
			return direction;
		}
	}
	

	
	private void updateAngles()
	{
		checkInterruption();
		// angle de déplacement
		currentAngle = agentSnake.currentAngle%(2*Math.PI);
		if(currentAngle<0)
			currentAngle+=2*Math.PI;
	}
	 
	
	public Direction bestChoice(Board board, Set<Position> trail)
	{
		checkInterruption();
		HashMap<Direction, Double > distance = new HashMap<Direction, Double >();
	    distance.put(Direction.RIGHT, 0.0);
	    distance.put(Direction.NONE, 0.0);
	    distance.put(Direction.LEFT, 0.0);
		HashMap<Direction, Position > pos = new HashMap<Direction, Position >();

	    HashMap<Direction, Double > angle = new HashMap<Direction, Double >();
	    angle.put(Direction.NONE, Math.PI/2);
	    angle.put(Direction.RIGHT, Math.PI/2);
	    angle.put(Direction.LEFT, Math.PI/2);
		Direction direction = Direction.NONE;
		Direction directiontmp = Direction.NONE;
		boolean sight=false;
		for(Position position: trail)
		{
			double tmp = Math.sqrt(Math.pow(agentSnake.currentX-position.x, 2)+ Math.pow(agentSnake.currentY-position.y,2));
			
			double angletmp = (Math.atan2(position.y-agentSnake.currentY, position.x-agentSnake.currentX))%(2*Math.PI);
			if(angletmp<0)
				angletmp+=2*Math.PI;
			
				if((currentAngle>=angletmp - Math.PI/2 && currentAngle<=angletmp + Math.PI/2 ) || (currentAngle>=angletmp - 2*Math.PI - Math.PI/2 && currentAngle<=angletmp - 2*Math.PI+ Math.PI/2)|| (currentAngle>=angletmp + 2*Math.PI - Math.PI/2 && currentAngle<=angletmp + 2*Math.PI+ Math.PI/2))
					sight=true;
				else
					sight=false;
			
				if(angletmp>(currentAngle - Math.PI/2) && angletmp<currentAngle-0.25)
					directiontmp = Direction.RIGHT;
				else if(angletmp>currentAngle+0.25 && angletmp<(currentAngle + Math.PI/2))
					directiontmp = Direction.LEFT;
				else if((currentAngle + Math.PI/2)>2*Math.PI+0.25 && angletmp<(currentAngle + Math.PI/2-2*Math.PI)-0.25)
					directiontmp = Direction.LEFT;
				else if((currentAngle - Math.PI/2)<-0.25 && angletmp>(currentAngle - Math.PI/2)+2*Math.PI+0.25)
					directiontmp = Direction.RIGHT;
				else directiontmp = Direction.NONE;
				
			if(isInCorner(position, board))
				tmp/=2;
			if( sight &&  (tmp<distance.get(directiontmp) || distance.get(directiontmp)==0)&& tmp>agentSnake.headRadius)
			{	
				distance.put(directiontmp, tmp);
				pos.put(directiontmp, position);
			}
			
		}
		
		
		double none = distance.get(Direction.NONE);
		double left = distance.get(Direction.LEFT);
		double right = distance.get(Direction.RIGHT);
		if(pos.get(Direction.NONE)!=null)
		{
			int tmp=board.width/2-pos.get(Direction.NONE).x;
			if(tmp<0)
				tmp*=-1;
			none -= Math.sqrt(tmp)*Math.log(tmp)/2;
			tmp=board.height/2-pos.get(Direction.NONE).y;
			if(tmp<0)
				tmp*=-1;
			none -= Math.sqrt(tmp)*Math.log(tmp)/2;
		}
		if(pos.get(Direction.LEFT)!=null)
		{
			int tmp=board.width/2-pos.get(Direction.LEFT).x;
			if(tmp<0)
				tmp*=-1;
			left -= Math.sqrt(tmp)*Math.log(tmp)/2;
			tmp=board.height/2-pos.get(Direction.LEFT).y;
			if(tmp<0)
				tmp*=-1;
			left -= Math.sqrt(tmp)*Math.log(tmp)/2;
		}
		if(pos.get(Direction.RIGHT)!=null)
		{
			int tmp=board.width/2-pos.get(Direction.RIGHT).x;
			if(tmp<0)
				tmp*=-1;
			right -= Math.sqrt(tmp)*Math.log(tmp)/2;
			tmp=board.height/2-pos.get(Direction.RIGHT).y;
			if(tmp<0)
				tmp*=-1;
			right -= Math.sqrt(tmp)*Math.log(tmp)/2;
		}
		if((left>=0 && right<left*1.2 && right>left*0.8) || (left<0 && right>left*1.2 && right<left*0.8)  && right>none) //si right et left presque pareil , on prend right
		{
			return Direction.RIGHT;
		}
		else if((none>=0 && right<none*1.2 && right>none*0.8) || (none<0 && right>none*1.2 && right<none*0.8) && right>left) // si right et none presque pareil , on prend right
		{
			return Direction.RIGHT;
		}
		else if((none>=0 && left<none*1.2 && left>none*0.8) || (none<0 && left>none*1.2 && left<none*0.8 ) && left>right) // si left et none presque pareil, on prend left
		{
			return Direction.LEFT;
		}
		
		if(none>left && none>right)
			direction=Direction.NONE;
		else if(left>right)
			direction=Direction.LEFT;
		else
			direction=Direction.RIGHT;
		
		return direction;
	}
	
	private boolean isInCorner(Position position, Board board)
	{
		checkInterruption();	// on doit tester l'interruption au début de chaque méthode
		
		boolean result = position.x<CORNER_THRESHOLD && position.y<CORNER_THRESHOLD
			|| board.width-position.x<CORNER_THRESHOLD && position.y<CORNER_THRESHOLD
			|| position.x<CORNER_THRESHOLD && board.height-position.y<CORNER_THRESHOLD
			|| board.width-position.x<CORNER_THRESHOLD && board.height-position.y<CORNER_THRESHOLD;
		return result;
	}
	
	
	public void getObstacle(Board board, Set<Position> trail)
	{		
		checkInterruption();
		for(int i=0;i<board.snakes.length;++i)
		{	
			Snake snake = board.snakes[i];	
			trail.addAll(snake.oldTrail);
			trail.addAll(snake.newTrail);
		}
	}
	
	private void ObstacleBorder(Board board)
	{	
		checkInterruption();
		for(int i =0; i<getBoard().width ; i++ )
		{	
			Position pos = new Position(i,0);
			Position pos2 = new Position(i,800);
			border.add(pos);
			border.add(pos2);			
		}
		for(int i =0; i<board.height ; i++ )
		{	
			Position pos = new Position(0,i);
			Position pos2 = new Position(800,i);
			border.add(pos);
			border.add(pos2);
		}
	}
	
	
}

