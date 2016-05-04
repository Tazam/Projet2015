package fr.univavignon.courbes.agents.voisin;

/*
 * Courbes
 * Copyright 2015-16 L3 Info UAPV 2015-16
 * 
 * This file is part of Courbes.
 * 
 * Courbes is free software: you can redistribute it and/or modify it under the terms 
 * of the GNU General Public License as published by the Free Software Foundation, 
 * either version 2 of the License, or (at your option) any later version.
 * 
 * Courbes is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Courbes. If not, see <http://www.gnu.org/licenses/>.
 */

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

	private long startTime = 1000;
	
	
	
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
			Position posSnake = new Position(agentSnake.currentX,agentSnake.currentY);
			getObstacle(board, trail);
			HashMap<Direction, Double > valeurDirection = bestChoice(board, trail,posSnake,0, false);
			if(valeurDirection.containsKey(Direction.RIGHT))
				previousDirection = Direction.RIGHT;
			else if(valeurDirection.containsKey(Direction.LEFT))
				previousDirection = Direction.LEFT;
			else
				previousDirection = Direction.NONE;
			startTime=(System.currentTimeMillis()+startTime)/2;
			return previousDirection;
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
	 
	
	public HashMap<Direction, Double > bestChoice(Board board, Set<Position> trail, Position pos, double val, boolean danger)
	{
		checkInterruption();
		HashMap<Direction, Double > valeurDirection = new HashMap<Direction, Double >();
		HashMap<Direction, Double > resultat = new HashMap<Direction, Double >();
		int res = getWhereSnake(pos,10);
		if(res==0)
			if(danger)
				val+=0.5;
			else
				val++;
		if(res==1)
		{
			val+=0.5;
			danger=true;
		}
		if(res==2)
		{
			resultat.put(Direction.NONE,0.0);
			return resultat;
		}
		valeurDirection.putAll(bestChoice(board, trail, calculatedPosition(Direction.RIGHT, pos), val), danger);
		valeurDirection.putAll(bestChoice(board, trail, calculatedPosition(Direction.LEFT, pos), val), danger);
		valeurDirection.putAll(bestChoice(board, trail, calculatedPosition(Direction.NONE, pos), val), danger);
		if(valeurDirection.get(Direction.RIGHT)>valeurDirection.get(Direction.LEFT) && valeurDirection.get(Direction.RIGHT)>valeurDirection.get(Direction.NONE))
			resultat.put(Direction.RIGHT,valeurDirection.get(Direction.RIGHT)); 
		else if(valeurDirection.get(Direction.LEFT)>valeurDirection.get(Direction.RIGHT) && valeurDirection.get(Direction.LEFT)>valeurDirection.get(Direction.NONE))
			resultat.put(Direction.LEFT,valeurDirection.get(Direction.LEFT)); 
		else
			resultat.put(Direction.NONE,valeurDirection.get(Direction.NONE)); 
		return resultat;
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

