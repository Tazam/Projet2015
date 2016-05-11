
/*
 * 
 * TODO 
 * ajouter une fonction/ ameliorer la fonction getWhereSnake
 * La fonction devra prendre 2 poisitions en parametre et verifier qu'il n'y a pas d'obstacle entre les 2 pos
 * (return 2= on passe par un obstacle,  return 1= on s'approche trop pres de la tete d'un serpent adverse, return 0 on est safe)
 * 
 * TODO 
 * optimiser le programme : voici quelque valeur de test 
 *  val max		   		1	   2	   3	   4	   5	   6	    7   	8	    9
 *	tour de boucle		4	   4	   120	   350	   1000	   3000   	6500    27000	40000
 *	temps(ms)			1	   1	   15	   70	   200	   600	    1500    3500	4500
 *	temps/boucle		0,25   0,25	   0,125   0,2	   0,2	   0,2   	0,2    	0,13	0,11
 *	distance (pixel)	1	   1	   4	   30	   120	   400   	500  	650 	800
 *
 *  Il faudrait obtenir :
 *	  temps(ms)	      	500 ou moins
 *    distance (pixel)	750 ou plus
 *
 *  TODO
 *  
 *  Reflechir a un algo qui prend moins de ressource si jamais on n'arrive pas a faire fonctionner l'ia "voisin"
 *  
 *  
 *  Pour l'instant l'ia tourne en rond a cause du 1er todo (les position tester ne sont pas pile sur l'obstacle mais un peux apres)
 */

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
	
	private long startTime = 1000;
	private Direction direction;
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

<<<<<<< HEAD
	public int getWhereSnake(Position position, Position position2, int val, Set<Position>trail)
	{	
		checkInterruption();	// on doit tester l'interruption au début de chaque méthode
		Board board=getBoard();
		for(Snake s: board.snakes)
		{
			checkInterruption();
			//Check si le serpent testé n'est pas l'agent.
			if(s.playerId!=agentSnake.playerId)
			{
				//si on est sur lui, on renvoie 2
				if((position.x==s.currentX && position.y==s.currentY) && (position2.x==s.currentX && position2.y==s.currentY))
				{
					return 2;
				}
				//si on est dans son rayon d'action (prédeterminé)
				if((Math.sqrt((Math.pow((s.currentX-position.x), 2))-(Math.pow((s.currentY-position.y), 2)))<val) && (Math.sqrt((Math.pow((s.currentX-position2.x), 2))-(Math.pow((s.currentY-position2.y), 2)))<val))
				{
					return 1;
				}
			}
			for(Position pos: trail)
			{
				checkInterruption();
				if((position.x==s.currentX && position.y==s.currentY) && (position2.x==s.currentX && position2.y==s.currentY))
				{
					return 2;
				}
			}
=======

	public int getWhereSnake(Position position, int val, Set<Position>trail, Board board)
	{	
		checkInterruption();	// on doit tester l'interruption au début de chaque méthode
		for(Snake s: board.snakes)
		{
			checkInterruption();
			//Check si le serpent testé n'est pas l'agent.
			if(s.playerId!=agentSnake.playerId)
			{
				//si on est sur lui, on renvoie 2
				if(position.x==s.currentX && position.y==s.currentY)
				{
					return 2;
				}
				//si on est dans son rayon d'action (prédeterminé)
				if(Math.sqrt((Math.pow((s.currentX-position.x), 2))-(Math.pow((s.currentY-position.y), 2)))<val)
				{
					return 1;
				}
			}
			if(position.x<0 || position.y<0 || position.x>board.width || position.y>board.height)
				return 2;
			for(Position pos: trail)
			{
				checkInterruption();
				if(position.x==s.currentX && position.y==s.currentY)
				{
					return 2;
				}
			}
>>>>>>> branch 'master' of https://github.com/Tazam/Projet2015.git
		}
		return 0;
	}
<<<<<<< HEAD
=======
	
	

	
>>>>>>> branch 'master' of https://github.com/Tazam/Projet2015.git

	@Override
	public Direction processDirection() {
		
		checkInterruption();
		long time = System.currentTimeMillis();
		Board board = getBoard();
		if(board == null)
			return null;
		else
		{
			if(border.size()==0)
				ObstacleBorder(board);
			if(agentSnake==null)
				agentSnake = board.snakes[getPlayerId()];
			updateAngles();
			Set<Position> trail = new TreeSet<Position>(border);
			Position posSnake = new Position(agentSnake.currentX,agentSnake.currentY);
			getObstacle(board, trail);
			System.out.println("debut");
			int niveau=0;
			System.out.println("ma pos="+posSnake);
			bestChoice(board, trail,posSnake,0, false, currentAngle, posSnake, niveau);
			startTime=(System.currentTimeMillis()-time+startTime)/2;
			System.out.println(startTime);
			System.out.println("fin");
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
	 
	public double bestChoice(Board board, Set<Position> trail, Position pos, double val, boolean danger, double angle, Position posSnake, int niveau, Position lastpos)
	{
		checkInterruption();
		HashMap<Direction, Double > valeurDirection = new HashMap<Direction, Double >();
		double resultat = val;
		if(pos!=posSnake)
		{
<<<<<<< HEAD
			resultat.put(Direction.NONE,0.0);
			return resultat;
		}
		int res = getWhereSnake(pos,10,trail);
		if(res==0)
			if(danger)
=======
			if(niveau>=6) // nombre de tour de boucle max
			{
				return resultat;
			}
			int res = getWhereSnake(pos, lastpos,10, trail , board);
			if(res==0)
				if(danger)
					val+=0.5;
				else
					val++;
			if(res==1)
			{
>>>>>>> branch 'master' of https://github.com/Tazam/Projet2015.git
				val+=0.5;
				danger=true;
			}
			if(res==2)
			{
				return resultat;
			}
		}
		Pair<Position, Double> cpos = new Pair<Position, Double>();
		cpos = calculatePosition(Direction.RIGHT, pos, angle);
		valeurDirection.put(Direction.RIGHT, bestChoice(board, trail, cpos.getFirst(), val, danger, cpos.getSecond(),posSnake,niveau+1, pos));
		checkInterruption();
		cpos = calculatePosition(Direction.LEFT, pos, angle);
		valeurDirection.put(Direction.LEFT, bestChoice(board, trail, cpos.getFirst(), val, danger, cpos.getSecond(),posSnake,niveau+1, pos));
		checkInterruption();
		cpos = calculatePosition(Direction.NONE, pos, angle);
		valeurDirection.put(Direction.NONE, bestChoice(board, trail, cpos.getFirst(), val, danger, cpos.getSecond(),posSnake,niveau+1, pos));
		checkInterruption();
		double dist = Math.sqrt(
				Math.pow(pos.x-cpos.getFirst().x, 2) 
				+ Math.pow(pos.y-cpos.getFirst().y,2));
		if(valeurDirection.get(Direction.RIGHT)>=valeurDirection.get(Direction.LEFT) && valeurDirection.get(Direction.RIGHT)>=valeurDirection.get(Direction.NONE))
		{
			resultat = valeurDirection.get(Direction.RIGHT);
			if(niveau<=2)
			System.out.println("niveau="+niveau+" pos="+pos+ " cpos="+cpos.getFirst()+ "dist="+dist);
			direction = Direction.RIGHT;
		}
		else if(valeurDirection.get(Direction.LEFT)>=valeurDirection.get(Direction.RIGHT) && valeurDirection.get(Direction.LEFT)>=valeurDirection.get(Direction.NONE))
		{
			resultat = valeurDirection.get(Direction.LEFT);
			if(niveau<=2)
			System.out.println("niveau="+niveau+" pos="+pos+ " cpos="+cpos.getFirst()+ "dist="+dist);
			direction = Direction.LEFT;
		}
		else
		{
			resultat = valeurDirection.get(Direction.NONE); 
			if(niveau<=2)
			System.out.println("niveau="+niveau+" pos="+pos+ " cpos="+cpos.getFirst()+ "dist="+dist);
			direction = Direction.NONE;
		}
		if(niveau==0)
			System.out.println(valeurDirection);
			
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
		checkInterruption();
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
