
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

/*
 * Fonction prenant en paramètre une position et un int et qui regarde si la position touche un snake adverse, renvoie 2
 * si jamais on est à une distance x (le int) on renvoie 1, sinon renvoie 0;
 */

public class AgentImpl extends Agent
{
	/** Moitié de l'angle de vision de l'agent, i.e. délimitant la zone traitée devant lui pour détecter des obstacles. Contrainte : doit être inférieure à PI */
	private static double ANGLE_WIDTH = Math.PI/2;
	/** Distance en pixels à partir de laquelle on considère qu'on est dans un coin */
	private static int CORNER_THRESHOLD = 100;
	private Set<Position> border = new TreeSet<Position>();
	/** Direction courante du serpent de l'agent */
	private double currentAngle;
	private Direction previousDirection = Direction.NONE;

	private long startTime = 1000;
	/**
	 * Crée un agent contrôlant le joueur spécifié
	 * dans la partie courante.
	 * 
	 * @param playerId
	 * 		Numéro du joueur contrôlé par cet agent.
	 */
	public AgentImpl(Integer playerId) 
	{	super(playerId);
	}

	/** Serpent contrôlé par l'agent */
	private Snake agentSnake;
	/** Temps avant que l'agent ne change de direction */ 
	private long timeBeforeDirChange = 0;

	public int getWhereSnake(Position position, int x)
	{	
		checkInterruption();	// on doit tester l'interruption au début de chaque méthode
		Board board=getBoard();
		for(Snake s: board.snakes)
		{
			checkInterruption();
			if(s.playerId!=agentSnake.playerId)
			{
				if(position.x==s.currentX && position.y==s.currentY)
				{
					return 2;
				}
				/*else if(position.x==add(s, x).x && position.y==add(s, x).y)
				{
					return 1;
				}*/
			}
			
		}
		return 0;
	}
	
	public Position add(Position serp, int val)
	{
		serp.x+=val;
		serp.y+=val;
		return serp;
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
			HashMap<Direction, Double > valeurDirection = bestChoice(board, trail,posSnake,0, false, currentAngle);
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
	 
	
	public HashMap<Direction, Double > bestChoice(Board board, Set<Position> trail, Position pos, double val, boolean danger, double angle)
	{
		checkInterruption();
		HashMap<Direction, Double > valeurDirection = new HashMap<Direction, Double >();
		HashMap<Direction, Double > resultat = new HashMap<Direction, Double >();
		if(val>100)
		{
			resultat.put(Direction.NONE,0.0);
			return resultat;
		}
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
		
		Pair<Position, Double> cpos = calculatePosition(Direction.RIGHT, pos, angle);
		valeurDirection.putAll(bestChoice(board, trail, cpos.getFirst(), val, danger, cpos.getSecond()));
		cpos = calculatePosition(Direction.LEFT, pos, angle);
		valeurDirection.putAll(bestChoice(board, trail, cpos.getFirst(), val, danger, cpos.getSecond()));
		cpos = calculatePosition(Direction.NONE, pos, angle);
		valeurDirection.putAll(bestChoice(board, trail, cpos.getFirst(), val, danger, cpos.getSecond()));
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
